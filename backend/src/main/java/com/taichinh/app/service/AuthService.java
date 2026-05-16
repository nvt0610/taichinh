package com.taichinh.app.service;

import com.taichinh.app.dto.auth.LoginRequest;
import com.taichinh.app.dto.auth.LoginResponse;
import com.taichinh.app.dto.auth.RefreshTokenRequest;
import com.taichinh.app.dto.auth.RegisterRequest;
import com.taichinh.app.dto.auth.RegisterResponse;
import com.taichinh.app.dto.auth.TokenResponse;
import com.taichinh.app.entity.RefreshToken;
import com.taichinh.app.entity.Role;
import com.taichinh.app.entity.User;
import com.taichinh.app.exception.BusinessException;
import com.taichinh.app.exception.ErrorCode;
import com.taichinh.app.repository.RefreshTokenRepository;
import com.taichinh.app.repository.RoleRepository;
import com.taichinh.app.repository.UserRepository;
import com.taichinh.app.security.JwtService;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String DEFAULT_ROLE_NAME = "USER";

    private final SecureRandom secureRandom = new SecureRandom();
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long refreshTokenExpirationMillis;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            RefreshTokenRepository refreshTokenRepository,
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${jwt.refresh-expiration}") long refreshTokenExpirationMillis) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenExpirationMillis = refreshTokenExpirationMillis;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String normalizedUsername = request.username().trim();
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByUsernameAndDeletedAtIsNull(normalizedUsername)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Username is already in use.");
        }
        if (userRepository.existsByEmailAndDeletedAtIsNull(normalizedEmail)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Email is already in use.");
        }

        Role defaultRole = roleRepository.findByName(DEFAULT_ROLE_NAME)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR, "Default user role is not configured."));

        User user = new User(normalizedUsername, normalizedEmail, passwordEncoder.encode(request.password()));
        User savedUser = userRepository.saveAndFlush(user);

        assignRole(savedUser, defaultRole);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getCreatedAt());
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String identifier = request.usernameOrEmail().trim();

        User user = findActiveUserByIdentifier(identifier);

        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED, "User account is inactive.");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Invalid username/email or password.");
        }

        user.setLastLoginAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        List<String> roles = findRoleNamesByUserId(savedUser.getId());
        String accessToken = jwtService.generateAccessToken(savedUser, roles);
        RefreshToken refreshToken = createRefreshToken(savedUser);

        return new LoginResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                roles,
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                jwtService.getAccessTokenExpirationMillis() / 1000,
                refreshTokenExpirationMillis / 1000,
                savedUser.getLastLoginAt());
    }

    @Transactional
    public TokenResponse refreshAccessToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = findValidRefreshToken(request.refreshToken());
        User user = findActiveUserById(refreshToken.getUserId());
        List<String> roles = findRoleNamesByUserId(user.getId());

        return new TokenResponse(
                jwtService.generateAccessToken(user, roles),
                "Bearer",
                jwtService.getAccessTokenExpirationMillis() / 1000);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByToken(request.refreshToken())
                .ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                    refreshTokenRepository.save(refreshToken);
                });
    }

    private User findActiveUserByIdentifier(String identifier) {
        return userRepository.findByUsernameAndDeletedAtIsNull(identifier)
                .or(() -> userRepository.findByEmailAndDeletedAtIsNull(identifier.toLowerCase(Locale.ROOT)))
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_CREDENTIALS,
                        "Invalid username/email or password."));
    }

    private User findActiveUserById(java.util.UUID userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN, "Invalid refresh token."));

        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED, "User account is inactive.");
        }

        return user;
    }

    private RefreshToken createRefreshToken(User user) {
        String token = generateOpaqueToken();
        LocalDateTime expiredAt = LocalDateTime.now().plus(refreshTokenExpirationMillis, ChronoUnit.MILLIS);
        return refreshTokenRepository.save(new RefreshToken(user.getId(), token, expiredAt));
    }

    private RefreshToken findValidRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN, "Invalid refresh token."));

        if (refreshToken.isRevoked()) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN, "Refresh token has been revoked.");
        }
        if (!refreshToken.getExpiredAt().isAfter(LocalDateTime.now())) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN, "Refresh token has expired.");
        }

        return refreshToken;
    }

    private String generateOpaqueToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private void assignRole(User user, Role role) {
        jdbcTemplate.update(
                "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)",
                user.getId(),
                role.getId());
    }

    private List<String> findRoleNamesByUserId(java.util.UUID userId) {
        return jdbcTemplate.query(
                """
                SELECT r.name
                FROM roles r
                JOIN user_roles ur ON ur.role_id = r.id
                WHERE ur.user_id = ?
                ORDER BY r.name
                """,
                (rs, rowNum) -> rs.getString("name"),
                userId);
    }
}

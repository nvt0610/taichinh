package com.taichinh.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taichinh.app.dto.common.ApiResponse;
import com.taichinh.app.dto.common.ErrorResponse;
import com.taichinh.app.exception.ErrorCode;
import com.taichinh.app.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final ObjectMapper objectMapper;

  public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.objectMapper = objectMapper;
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(exception -> exception
            .authenticationEntryPoint((request, response, authException) ->
                writeErrorResponse(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    ErrorCode.UNAUTHORIZED,
                    ErrorCode.UNAUTHORIZED.getDefaultMessage()))
            .accessDeniedHandler((request, response, accessDeniedException) ->
                writeErrorResponse(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    ErrorCode.FORBIDDEN,
                    ErrorCode.FORBIDDEN.getDefaultMessage())))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()
            .requestMatchers("/api/wallets/**").authenticated()
            .requestMatchers("/api/categories/**").authenticated()
            .requestMatchers("/api/transactions/**").authenticated()
            .requestMatchers("/api/dashboard/**").authenticated()
            .anyRequest().authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  private void writeErrorResponse(
      HttpServletResponse response,
      int status,
      ErrorCode errorCode,
      String message) throws java.io.IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getWriter(), ApiResponse.error(message, ErrorResponse.of(errorCode)));
  }
}

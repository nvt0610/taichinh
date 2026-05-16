package com.taichinh.app.security;

import com.taichinh.app.exception.BusinessException;
import com.taichinh.app.exception.ErrorCode;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserProvider {

    public UUID getUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Authenticated user is required.");
        }

        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Authenticated user principal is invalid.");
        }
    }
}

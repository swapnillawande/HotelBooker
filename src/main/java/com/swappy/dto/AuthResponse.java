package com.swappy.dto;

import java.time.LocalDateTime;

public record AuthResponse(String accessToken, LocalDateTime expiresAt, UserProfileDto user) {
}

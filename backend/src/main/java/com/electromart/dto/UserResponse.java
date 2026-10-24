package com.electromart.dto;

import com.electromart.entity.User;

/** Profile data returned by GET /api/users/me. Contains no Google tokens and no internal fields. */
public record UserResponse(
        Long id,
        String name,
        String email,
        String pictureUrl,
        String role
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPictureUrl(),
                user.getRole().name());
    }
}

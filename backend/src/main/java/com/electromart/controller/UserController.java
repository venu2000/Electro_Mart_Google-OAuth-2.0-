package com.electromart.controller;

import com.electromart.dto.UserResponse;
import com.electromart.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        // The name is the "sub" of OUR JWT (our user id), set by JwtAuthenticationFilter.
        // Identity never comes from the request body or query string.
        Long userId = Long.valueOf(authentication.getName());
        return UserResponse.from(userService.getById(userId));
    }
}

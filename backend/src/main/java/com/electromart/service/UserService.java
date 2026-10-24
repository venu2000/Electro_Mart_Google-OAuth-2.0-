package com.electromart.service;

import com.electromart.entity.AuthProvider;
import com.electromart.entity.Role;
import com.electromart.entity.User;
import com.electromart.exception.ResourceNotFoundException;
import com.electromart.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Called after Google has authenticated the user. Looks up the local user by Google's
     * stable "sub" and creates it on first login. Never creates a duplicate for the same sub.
     */
    @Transactional
    public User findOrCreateGoogleUser(String googleSub, String email, String name, String pictureUrl) {
        return userRepository.findByProviderAndGoogleId(AuthProvider.GOOGLE, googleSub)
                .map(existing -> {
                    existing.updateProfile(email, name, pictureUrl);
                    return existing;
                })
                .orElseGet(() -> userRepository.save(
                        new User(googleSub, email, name, pictureUrl, AuthProvider.GOOGLE, Role.USER)));
    }

    @Transactional(readOnly = true)
    public User findByGoogleSubject(String googleSub) {
        return userRepository.findByProviderAndGoogleId(AuthProvider.GOOGLE, googleSub)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}

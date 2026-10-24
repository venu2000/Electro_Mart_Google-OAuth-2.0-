package com.electromart.repository;

import com.electromart.entity.AuthProvider;
import com.electromart.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderAndGoogleId(AuthProvider provider, String googleId);
}

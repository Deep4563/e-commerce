package com.chiragbhisikar.e_commerce.repository;

import com.chiragbhisikar.e_commerce.model.User;
import com.chiragbhisikar.e_commerce.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUserId(Long userId);
}

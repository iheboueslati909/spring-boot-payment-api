package com.payment.api.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import com.payment.api.model.auth.RefreshToken;
import com.payment.api.model.auth.User;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByToken(String token);
  int deleteByUser(User user);
}

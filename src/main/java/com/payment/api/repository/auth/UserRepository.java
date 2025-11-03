package com.payment.api.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UserRepository extends JpaRepository<com.payment.api.model.auth.User, Long> {
  Optional<com.payment.api.model.auth.User> findByEmail(String email);
}

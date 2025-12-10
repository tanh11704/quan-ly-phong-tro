package com.tpanh.backend.repository;

import com.tpanh.backend.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(final String username);

    Optional<User> findByZaloId(final String zaloId);

    Optional<User> findByEmail(final String email);
}

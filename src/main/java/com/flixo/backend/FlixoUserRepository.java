package com.flixo.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FlixoUserRepository extends JpaRepository<FlixoUser, Long> {

    Optional<FlixoUser> findByEmail(String email);

    boolean existsByEmail(String email);
}
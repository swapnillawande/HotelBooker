package com.swappy.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.swappy.entities.AuthSession;

public interface AuthSessionRepository extends JpaRepository<AuthSession, Long> {

    @Query("""
            select distinct session
            from AuthSession session
            join fetch session.user user
            left join fetch user.roles
            where session.tokenHash = :tokenHash
              and session.revokedAt is null
              and session.expiresAt > :now
            """)
    Optional<AuthSession> findActiveSession(
            @Param("tokenHash") String tokenHash,
            @Param("now") LocalDateTime now);
}

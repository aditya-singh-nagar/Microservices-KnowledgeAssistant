package com.knowledgeassistant.authservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.knowledgeassistant.authservice.entities.User;
import com.knowledgeassistant.authservice.enums.AccountStatus;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);
    Optional<User> findByUserName(String username);
    boolean existsByEmail(String email);
    boolean existsByUserName(String username);

    List<User> findByAccountStatus(AccountStatus accountStatus);
}

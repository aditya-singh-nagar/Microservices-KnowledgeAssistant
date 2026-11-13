package com.knowledgeassistant.usermanagementservice.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.knowledgeassistant.usermanagementservice.entities.User;
import com.knowledgeassistant.usermanagementservice.enums.AccountStatus;

public interface UserRepository extends JpaRepository<User, Long> {
	
	Optional<User> findByEmail(String email);
    Optional<User> findByUserName(String userName);
    // Used by Admins/Managers to get all users waiting for approval
    List<User> findByAccountStatus(AccountStatus accountStatus);
    boolean existsByEmail(String email);
    boolean existsByUserName(String userName);
	
//    Optional<User>  findById(String approverId);
}

package com.knowledgeassistant.usermanagementservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.knowledgeassistant.usermanagementservice.entities.User;

public interface UserRepository extends JpaRepository<Long, User> {

}

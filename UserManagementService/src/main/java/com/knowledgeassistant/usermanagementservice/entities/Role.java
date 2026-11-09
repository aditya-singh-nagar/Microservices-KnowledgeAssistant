package com.knowledgeassistant.usermanagementservice.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String role; // e.g. "ROLE_ADMIN", "ROLE_EMPLOYEE", "ROLE_MANAGER", "ROLE_PUBLIC"

    private String description;
}
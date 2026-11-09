package com.knowledgeassistant.authservice.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.UpdateTimestamp;

import com.knowledgeassistant.authservice.enums.AccountStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "users")
@Data
@RequiredArgsConstructor

public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false,unique = true,length = 50)
	private String userName;
	
	@Column(nullable = false)
	private String password;
	
	@Column(nullable = false,unique = true,length = 50)
	private String email;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "account_status",nullable = false)
	private AccountStatus accountStatus;
	
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "user_roles",joinColumns = @JoinColumn(name = "user_id"),
	inverseJoinColumns = @JoinColumn(name="role_id"))
	private Set<Role> roles = new HashSet<>();
	
	@Column(name = "approved_by")
	private String approvedBy;
	
	@Column(name = "approved_at")
	private LocalDate approvedAt;
	
	@UpdateTimestamp
    @Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();
}

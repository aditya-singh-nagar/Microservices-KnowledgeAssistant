package com.knowledgeassistant.authservice.config;



import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.knowledgeassistant.authservice.entities.Role;
import com.knowledgeassistant.authservice.entities.User;
import com.knowledgeassistant.authservice.enums.AccountStatus;
import com.knowledgeassistant.authservice.repository.RoleRepository;
import com.knowledgeassistant.authservice.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepo, RoleRepository roleRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        Role adminRole = createRoleIfNotFound("ROLE_ADMIN", "Full Administrator with all permissions");
        createRoleIfNotFound("ROLE_MANAGER", "Department Manager can approve employees");
        createRoleIfNotFound("ROLE_EMPLOYEE", "Standard Employee user");
        createRoleIfNotFound("ROLE_PUBLIC", "Public limited user");

        if (userRepo.findByEmail("admin@algoworks.com").isEmpty()) {
            User admin = new User();
            admin.setUserName("ADMIN");
            admin.setEmail("admin@algoworks.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setAccountStatus(AccountStatus.ACTIVE);
            admin.setApprovedBy("SYSTEM");
            admin.setRoles(Set.of(adminRole));

            userRepo.save(admin);
            System.out.println("Default Admin seeded: admin@algoworks.com");
        }
    }

    private Role createRoleIfNotFound(String roleName, String description) {
        return roleRepo.findByRole(roleName).orElseGet(() -> {
            Role newRole = new Role();
            newRole.setRole(roleName);
            newRole.setDescription(description);
            return roleRepo.save(newRole);
        });
    }
}
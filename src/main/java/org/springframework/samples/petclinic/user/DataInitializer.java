package org.springframework.samples.petclinic.user;

import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Initializes the admin user on application startup if it doesn't exist
 */
@Component
public class DataInitializer {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	public DataInitializer(UserRepository userRepository,
						   RoleRepository roleRepository,
						   PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@PostConstruct
	public void initializeAdminUser() {
		// Check if admin already exists
		if (userRepository.findByEmail("admin@carrepair.com").isPresent()) {
			System.out.println("Admin user already exists");
			return;
		}

		try {
			// Create admin user
			User admin = new User();
			admin.setFirstName("Admin");
			admin.setLastName("User");
			admin.setEmail("admin@carrepair.com");
			admin.setPassword(passwordEncoder.encode("Admin123!"));
			admin.setPhone("5551234567");
			admin.setIsActive(true);
			admin.setIsApproved(true);  // Admin is pre-approved

			// Assign ADMIN role
			Role adminRole = roleRepository.findByName("ADMIN")
				.orElseThrow(() -> new RuntimeException("ADMIN role not found in database"));

			Set<Role> roles = new HashSet<>();
			roles.add(adminRole);
			admin.setRoles(roles);

			// Save admin
			userRepository.save(admin);

			System.out.println("===============================================");
			System.out.println("ADMIN USER CREATED SUCCESSFULLY!");
			System.out.println("===============================================");
			System.out.println("Email: admin@carrepair.com");
			System.out.println("Password: Admin123!");
			System.out.println("===============================================");
			System.out.println("CHANGE THIS PASSWORD AFTER FIRST LOGIN!");
			System.out.println("===============================================");

		} catch (Exception e) {
			System.err.println("Failed to create admin user: " + e.getMessage());
			System.err.println("Please make sure ADMIN role exists in the 'roles' table");
		}
	}
}

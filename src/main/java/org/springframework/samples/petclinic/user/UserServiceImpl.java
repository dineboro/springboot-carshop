package org.springframework.samples.petclinic.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * Register student (for instructor's demo project)
	 */
	@Override
	public User registerNewStudent(User user) {
		// Hash the user's password
		user.setPassword(passwordEncoder.encode(user.getPassword()));

		// Students are auto-approved
		user.setIsActive(true);
		user.setIsApproved(true);

		// Default the user's default role
		Role studentRole = roleRepository.findByName("STUDENT")
			.orElseThrow(() -> new RuntimeException("Student Role Not Found"));

		Set<Role> roles = new HashSet<>();
		roles.add(studentRole);
		user.setRoles(roles);

		// Save the user's data
		return userRepository.save(user);
	}

	/**
	 * Register manager - requires admin approval
	 */
	@Override
	public User registerNewManager(User user) {
		// Hash password
		user.setPassword(passwordEncoder.encode(user.getPassword()));

		// Set as NOT approved (admin must approve)
		user.setIsActive(true);
		user.setIsApproved(false);  // ← KEY: Requires approval

		// Assign MANAGER role
		Role managerRole = roleRepository.findByName("MANAGER")
			.orElseThrow(() -> new RuntimeException("MANAGER role not found"));

		Set<Role> roles = new HashSet<>();
		roles.add(managerRole);
		user.setRoles(roles);

		// Save user
		return userRepository.save(user);
	}

	/**
	 * Register customer (for portal access)
	 */
	@Override
	public User registerNewCustomer(User user) {
		// Hash password
		user.setPassword(passwordEncoder.encode(user.getPassword()));

		// Customers are auto-approved
		user.setIsActive(true);
		user.setIsApproved(true);

		// Assign CUSTOMER role
		Role customerRole = roleRepository.findByName("CUSTOMER")
			.orElseThrow(() -> new RuntimeException("Customer Role Not Found"));

		Set<Role> roles = new HashSet<>();
		roles.add(customerRole);
		user.setRoles(roles);

		// Save user
		return userRepository.save(user);
	}

	/**
	 * Create employee (by manager or admin)
	 * Employees = RECEPTIONIST or TECHNICIAN
	 */
	@Override
	public User createEmployee(User user, String roleName, Integer createdBy) {
		// Hash password
		user.setPassword(passwordEncoder.encode(user.getPassword()));

		// Employees are auto-approved when created by manager
		user.setIsActive(true);
		user.setIsApproved(true);

		// Assign role (RECEPTIONIST or TECHNICIAN)
		Role role = roleRepository.findByName(roleName)
			.orElseThrow(() -> new RuntimeException(roleName + " role not found"));

		Set<Role> roles = new HashSet<>();
		roles.add(role);
		user.setRoles(roles);

		// Save user
		return userRepository.save(user);
	}
}

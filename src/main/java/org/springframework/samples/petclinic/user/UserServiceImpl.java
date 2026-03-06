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
		// Hash password
		user.setPassword(passwordEncoder.encode(user.getPassword()));

		// Students are auto-approved
		user.setIsActive(true);
		user.setIsApproved(true);

		// Assign STUDENT role
		Role studentRole = roleRepository.findByName("STUDENT")
			.orElseThrow(() -> new RuntimeException("Student Role Not Found"));

		Set<Role> roles = new HashSet<>();
		roles.add(studentRole);
		user.setRoles(roles);

		return userRepository.save(user);
	}

	/**
	 * NEW: Register any user (Manager, Receptionist, Technician)
	 * - No role assigned (admin will assign later)
	 * - Requires admin approval
	 */
	@Override
	public User registerNewUser(User user) {
		// Hash password
		user.setPassword(passwordEncoder.encode(user.getPassword()));

		// Set as NOT approved (admin must approve AND assign role)
		user.setIsActive(true);
		user.setIsApproved(false);  // ← Requires approval

		// NO ROLE ASSIGNED - admin will assign role when approving
		user.setRoles(new HashSet<>());

		// Save user
		return userRepository.save(user);
	}

	/**
	 * Register customer (for portal access via invitation)
	 */
	@Override
	public User registerNewCustomer(User user) {
		// Hash password
		user.setPassword(passwordEncoder.encode(user.getPassword()));

		// Customers are auto-approved when invited
		user.setIsActive(true);
		user.setIsApproved(true);

		// Assign CUSTOMER role
		Role customerRole = roleRepository.findByName("CUSTOMER")
			.orElseThrow(() -> new RuntimeException("Customer Role Not Found"));

		Set<Role> roles = new HashSet<>();
		roles.add(customerRole);
		user.setRoles(roles);

		return userRepository.save(user);
	}

	/**
	 * NEW: Admin assigns role to user after approval
	 */
	@Override
	public void assignRole(Integer userId, String roleName) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new RuntimeException("User not found"));

		Role role = roleRepository.findByName(roleName)
			.orElseThrow(() -> new RuntimeException(roleName + " role not found"));

		// Add role to user's roles
		Set<Role> roles = user.getRoles();
		if (roles == null) {
			roles = new HashSet<>();
		}
		roles.add(role);
		user.setRoles(roles);

		userRepository.save(user);
	}
}

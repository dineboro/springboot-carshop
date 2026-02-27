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

	// This method is to register user's for Marc's demo project
	public User registerNewStudent(User user) {
		// Hash the user's password
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		// Default the user's default role
		Role studentRole = roleRepository.findByName("STUDENT").orElseThrow(() -> new RuntimeException("Student Role Not Found"));
		Set<Role> roles = new HashSet<>();
		roles.add(studentRole);
		user.setRoles(roles);
		// Save the user's data
		return userRepository.save(user);
	}


}

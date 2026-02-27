package org.springframework.samples.petclinic.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private RoleRepository roleRepository;
	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserServiceImpl userService;

	private User testUser;
	private Role studentRole;

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setEmail("test@kirkwood.edu");
		testUser.setPassword("rawPassword");

		studentRole = new Role();
		studentRole.setName("STUDENT");
	}

	@Test
	void registerNewUser() {
		// --- 1. ARRANGE Mock Behavior (When these methods are called, return this) ---
		// Simulate password hashing: encoder.encode() should return the hashed string
		when(passwordEncoder.encode(testUser.getPassword())).thenReturn("hashedPassword");

		// Simulate role lookup: roleRepository.findByName() should return the STUDENT role
		when(roleRepository.findByName("STUDENT")).thenReturn(Optional.of(studentRole));

		// Simulate save: userRepository.save() should return the user object that was passed to it
		when(userRepository.save(any(User.class))).thenReturn(testUser);

		// --- 2. ACT by calling the method to test ---
		User registeredUser = userService.registerNewStudent(testUser);

		// --- 3. ASSERT by verifying the results ---
		// Check that the user object returned is not null
		assertNotNull(registeredUser);

		// Check that the password was indeed hashed
		assertEquals("hashedPassword", registeredUser.getPassword(), "Password must be hashed.");

		// Check that the STUDENT role was assigned
		assertTrue(registeredUser.getRoles().contains(studentRole), "User must have the STUDENT role.");

		// --- 4. Verify Mock Interactions (Check the service called its dependencies correctly) ---
		// Verify that the encoder was called once
		verify(passwordEncoder, times(1)).encode("rawPassword");

		// Verify that the role repository was called once
		verify(roleRepository, times(1)).findByName("STUDENT");

		// Verify that the user was saved once
		verify(userRepository, times(1)).save(testUser);
	}
}

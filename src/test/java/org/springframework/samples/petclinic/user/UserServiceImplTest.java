package org.springframework.samples.petclinic.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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

	/*
	 * RENAME NOTE: The original method was called registerNewUser() but the body calls
	 * userService.registerNewStudent(). Renamed to registerNewStudent so the test name
	 * matches what is actually being exercised.
	 */
	@Test
	void registerNewStudent() {
		// --- 1. ARRANGE Mock Behavior (When these methods are called, return this) ---
		// Simulate password hashing: encoder.encode() should return the hashed string
		when(passwordEncoder.encode(testUser.getPassword())).thenReturn("hashedPassword");

		// Simulate role lookup: roleRepository.findByName() should return the STUDENT
		// role
		when(roleRepository.findByName("STUDENT")).thenReturn(Optional.of(studentRole));

		// Simulate save: userRepository.save() should return the user object that was
		// passed to it
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

		// --- 4. Verify Mock Interactions (Check the service called its dependencies
		// correctly) ---
		// Verify that the encoder was called once
		verify(passwordEncoder, times(1)).encode("rawPassword");

		// Verify that the role repository was called once
		verify(roleRepository, times(1)).findByName("STUDENT");

		// Verify that the user was saved once
		verify(userRepository, times(1)).save(testUser);
	}

	// =========================================================================
	// registerNewUser — staff registration (no role, needs admin approval)
	// =========================================================================

	/*
	 * Staff register through /register. They get NO role assigned at this point and
	 * isApproved is set to false. An admin must approve them and assign a role
	 * separately. This is the core business rule for the car repair shop workflow.
	 */
	@Test
	void registerNewUser_SetsApprovedFalseAndNoRoles() {
		// Arrange
		when(passwordEncoder.encode("rawPassword")).thenReturn("hashedPassword");
		when(userRepository.save(any(User.class))).thenReturn(testUser);

		// Act
		User result = userService.registerNewUser(testUser);

		// Assert
		assertNotNull(result);
		assertEquals("hashedPassword", result.getPassword(), "Password must be hashed before saving.");

		assertFalse(result.getIsApproved(),
				"Staff accounts must require admin approval — isApproved must be false after registration.");

		assertTrue(result.getIsActive(), "Account should be active so it can be found by the admin approval workflow.");

		assertTrue(result.getRoles().isEmpty(),
				"No role should be assigned at registration — admin assigns the role when approving.");

		// Role repository must never be touched during staff registration
		verifyNoInteractions(roleRepository);
		verify(userRepository, times(1)).save(testUser);
	}

	// =========================================================================
	// registerNewStudent — auto-approved, STUDENT role missing from DB
	// =========================================================================

	/*
	 * If the STUDENT role was never seeded (e.g. someone truncated the roles table),
	 * registerNewStudent should throw rather than silently save a user with no role. Also
	 * verifies the user is NOT saved when role lookup fails.
	 */
	@Test
	void registerNewStudent_MissingStudentRole_ThrowsRuntimeException() {
		// Arrange — role not in database
		when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
		when(roleRepository.findByName("STUDENT")).thenReturn(Optional.empty());

		// Act + Assert
		RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.registerNewStudent(testUser));

		assertTrue(ex.getMessage().contains("Student Role Not Found"),
				"Exception message should tell the developer which role is missing.");

		// User must NOT be persisted if the role lookup failed
		verify(userRepository, never()).save(any());
	}

	// =========================================================================
	// assignRole — admin assigns a role to an approved user
	// =========================================================================

	/*
	 * After approving a staff member, the admin picks a role (MANAGER, RECEPTIONIST,
	 * TECHNICIAN). assignRole() finds the user by ID, finds the role by name, adds the
	 * role to the user's set, and saves.
	 *
	 * We use ArgumentCaptor to inspect exactly what was passed to save() rather than
	 * relying on the return value — this is more reliable because it checks the actual
	 * state of the object that gets persisted.
	 */
	@Test
	void assignRole_AddsRoleToUser() {
		// Arrange
		testUser.setRoles(new HashSet<>()); // starts with no roles

		Role managerRole = new Role();
		managerRole.setId(5);
		managerRole.setName("MANAGER");

		when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
		when(roleRepository.findByName("MANAGER")).thenReturn(Optional.of(managerRole));
		when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

		// Act
		userService.assignRole(1, "MANAGER");

		// Capture what was actually passed to save()
		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(captor.capture());

		// Assert the saved user has the MANAGER role
		assertTrue(captor.getValue().getRoles().stream().anyMatch(r -> r.getName().equals("MANAGER")),
				"MANAGER role should have been added to the user before saving.");
	}

	// =========================================================================
	// assignRole — user not found
	// =========================================================================

	/*
	 * If an admin tries to assign a role to a user ID that does not exist (deleted
	 * between requests), assignRole should throw and NOT call save.
	 */
	@Test
	void assignRole_UnknownUserId_ThrowsRuntimeException() {
		// Arrange — no user with ID 999
		when(userRepository.findById(999)).thenReturn(Optional.empty());

		// Act + Assert
		RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.assignRole(999, "ADMIN"));

		assertTrue(ex.getMessage().contains("User not found"));
		verify(userRepository, never()).save(any());
	}

	// =========================================================================
	// assignRole — role name not found in DB
	// =========================================================================

	/*
	 * If the admin somehow submits a role name that does not exist in the roles table
	 * (e.g. a typo or a stale dropdown), assignRole should throw and NOT save the user
	 * with a broken state.
	 */
	@Test
	void assignRole_UnknownRoleName_ThrowsRuntimeException() {
		// Arrange — user exists but role name is wrong
		testUser.setRoles(new HashSet<>());
		when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
		when(roleRepository.findByName("TYPO_ROLE")).thenReturn(Optional.empty());

		// Act + Assert
		RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.assignRole(1, "TYPO_ROLE"));

		assertTrue(ex.getMessage().contains("TYPO_ROLE"),
				"Exception should name the missing role so it is easy to debug.");
		verify(userRepository, never()).save(any());
	}

	// =========================================================================
	// assignRole — existing roles must not be cleared
	// =========================================================================

	/*
	 * If a user already has USER role and an admin adds MANAGER, they should end up with
	 * both roles. A naive implementation that replaces the set instead of adding to it
	 * would fail this test.
	 */
	@Test
	void assignRole_PreservesExistingRoles() {
		// Arrange — user already has USER role
		Role userRole = new Role();
		userRole.setId(1);
		userRole.setName("USER");

		Set<Role> existingRoles = new HashSet<>();
		existingRoles.add(userRole);
		testUser.setRoles(existingRoles);

		Role managerRole = new Role();
		managerRole.setId(5);
		managerRole.setName("MANAGER");

		when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
		when(roleRepository.findByName("MANAGER")).thenReturn(Optional.of(managerRole));
		when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

		// Act
		userService.assignRole(1, "MANAGER");

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(captor.capture());

		Set<Role> savedRoles = captor.getValue().getRoles();
		assertEquals(2, savedRoles.size(), "User should have both USER and MANAGER roles after assignment.");
		assertTrue(savedRoles.stream().anyMatch(r -> r.getName().equals("USER")));
		assertTrue(savedRoles.stream().anyMatch(r -> r.getName().equals("MANAGER")));
	}

}

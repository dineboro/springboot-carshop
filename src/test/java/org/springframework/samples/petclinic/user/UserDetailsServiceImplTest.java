package org.springframework.samples.petclinic.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserDetailsServiceImpl userDetailsService;

	private User testUser;

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setEmail("example-student@kirkwood.edu");
		testUser.setPassword("hashedPassword");
		testUser.setIsApproved(true);
		testUser.setIsActive(true);
		Role studentRole = new Role();
		studentRole.setName("STUDENT");
		testUser.setRoles(Set.of(studentRole));
	}

	// =========================================================================
	// Your original test — kept exactly as-is
	// =========================================================================

	@Test
	void loadUserByUsername() {
		// Arrange
		// When findByEmail is called with the test email, return the user
		when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

		// Act
		UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getEmail());

		// Assert
		assertNotNull(userDetails);
		assertEquals(testUser.getEmail(), userDetails.getUsername());
		assertEquals(testUser.getPassword(), userDetails.getPassword());
		// Check that the roles were loaded correctly
		assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")));

		verify(userRepository, times(1)).findByEmail(testUser.getEmail());
	}

	// =========================================================================
	// Email not found in the database
	// =========================================================================

	/*
	 * When no user exists for the given email, UserDetailsServiceImpl throws
	 * UsernameNotFoundException. Spring Security catches this and treats it as a failed
	 * login — the user sees the "Invalid email or password" message.
	 *
	 * assertThrows() verifies both the exception type and the message content.
	 */
	@Test
	void loadUserByUsername_UnknownEmail_ThrowsUsernameNotFoundException() {
		// Arrange — repository finds nothing
		when(userRepository.findByEmail("ghost@kirkwood.edu")).thenReturn(Optional.empty());

		// Act + Assert
		UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
				() -> userDetailsService.loadUserByUsername("ghost@kirkwood.edu"));

		assertTrue(ex.getMessage().contains("ghost@kirkwood.edu"),
				"Exception message should include the email that was not found");
	}

	// =========================================================================
	// Account not yet approved (isApproved = false)
	// =========================================================================

	/*
	 * Staff register but must wait for admin approval. UserDetailsServiceImpl throws
	 * DisabledException for unapproved accounts.
	 *
	 * SecurityConfig's failureHandler catches DisabledException specifically and
	 * redirects to /login?disabled instead of /login?error — that is why it must be
	 * DisabledException and not a generic AuthenticationException.
	 */
	@Test
	void loadUserByUsername_UnapprovedAccount_ThrowsDisabledException() {
		// Arrange — user exists but is not approved yet
		testUser.setIsApproved(false);
		when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

		// Act + Assert
		DisabledException ex = assertThrows(DisabledException.class,
				() -> userDetailsService.loadUserByUsername(testUser.getEmail()));

		assertTrue(ex.getMessage().contains("pending approval"),
				"Exception message should mention pending approval so admins can identify the cause");
	}

	// =========================================================================
	// Account deactivated by admin (isActive = false)
	// =========================================================================

	/*
	 * An admin can deactivate an account without deleting it. The service throws
	 * DisabledException here too — the same redirect path as unapproved, but with a
	 * different message so users know why they cannot log in.
	 */
	@Test
	void loadUserByUsername_DeactivatedAccount_ThrowsDisabledException() {
		// Arrange — approved but deactivated
		testUser.setIsActive(false);
		when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

		// Act + Assert
		DisabledException ex = assertThrows(DisabledException.class,
				() -> userDetailsService.loadUserByUsername(testUser.getEmail()));

		assertTrue(ex.getMessage().contains("deactivated"),
				"Exception message should say deactivated so it differs from the pending-approval message");
	}

	// =========================================================================
	// User has multiple roles — all must appear in authorities
	// =========================================================================

	/*
	 * A user could be both TECHNICIAN and MANAGER. All roles must be converted to
	 * GrantedAuthority objects with the ROLE_ prefix or Spring Security's hasRole() /
	 * hasAnyRole() checks will silently fail.
	 */
	@Test
	void loadUserByUsername_MultipleRoles_AllRolesInAuthorities() {
		// Arrange — give testUser two roles
		Role techRole = new Role();
		techRole.setName("TECHNICIAN");
		Role managerRole = new Role();
		managerRole.setName("MANAGER");
		testUser.setRoles(Set.of(techRole, managerRole));

		when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

		// Act
		UserDetails userDetails = userDetailsService.loadUserByUsername(testUser.getEmail());

		// Assert — both roles present with correct ROLE_ prefix
		assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TECHNICIAN")));
		assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")));
		assertEquals(2, userDetails.getAuthorities().size());
	}

}

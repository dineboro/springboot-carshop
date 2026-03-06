package org.springframework.samples.petclinic.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	private final UserRepository userRepository;

	public UserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		// 1. Find the user via the UserRepository
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));

		// 2. Check if user is approved (for managers/staff)
		//  FIXED: Use Boolean.TRUE.equals() to safely handle null
		if (!Boolean.TRUE.equals(user.getIsApproved())) {
			throw new DisabledException("Your account is pending approval. Please contact an administrator.");
		}

		// 3. Check if user is active
		// FIXED: Use Boolean.TRUE.equals() to safely handle null
		if (!Boolean.TRUE.equals(user.getIsActive())) {
			throw new DisabledException("Your account has been deactivated.");
		}

		// 4. Convert your custom User model into the UserDetails object that Spring Security understands
		// FIXED: Use Boolean.TRUE.equals() to avoid NullPointerException
		return org.springframework.security.core.userdetails.User.builder()
			.username(user.getEmail())
			.password(user.getPassword()) // Spring Security will compare this HASH with the login password
			.disabled(!Boolean.TRUE.equals(user.getIsActive()) || !Boolean.TRUE.equals(user.getIsApproved()))
			.roles(user.getRoles().stream()
				.map(role -> role.getName())
				.toArray(String[]::new)) // Converts your Role set into Spring's required format
			.build();
	}
}

package org.springframework.samples.petclinic.user;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	private final UserRepository userRepository;

	public UserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		// 1. Find the user
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));

		// 2. Check if user is approved
		if (!Boolean.TRUE.equals(user.getIsApproved())) {
			throw new DisabledException("Your account is pending approval. Please contact an administrator.");
		}

		// 3. Check if user is active
		if (!Boolean.TRUE.equals(user.getIsActive())) {
			throw new DisabledException("Your account has been deactivated.");
		}

		// 4. Build authorities — use ROLE_ prefix to match Spring Security conventions
		List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
			.map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
			.collect(Collectors.toList());

		// 5. Return UserDetails
		return org.springframework.security.core.userdetails.User.builder()
			.username(user.getEmail())
			.password(user.getPassword())
			.authorities(authorities)
			.build();
	}
}

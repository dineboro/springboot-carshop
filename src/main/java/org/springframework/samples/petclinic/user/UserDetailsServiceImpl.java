package org.springframework.samples.petclinic.user;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
	public UserDetails loadUserByUsername(String emailOrPhone) throws UsernameNotFoundException {
		// 1. Find the user by email first, then fall back to phone number
		User user = userRepository.findByEmail(emailOrPhone)
			.or(() -> userRepository.findByPhone(emailOrPhone))
			.orElseThrow(() -> new UsernameNotFoundException("No user found with: " + emailOrPhone));

		// 2. Block deleted accounts before anything else
		if (user.getDeletedAt() != null) {
			throw new UsernameNotFoundException("No user found with: " + emailOrPhone);
		}

		// 3. Build authorities — use ROLE_ prefix to match Spring Security conventions
		List<SimpleGrantedAuthority> authorities = user.getRoles()
			.stream()
			.map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
			.collect(Collectors.toList());

		// 4. Return UserDetails — disabled(true) lets Spring Security throw
		// DisabledException via preAuthenticationChecks, which reaches the failure
		// handler correctly (throwing directly from here gets wrapped).
		boolean approved = Boolean.TRUE.equals(user.getIsApproved());
		boolean active = Boolean.TRUE.equals(user.getIsActive());
		return org.springframework.security.core.userdetails.User.builder()
			.username(user.getEmail())
			.password(user.getPassword())
			.authorities(authorities)
			.disabled(!approved || !active)
			.build();
	}

}

package org.springframework.samples.petclinic.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@Configuration
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		// Make the AuthenticationManager (which knows about your UserDetailsService)
		// available for injection in your controllers.
		return config.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable()) // Disable Cross-Site Request Forgery for API development
			.authorizeHttpRequests(authorize -> authorize
				// This allows unmapped paths to result in 404, and allows all web viewing.
				.requestMatchers(HttpMethod.GET).permitAll()

				// Allows guest users to make POST requests
				.requestMatchers("/register-student", "/login", "/schools/new", "/owners/new").permitAll()

				// PROTECTED CATCH-ALL (This protects unlisted POST/PUT/DELETE, etc.)
				.anyRequest().authenticated()
			)
			// Ensure all auto-challenge mechanisms are disabled
			.httpBasic(AbstractHttpConfigurer::disable) // Disable the login popup
			.formLogin(AbstractHttpConfigurer::disable); // Stop formLogin redirect

		return http.build();
	}
}

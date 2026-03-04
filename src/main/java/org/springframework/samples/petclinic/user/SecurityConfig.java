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
		return config.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(authorize -> authorize
				// Allow all GET requests (for viewing pages)
				.requestMatchers(HttpMethod.GET).permitAll()

				// Allow POST for registration, login, and creating new subscriptions
				.requestMatchers("/register-student", "/register", "/login", "/schools/new", "/owners/new", "/subscriptions/new").permitAll()

				// Protect all other requests
				.anyRequest().authenticated()
			)
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(form -> form
				.loginPage("/login")
				.usernameParameter("email")
				.defaultSuccessUrl("/login-success", true)
				.failureHandler((request, response, exception) -> {
					// Check if it's a disabled account
					if (exception instanceof org.springframework.security.authentication.DisabledException) {
						response.sendRedirect("/login?disabled");
					} else {
						request.getSession().setAttribute("LAST_EMAIL", request.getParameter("email"));
						response.sendRedirect("/login?error");
					}
				})
				.permitAll()
			)
			.logout(logout -> logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/login?logout")
				.permitAll()
			);

		return http.build();
	}
}

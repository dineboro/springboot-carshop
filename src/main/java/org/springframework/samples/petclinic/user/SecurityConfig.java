package org.springframework.samples.petclinic.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.DisabledException;
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
		http.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(authorize -> authorize
				// Admin only
				.requestMatchers("/admin/**")
				.hasRole("ADMIN")

				// Customer create/edit — MANAGER and RECEPTIONIST only
				.requestMatchers(HttpMethod.GET, "/customers/new", "/customers/*/edit")
				.hasAnyRole("ADMIN", "MANAGER", "RECEPTIONIST")
				.requestMatchers(HttpMethod.POST, "/customers/new", "/customers/*/edit")
				.hasAnyRole("ADMIN", "MANAGER", "RECEPTIONIST")

				// Vehicle create/edit/delete — MANAGER and RECEPTIONIST only
				.requestMatchers(HttpMethod.GET, "/vehicles/new", "/vehicles/*/edit")
				.hasAnyRole("ADMIN", "MANAGER", "RECEPTIONIST")
				.requestMatchers(HttpMethod.POST, "/vehicles/new", "/vehicles/*/edit", "/vehicles/*/delete")
				.hasAnyRole("ADMIN", "MANAGER", "RECEPTIONIST")

				// Appointment create/edit — MANAGER and RECEPTIONIST only
				.requestMatchers(HttpMethod.GET, "/appointments/new", "/appointments/*/edit")
				.hasAnyRole("ADMIN", "MANAGER", "RECEPTIONIST")
				.requestMatchers(HttpMethod.POST, "/appointments/new", "/appointments/*/edit")
				.hasAnyRole("ADMIN", "MANAGER", "RECEPTIONIST")

				// Appointment status/notes update — TECHNICIAN, MANAGER, RECEPTIONIST
				.requestMatchers("/appointments/*/status")
				.hasAnyRole("ADMIN", "MANAGER", "RECEPTIONIST", "TECHNICIAN")

				// Allow all other GET requests (view/read pages)
				.requestMatchers(HttpMethod.GET)
				.permitAll()

				// Public pages anyone can see
				.requestMatchers("/", "/schools/**", "/register-student", "/css/**", "/images/**", "/recipes/**",
						"/recipes/new")
				.permitAll()

				// Require login for the profile and any other user settings
				.requestMatchers("/users/profile", "/users/delete")
				.authenticated()

				// Allow POST for registration, login, and creating new subscriptions
				.requestMatchers("/register-student", "/register", "/login", "/schools/new", "/owners/new",
						"/subscriptions/new")
				.permitAll()

				// Service catalog — managers/admins manage, others read-only
				.requestMatchers(HttpMethod.POST, "/service-catalog/**")
				.hasAnyRole("ADMIN", "MANAGER")
				.requestMatchers(HttpMethod.GET, "/service-catalog/new")
				.hasAnyRole("ADMIN", "MANAGER")

				// Invoices — managers and service advisors
				.requestMatchers(HttpMethod.POST, "/invoices/**")
				.hasAnyRole("ADMIN", "MANAGER", "SERVICE_ADVISOR", "RECEPTIONIST")

				// Estimates — managers and service advisors
				.requestMatchers(HttpMethod.POST, "/estimates/**")
				.hasAnyRole("ADMIN", "MANAGER", "SERVICE_ADVISOR", "RECEPTIONIST")

				// Service lines — managers, service advisors, technicians
				.requestMatchers(HttpMethod.POST, "/appointments/*/service-lines/**")
				.hasAnyRole("ADMIN", "MANAGER", "SERVICE_ADVISOR", "TECHNICIAN")

				// Reminders — managers and receptionists
				.requestMatchers(HttpMethod.POST, "/appointments/*/reminders/**")
				.hasAnyRole("ADMIN", "MANAGER", "RECEPTIONIST")

				// Communications — all staff
				.requestMatchers(HttpMethod.POST, "/communications/**")
				.authenticated()

				// Service history — managers, technicians
				.requestMatchers(HttpMethod.POST, "/service-history/**")
				.hasAnyRole("ADMIN", "MANAGER", "TECHNICIAN")

				// Require login for shop management features
				.requestMatchers("/appointments/**", "/vehicles/**", "/employees/**", "/service-catalog/**",
						"/invoices/**", "/estimates/**", "/communications/**", "/service-history/**")
				.authenticated()

				// Protect all other requests
				.anyRequest()
				.authenticated())
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(form -> form.loginPage("/login")
				.usernameParameter("email")
				.defaultSuccessUrl("/login-success", true)
				.failureHandler((request, response, exception) -> {
					// Check if it's a disabled account
					System.out.println(exception.getMessage());
					if (exception instanceof DisabledException) {
						response.sendRedirect("/login?disabled");
					}
					else {
						request.getSession().setAttribute("LAST_EMAIL", request.getParameter("email"));
						response.sendRedirect("/login?error");
					}
				})
				.permitAll())
			.logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout").permitAll())
			// Handle access denied (when non-admin tries to access admin pages)
			.exceptionHandling(exception -> exception.accessDeniedPage("/access-denied"));

		return http.build();
	}

}

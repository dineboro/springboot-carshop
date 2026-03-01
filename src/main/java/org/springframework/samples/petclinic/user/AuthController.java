package org.springframework.samples.petclinic.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.samples.petclinic.school.School;
import org.springframework.samples.petclinic.school.SchoolRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {
	private final UserService userService;
	private final SchoolRepository schoolRepository;
	private final AuthenticationManager authenticationManager; // Add this field

	// Add to Constructor
	public AuthController(UserService userService, SchoolRepository schoolRepository, AuthenticationManager authenticationManager) {
		this.userService = userService;
		this.schoolRepository = schoolRepository;
		this.authenticationManager = authenticationManager;
	}

	@GetMapping("/register-student")
	public String initRegisterForm(Model model) {
		model.addAttribute("user", new User());
		return "auth/registerForm";
	}

	@PostMapping("/register-student")
	public String processRegisterForm(@Valid User user,
									  BindingResult result,
									  RedirectAttributes redirectAttributes,
									  HttpServletRequest request) {
		if (result.hasErrors()) {
			return "auth/registerForm";
		}

		String rawPassword = user.getPassword();

		// 1. Save the User (UserService handles password hashing)
		try {
			userService.registerNewStudent(user);
		} catch (RuntimeException ex) {
			// Handle duplicate email or other service errors
			result.rejectValue("email", "duplicateEmail", "This email is already registered");
			return "auth/registerForm";
		}

		// To do: Send email verification before auto log in.
		// 2. LOGIN using the authenticationManager.
		try {
			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user.getEmail(), rawPassword);
			Authentication authentication = authenticationManager.authenticate(authToken);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			HttpSession session = request.getSession(true);
			session.setAttribute(
				HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
				SecurityContextHolder.getContext()
			);
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("messageDanger", "Account created, but auto-login failed.");
			return "redirect:/login";
		}

		// 3. Redirect a new user
		String email = user.getEmail();
		Optional<School> school = findSchoolByRecursiveDomain(email);

		if(school.isPresent()) {
			redirectAttributes.addFlashAttribute("messageSuccess",
				"Your user account has been created. You have been redirected to " + school.get().getName() + "'s school page.");
			return "redirect:/schools/" + school.get().getDomain().substring(0, school.get().getDomain().length() - 4);
		} else {
			redirectAttributes.addFlashAttribute("messageWarning",
				"Your user account has been created, but we could not find a school matching your email domain");
			// Redirect a user to the schools page if their school was not found.
			return "redirect:/schools";
		}
	}

	private Optional<School> findSchoolByRecursiveDomain(String email) {
		// 1. Extract the initial domain (e.g., "student.kirkwood.edu")
		String domain = email.substring(email.indexOf("@") + 1);

		// 2. Loop while the domain is valid (has at least one dot)
		while (domain.contains(".")) {
			// 3. Check Database
			Optional<School> school = schoolRepository.findByDomain(domain);
			if (school.isPresent()) {
				return school; // Found match (e.g., "kirkwood.edu")
			}

			// 4. Strip the first part (e.g., "student.kirkwood.edu" -> "kirkwood.edu")
			int dotIndex = domain.indexOf(".");
			domain = domain.substring(dotIndex + 1);
		}

		return Optional.empty();
	}


	/**
	 * Show login page
	 */
	@GetMapping("/login")
	public String showLoginPage() {
		return "login";
	}

	/**
	 * Show customer registration page
	 */
	@GetMapping("/register")
	public String showRegisterPage(Model model) {
		model.addAttribute("user", new User());
		return "register";
	}

	/**
	 * Process customer registration
	 */
	@PostMapping("/register")
	public String processCustomerRegister(@Valid User user,
										  BindingResult result,
										  RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			return "register";
		}

		try {
			// Register as customer (reuse existing student registration for now)
			userService.registerNewStudent(user);
			redirectAttributes.addFlashAttribute("messageSuccess",
				"Registration successful! Please login.");
			return "redirect:/login";
		} catch (RuntimeException ex) {
			result.rejectValue("email", "duplicateEmail",
				"This email is already registered");
			return "register";
		}
	}

//	@PostMapping("/login")
//	public ResponseEntity<String> authenticateUser(@RequestBody LoginRequest loginRequest) {
	// 1. Create a token with the user's plain text credentials
//		Authentication authenticationToken = new UsernamePasswordAuthenticationToken(
//			loginRequest.getEmail(),
//			loginRequest.getPassword()
//		);

	// 2. Process authentication using the manager (which uses your UserDetailsService)
//		Authentication authentication = authenticationManager.authenticate(authenticationToken);

	// 3. Optional: Set the authenticated user in the security context (needed for session-based security)
	// Since your app is stateless, you would typically generate a JWT token here.
	// For testing, we'll confirm success.

	// If the line above didn't throw an exception, authentication succeeded.
//		return new ResponseEntity<>("User logged in successfully!", HttpStatus.OK);
//	}
}

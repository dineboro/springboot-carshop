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

import java.security.Principal;
import java.util.Optional;

@Controller
public class AuthController {
	private final UserService userService;
	private final SchoolRepository schoolRepository;
	private final AuthenticationManager authenticationManager;

	public AuthController(UserService userService, SchoolRepository schoolRepository, AuthenticationManager authenticationManager) {
		this.userService = userService;
		this.schoolRepository = schoolRepository;
		this.authenticationManager = authenticationManager;
	}

	// ========================================
	// STUDENT REGISTRATION (Keep for instructor's demo)
	// ========================================

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

		try {
			userService.registerNewStudent(user);
		} catch (RuntimeException ex) {
			result.rejectValue("email", "duplicateEmail", "This email is already registered");
			return "auth/registerForm";
		}

		// Auto-login
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

		// Redirect to school page
		String email = user.getEmail();
		Optional<School> school = findSchoolByRecursiveDomain(email);

		if(school.isPresent()) {
			redirectAttributes.addFlashAttribute("messageSuccess",
				"Your user account has been created. You have been redirected to " + school.get().getName() + "'s school page.");
			return "redirect:/schools/" + school.get().getDomain().substring(0, school.get().getDomain().length() - 4);
		} else {
			redirectAttributes.addFlashAttribute("messageWarning",
				"Your user account has been created, but we could not find a school matching your email domain");
			return "redirect:/schools";
		}
	}

	// ========================================
	// LOGIN & LOGIN SUCCESS
	// ========================================

	@GetMapping("/login")
	public String showLoginPage() {
		return "auth/login";
	}

	@GetMapping("/login-success")
	public String processLoginSuccess(Principal principal, RedirectAttributes redirectAttributes) {
		String email = principal.getName();
		Optional<School> school = findSchoolByRecursiveDomain(email);

		if(school.isPresent()) {
			redirectAttributes.addFlashAttribute("messageSuccess",
				"Welcome back! You have been redirected to " + school.get().getName() + "'s school page.");
			return "redirect:/schools/" + school.get().getDomain().substring(0, school.get().getDomain().length() - 4);
		} else {
			redirectAttributes.addFlashAttribute("messageWarning",
				"Welcome back! We could not find a school matching your email domain");
			return "redirect:/schools";
		}
	}

	// ========================================
	// MANAGER REGISTRATION (NEW)
	// ========================================

	/**
	 * Show manager registration page
	 */
	@GetMapping("/register")
	public String showRegisterPage(Model model) {
		model.addAttribute("user", new User());
		return "auth/register";
	}

	/**
	 * Process manager self-registration
	 * Manager registers → is_approved = FALSE → waits for admin approval
	 */
	@PostMapping("/register")
	public String processManagerRegister(@Valid User user,
										 BindingResult result,
										 RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			return "auth/register";
		}

		try {
			// Register as MANAGER (not approved yet)
			userService.registerNewManager(user);

			redirectAttributes.addFlashAttribute("messageSuccess",
				"Registration successful! Your account is pending approval. You will be notified once approved.");
			return "redirect:/login";

		} catch (RuntimeException ex) {
			result.rejectValue("email", "duplicateEmail",
				"This email is already registered");
			return "auth/register";
		}
	}

	// ========================================
	// HELPER METHODS
	// ========================================

	private Optional<School> findSchoolByRecursiveDomain(String email) {
		String domain = email.substring(email.indexOf("@") + 1);

		while (domain.contains(".")) {
			Optional<School> school = schoolRepository.findByDomain(domain);
			if (school.isPresent()) {
				return school;
			}

			int dotIndex = domain.indexOf(".");
			domain = domain.substring(dotIndex + 1);
		}

		return Optional.empty();
	}
}

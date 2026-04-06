package org.springframework.samples.petclinic.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.samples.petclinic.school.School;
import org.springframework.samples.petclinic.school.SchoolRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

/**
 * Handles user registration and the login landing page.
 *
 * Spring Security itself processes the POST /login form submission — we only need a GET
 * /login here to render the form.
 *
 * Routes: GET /register → show staff registration form POST /register → process staff
 * registration GET /login → show login form GET /login-success → post-login redirect
 * logic GET /register-student → show student registration form (instructor demo) POST
 * /register-student → process student registration (instructor demo)
 */
@Controller
public class AuthController {

	private final UserService userService;

	private final SchoolRepository schoolRepository;

	private final AuthenticationManager authenticationManager;

	public AuthController(UserService userService, SchoolRepository schoolRepository,
			AuthenticationManager authenticationManager) {
		this.userService = userService;
		this.schoolRepository = schoolRepository;
		this.authenticationManager = authenticationManager;
	}

	// =========================================================================
	// STAFF REGISTRATION
	// =========================================================================

	/**
	 * GET /register Renders the staff registration form. Adds an empty User object so
	 * Thymeleaf's th:object="${user}" binding works.
	 */
	@GetMapping("/register")
	public String showRegisterPage(Model model) {
		model.addAttribute("user", new User());
		return "auth/register";
	}

	/**
	 * POST /register Processes the staff registration form.
	 *
	 * Flow: 1. Run Bean Validation (@Valid) — if errors exist, re-render the form with
	 * messages. 2. Check for duplicate email — add a field-level error if found. 3. Save
	 * the user via UserService (handles password hashing, sets isApproved=false). 4.
	 * Redirect to /login with a success flash message.
	 *
	 * We do NOT auto-login staff after registration because they require admin approval
	 * before their account becomes usable.
	 */
	@PostMapping("/register")
	public String processUserRegister(@Valid User user, BindingResult result, RedirectAttributes redirectAttributes) {

		// Step 1 — return to form if Bean Validation failed (email, password rules, etc.)
		if (result.hasErrors()) {
			return "auth/register";
		}

		try {
			// Step 2 & 3 — UserServiceImpl.registerNewUser() hashes the password,
			// sets isActive=true, isApproved=false, and saves.
			userService.registerNewUser(user);

			// Step 4 — success: redirect to login with a flash message
			redirectAttributes.addFlashAttribute("messageSuccess",
					"Registration successful! Your account is pending admin approval. "
							+ "You will be able to log in once an administrator approves your account.");

			return "redirect:/login";

		}
		catch (RuntimeException ex) {
			// Duplicate email — rejectValue adds a field-level error so the
			// inputField fragment can highlight it with the is-invalid class.
			result.rejectValue("email", "duplicateEmail",
					"This email address is already registered. Please use a different email or log in.");
			return "auth/register";
		}
	}

	// =========================================================================
	// LOGIN
	// =========================================================================

	/**
	 * GET /login Renders the login form.
	 *
	 * Spring Security handles the actual POST /login authentication automatically based
	 * on the formLogin() config in SecurityConfig — we never write a POST /login method
	 * ourselves.
	 *
	 * Query parameters set by Spring Security / our failureHandler: ?error → invalid
	 * credentials ?disabled → account pending approval (DisabledException) ?logout → user
	 * just logged out (set by logoutSuccessUrl in SecurityConfig)
	 *
	 * We also check the session for LAST_EMAIL — SecurityConfig's failureHandler stores
	 * the submitted email there so we can pre-fill the field after a failed login
	 * attempt, improving UX.
	 */
	@GetMapping("/login")
	public String showLoginPage(Model model, HttpSession session) {
		User user = new User();

		// Pre-fill email if the user just had a failed login attempt
		String lastEmail = (String) session.getAttribute("LAST_EMAIL");
		if (lastEmail != null) {
			user.setEmail(lastEmail);
			session.removeAttribute("LAST_EMAIL"); // consume it — only show once
		}

		model.addAttribute("user", user);
		return "auth/login";
	}

	// =========================================================================
	// LOGIN SUCCESS (post-authentication redirect logic)
	// =========================================================================

	/**
	 * GET /login-success Called by Spring Security's defaultSuccessUrl after a successful
	 * login.
	 *
	 * Tries to find a school matching the user's email domain. If found → redirect to
	 * that school's page with a welcome message. If not → redirect to /schools with a
	 * warning.
	 *
	 * Note: The flash message here is intentionally slightly different from the
	 * registration success message (A-grade requirement).
	 */
	@GetMapping("/login-success")
	public String processLoginSuccess(Principal principal, RedirectAttributes redirectAttributes) {
		String email = principal.getName();
		Optional<School> school = findSchoolByRecursiveDomain(email);

		if (school.isPresent()) {
			redirectAttributes.addFlashAttribute("messageSuccess",
					"Welcome back! You have been redirected to " + school.get().getName() + "'s school page.");
			return "redirect:/schools/" + school.get().getDomain().substring(0, school.get().getDomain().length() - 4);
		}
		else {
			redirectAttributes.addFlashAttribute("messageWarning",
					"Welcome back! We could not find a school matching your email domain.");
			return "redirect:/schools";
		}
	}

	// =========================================================================
	// STUDENT REGISTRATION (Marc's demo)
	// =========================================================================

	/**
	 * GET /register-student Shows the student registration form (instructor's demo
	 * project).
	 */
	@GetMapping("/register-student")
	public String initRegisterForm(Model model) {
		model.addAttribute("user", new User());
		return "auth/registerForm";
	}

	/**
	 * POST /register-student Processes student registration with auto-login.
	 *
	 * Students are auto-approved (no admin review needed) and are immediately
	 * authenticated after saving so they land on the correct school page.
	 */
	@PostMapping("/register-student")
	public String processRegisterForm(@Valid User user, BindingResult result, RedirectAttributes redirectAttributes,
			HttpServletRequest request) {
		if (result.hasErrors()) {
			return "auth/registerForm";
		}

		// Save raw password before hashing so we can authenticate with it
		String rawPassword = user.getPassword();

		try {
			userService.registerNewStudent(user);
		}
		catch (RuntimeException ex) {
			result.rejectValue("email", "duplicateEmail", "This email is already registered");
			return "auth/registerForm";
		}

		// Auto-login the student immediately after registration
		try {
			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user.getEmail(),
					rawPassword);
			Authentication authentication = authenticationManager.authenticate(authToken);
			SecurityContextHolder.getContext().setAuthentication(authentication);

			HttpSession session = request.getSession(true);
			session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
					SecurityContextHolder.getContext());
		}
		catch (Exception e) {
			redirectAttributes.addFlashAttribute("messageDanger",
					"Account created, but auto-login failed. Please log in manually.");
			return "redirect:/login";
		}

		// Redirect to school page matching the student's email domain
		String email = user.getEmail();
		Optional<School> school = findSchoolByRecursiveDomain(email);

		if (school.isPresent()) {
			redirectAttributes.addFlashAttribute("messageSuccess", "Your student account has been created. "
					+ "You have been redirected to " + school.get().getName() + "'s school page.");
			return "redirect:/schools/" + school.get().getDomain().substring(0, school.get().getDomain().length() - 4);
		}
		else {
			redirectAttributes.addFlashAttribute("messageWarning",
					"Your student account has been created, but we could not find "
							+ "a school matching your email domain.");
			return "redirect:/schools";
		}
	}

	// =========================================================================
	// HELPER METHODS
	// =========================================================================

	/**
	 * Walks up the email domain looking for a matching school.
	 *
	 * Example: "bob@student.kirkwood.edu" → tries "student.kirkwood.edu" (not found) →
	 * tries "kirkwood.edu" (found!) → returns that school
	 */
	private Optional<School> findSchoolByRecursiveDomain(String email) {
		String domain = email.substring(email.indexOf("@") + 1);
		while (domain.contains(".")) {
			Optional<School> school = schoolRepository.findByDomain(domain);
			if (school.isPresent()) {
				return school;
			}
			domain = domain.substring(domain.indexOf(".") + 1);
		}
		return Optional.empty();
	}

}

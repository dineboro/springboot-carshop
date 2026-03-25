package org.springframework.samples.petclinic.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.samples.petclinic.school.School;
import org.springframework.samples.petclinic.school.SchoolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@WebMvcTest(AuthController.class)
@DisabledInNativeImage
@DisabledInAotMode
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SchoolRepository schoolRepository;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private AuthenticationManager authenticationManager;

	/*
	 * WHY THIS BEAN IS HERE:
	 * @WebMvcTest loads the full Spring Security filter chain. That chain calls
	 * UserDetailsService to validate sessions on every incoming request. Without
	 * this mock bean, every test throws NoSuchBeanDefinitionException at startup
	 * before a single controller method is reached. We don't configure any
	 * behaviour on it — just registering it is enough to satisfy the filter chain.
	 */
	@MockitoBean
	private UserDetailsServiceImpl userDetailsService;

	// =========================================================================
	// Marc's test
	// =========================================================================

	@Test
	void testProcessRegister_WithSubdomainRedirect() throws Exception {
		// Mock: School exists for "kirkwood.edu"
		School kirkwood = new School();
		kirkwood.setId(1);
		kirkwood.setName("Kirkwood");
		kirkwood.setDomain("kirkwood.edu");

		// Repository only knows "kirkwood.edu"
		given(schoolRepository.findByDomain("kirkwood.edu")).willReturn(Optional.of(kirkwood));
		// Repository does NOT know "student.kirkwood.edu"
		given(schoolRepository.findByDomain("student.kirkwood.edu")).willReturn(Optional.empty());

		given(userService.registerNewStudent(any(User.class))).willReturn(new User());

		// MOCK THE LOGIN - When the controller asks to authenticate, return a dummy
		// "Success" token
		given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
			.willReturn(new TestingAuthenticationToken("user", "password", "STUDENT"));

		// User registers with SUBDOMAIN
		mockMvc.perform(post("/register-student").with(csrf())
				.param("email", "alex@student.kirkwood.edu") // <--- Subdomain input
				.param("password", "StrongPass1!"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/schools/kirkwood")); // Should still find ID 1
	}

	@Test
	void testInitLoginFormLoadsCorrectly() throws Exception {
		mockMvc.perform(get("/login"))
			.andExpect(status().isOk())
			.andExpect(view().name("auth/login"))
			.andExpect(model().attributeExists("user"));
	}

	@Test
	void testInitLoginFormRemembersFailedEmail() throws Exception {
		// Simulate a request where the session contains a failed login attempt
		mockMvc.perform(get("/login").sessionAttr("LAST_EMAIL", "wrong@kirkwood.edu"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("user"))
			// Verify the HTML output actually contains the email in the value attribute
			.andExpect(content().string(containsString("wrong@kirkwood.edu")));
	}

	@Test
	void testLoginSuccessRedirectsToSchool() throws Exception {
		// 1. Setup a fake school for the mock repository to return
		School mockSchool = new School();
		mockSchool.setName("Kirkwood Community College");
		mockSchool.setDomain("kirkwood.edu");

		given(schoolRepository.findByDomain(anyString())).willReturn(Optional.of(mockSchool));

		// 2. Create a simple fake Principal
		Principal mockPrincipal = () -> "student@kirkwood.edu";

		// 3. Perform the GET request, passing the principal directly
		mockMvc.perform(get("/login-success").principal(mockPrincipal))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/schools/kirkwood"))
			.andExpect(flash().attributeExists("messageSuccess"));
	}

	@Test
	void testLoginSuccessRedirectsToSchoolsListIfNotFound() throws Exception {
		given(schoolRepository.findByDomain(anyString())).willReturn(Optional.empty());

		// Create a fake Principal with an unknown domain
		Principal mockPrincipal = () -> "student@unknown.com";

		mockMvc.perform(get("/login-success").principal(mockPrincipal))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/schools"))
			.andExpect(flash().attributeExists("messageWarning"));
	}

	// =========================================================================
	// GET /register
	// =========================================================================

	/*
	 * Verifies the controller returns the correct template name and adds an
	 * empty User to the model. Without the model attribute, Thymeleaf's
	 * th:object="${user}" would throw a NullPointerException at render time.
	 */
	@Test
	void testShowRegisterPage_ReturnsFormWithEmptyUser() throws Exception {
		mockMvc.perform(get("/register"))
			.andExpect(status().isOk())
			.andExpect(view().name("auth/register"))
			.andExpect(model().attributeExists("user"));
	}

	// =========================================================================
	// POST /register — valid input
	// =========================================================================

	/*
	 * Happy path: all fields pass Bean Validation, service saves successfully.
	 * Controller should redirect to /login with a flash success message.
	 *
	 * WHY csrf(): Spring Security requires a CSRF token on every POST request.
	 * Without it the filter returns 403 before the controller is ever reached.
	 */
	@Test
	void testProcessUserRegister_WithValidData_RedirectsToLogin() throws Exception {
		given(userService.registerNewUser(any(User.class))).willReturn(new User());

		mockMvc.perform(post("/register").with(csrf())
				.param("firstName", "Jane")
				.param("lastName",  "Doe")
				.param("email",     "jane@carrepair.com")
				.param("password",  "SecurePass1"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/login"))
			.andExpect(flash().attributeExists("messageSuccess"));
	}

	// =========================================================================
	// POST /register — blank email  (custom error messages)
	// =========================================================================

	/*
	 * Blank email violates @NotEmpty on User.java.
	 * BindingResult captures the violation, the controller re-renders the form
	 * (status 200, not a redirect), and the model reports a field error on "email".
	 * The service must NOT be called — no point persisting invalid data.
	 */
	@Test
	void testProcessUserRegister_WithBlankEmail_ReturnsFormWithError() throws Exception {
		mockMvc.perform(post("/register").with(csrf())
				.param("firstName", "Jane")
				.param("lastName",  "Doe")
				.param("email",     "")            // violates @NotEmpty
				.param("password",  "SecurePass1"))
			.andExpect(status().isOk())
			.andExpect(view().name("auth/register"))
			.andExpect(model().attributeHasFieldErrors("user", "email"));
	}

	// =========================================================================
	// POST /register — weak password  (custom error messages)
	// =========================================================================

	/*
	 * "abc" violates both @Size(min=8) and @Pattern (needs uppercase + number).
	 * Controller must stay on the form and report a password field error.
	 */
	@Test
	void testProcessUserRegister_WithWeakPassword_ReturnsFormWithError() throws Exception {
		mockMvc.perform(post("/register").with(csrf())
				.param("firstName", "Jane")
				.param("lastName",  "Doe")
				.param("email",     "jane@carrepair.com")
				.param("password",  "abc"))         // violates @Size and @Pattern
			.andExpect(status().isOk())
			.andExpect(view().name("auth/register"))
			.andExpect(model().attributeHasFieldErrors("user", "password"));
	}

	// =========================================================================
	// POST /register — duplicate email  (custom error messages)
	// =========================================================================

	/*
	 * The service throws RuntimeException on a duplicate email.
	 * The controller catches it, calls result.rejectValue("email", ...) to add
	 * a field-level error, then re-renders the form — not a redirect.
	 */
	@Test
	void testProcessUserRegister_WithDuplicateEmail_ReturnsFormWithEmailError() throws Exception {
		given(userService.registerNewUser(any(User.class)))
			.willThrow(new RuntimeException("Duplicate email"));

		mockMvc.perform(post("/register").with(csrf())
				.param("firstName", "Jane")
				.param("lastName",  "Doe")
				.param("email",     "existing@carrepair.com")
				.param("password",  "SecurePass1"))
			.andExpect(status().isOk())
			.andExpect(view().name("auth/register"))
			.andExpect(model().attributeHasFieldErrors("user", "email"));
	}

	// =========================================================================
	// GET /login — ?logout param  (requirement)
	// =========================================================================

	/*
	 * After clicking Logout, SecurityConfig's logoutSuccessUrl redirects to
	 * /login?logout. The controller renders auth/login as normal — the template
	 * checks th:if="${param.logout}" to show the "logged out" confirmation message.
	 */
	@Test
	void testLoginPage_WithLogoutParam_RendersLoginView() throws Exception {
		mockMvc.perform(get("/login").param("logout", ""))
			.andExpect(status().isOk())
			.andExpect(view().name("auth/login"))
			.andExpect(model().attributeExists("user"));
	}

	// =========================================================================
	// GET /login — ?disabled param
	// =========================================================================

	/*
	 * SecurityConfig's failureHandler redirects to /login?disabled when
	 * UserDetailsServiceImpl throws DisabledException (account pending approval).
	 * The template checks th:if="${param.disabled}" to show the warning message.
	 */
	@Test
	void testLoginPage_WithDisabledParam_RendersLoginView() throws Exception {
		mockMvc.perform(get("/login").param("disabled", ""))
			.andExpect(status().isOk())
			.andExpect(view().name("auth/login"))
			.andExpect(model().attributeExists("user"));
	}

}

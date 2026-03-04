
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

		// MOCK THE LOGIN-When the controller asks to authenticate, return a dummy "Success" token
//		given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
//			.willReturn(new TestingAuthenticationToken("user", "password", "STUDENT"));

		// User registers with SUBDOMAIN
		mockMvc.perform(post("/register")
				.with(csrf())
				.param("email", "alex@student.kirkwood.edu") // <--- Subdomain input
				.param("password", "StrongPass1!"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/schools/kirkwood")); // Should still find ID 1
	}


	@Test
	void testInitLoginFormLoadsCorrectly() throws Exception {
		mockMvc.perform(get("/login"))
			.andExpect(status().isOk())
			.andExpect(view().name("auth/loginForm"))
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
}

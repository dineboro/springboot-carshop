
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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
}

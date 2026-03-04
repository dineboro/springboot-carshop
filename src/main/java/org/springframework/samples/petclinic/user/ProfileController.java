package org.springframework.samples.petclinic.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/users")
public class ProfileController {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public ProfileController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@ModelAttribute("languageOptions")
	public Map<String, String> populateLanguageOptions() {
		Map<String, String> options = new LinkedHashMap<>();
		options.put("en", "English");
		options.put("ko", "Korean");
		options.put("es", "Spanish");
		return options;
	}

	@GetMapping("/profile")
	public String showProfileForm(Model model, Principal principal) {
		String email = principal.getName();
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new RuntimeException("User not found"));
		user.setPassword("");
		model.addAttribute("user", user);
		return "users/profile";
	}

}

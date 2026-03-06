package org.springframework.samples.petclinic.system;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

	/**
	 * Access denied error page (403)
	 * Shown when user tries to access admin pages without ADMIN role
	 */
	@GetMapping("/access-denied")
	public String accessDenied() {
		return "accessDenied";
	}
}

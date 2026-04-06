package org.springframework.samples.petclinic.user;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;

@ControllerAdvice
public class GravatarAdvice {

	@ModelAttribute("navGravatarUrl")
	public String navGravatarUrl(Principal principal) {
		if (principal == null) {
			return null;
		}
		try {
			String email = principal.getName();
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] hash = md.digest(email.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : hash) {
				sb.append(String.format("%02x", b));
			}
			return "https://www.gravatar.com/avatar/" + sb + "?s=32&d=identicon";
		}
		catch (NoSuchAlgorithmException e) {
			return "https://www.gravatar.com/avatar/?d=identicon&s=32";
		}
	}

}

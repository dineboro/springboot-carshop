package org.springframework.samples.petclinic.user;

import org.springframework.samples.petclinic.customer.Customer;
import org.springframework.samples.petclinic.customer.CustomerRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Optional;

@ControllerAdvice
public class GravatarAdvice {

	private final UserRepository userRepository;

	private final CustomerRepository customerRepository;

	public GravatarAdvice(UserRepository userRepository, CustomerRepository customerRepository) {
		this.userRepository = userRepository;
		this.customerRepository = customerRepository;
	}

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

	@ModelAttribute("linkedCustomerId")
	public Integer linkedCustomerId(Principal principal) {
		if (principal == null) {
			return null;
		}
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			return null;
		}
		boolean isCustomer = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));
		if (!isCustomer) {
			return null;
		}
		// Try to find linked customer by userId first, then fall back to email
		Optional<User> user = userRepository.findByEmail(principal.getName());
		if (user.isPresent()) {
			Optional<Customer> byUserId = customerRepository.findByUserId(user.get().getId());
			if (byUserId.isPresent()) {
				return byUserId.get().getCustomerId();
			}
			// Fallback: match by email
			Optional<Customer> byEmail = customerRepository.findByEmail(principal.getName());
			return byEmail.map(Customer::getCustomerId).orElse(null);
		}
		return null;
	}

}
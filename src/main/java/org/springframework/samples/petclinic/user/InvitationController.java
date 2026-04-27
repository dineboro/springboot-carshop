package org.springframework.samples.petclinic.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.samples.petclinic.customer.Customer;
import org.springframework.samples.petclinic.customer.CustomerRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/invite")
public class InvitationController {

	private final InvitationTokenRepository tokenRepository;

	private final CustomerRepository customerRepository;

	private final UserService userService;

	private final AuthenticationManager authenticationManager;

	public InvitationController(InvitationTokenRepository tokenRepository, CustomerRepository customerRepository,
			UserService userService, AuthenticationManager authenticationManager) {
		this.tokenRepository = tokenRepository;
		this.customerRepository = customerRepository;
		this.userService = userService;
		this.authenticationManager = authenticationManager;
	}

	@GetMapping("/accept")
	public String showAcceptForm(@RequestParam String token, Model model, RedirectAttributes redirectAttributes) {
		Optional<InvitationToken> optToken = tokenRepository.findByToken(token);

		if (optToken.isEmpty()) {
			redirectAttributes.addFlashAttribute("messageDanger", "Invalid invitation link.");
			return "redirect:/login";
		}

		InvitationToken invite = optToken.get();

		if (Boolean.TRUE.equals(invite.getIsUsed())) {
			redirectAttributes.addFlashAttribute("messageDanger",
					"This invitation link has already been used. Please log in.");
			return "redirect:/login";
		}

		if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
			redirectAttributes.addFlashAttribute("messageDanger",
					"This invitation link has expired. Please contact the shop to request a new one.");
			return "redirect:/login";
		}

		Optional<Customer> optCustomer = customerRepository.findById(invite.getCustomerId());
		if (optCustomer.isEmpty()) {
			redirectAttributes.addFlashAttribute("messageDanger", "Customer record not found.");
			return "redirect:/login";
		}

		Customer customer = optCustomer.get();
		model.addAttribute("token", token);
		model.addAttribute("email", invite.getEmail());
		model.addAttribute("phone", customer.getPhone());
		model.addAttribute("customerName", customer.getCustomerName());
		return "auth/acceptInvitation";
	}

	@PostMapping("/accept")
	public String processAcceptForm(@RequestParam String token, @RequestParam String password,
			@RequestParam String confirmPassword, RedirectAttributes redirectAttributes, HttpServletRequest request) {

		if (!password.equals(confirmPassword)) {
			redirectAttributes.addFlashAttribute("messageDanger", "Passwords do not match.");
			return "redirect:/invite/accept?token=" + token;
		}

		if (password.length() < 8) {
			redirectAttributes.addFlashAttribute("messageDanger", "Password must be at least 8 characters.");
			return "redirect:/invite/accept?token=" + token;
		}

		Optional<InvitationToken> optToken = tokenRepository.findByToken(token);
		if (optToken.isEmpty() || Boolean.TRUE.equals(optToken.get().getIsUsed())
				|| optToken.get().getExpiresAt().isBefore(LocalDateTime.now())) {
			redirectAttributes.addFlashAttribute("messageDanger", "Invalid or expired invitation link.");
			return "redirect:/login";
		}

		InvitationToken invite = optToken.get();
		Optional<Customer> optCustomer = customerRepository.findById(invite.getCustomerId());
		if (optCustomer.isEmpty()) {
			redirectAttributes.addFlashAttribute("messageDanger", "Customer record not found.");
			return "redirect:/login";
		}

		Customer customer = optCustomer.get();

		User user = new User();
		user.setEmail(invite.getEmail());
		user.setPhone(customer.getPhone());
		user.setFirstName(customer.getCustomerName());
		user.setLastName("");
		user.setPassword(password);

		User savedUser = userService.registerNewCustomer(user);

		customer.setUserId(savedUser.getId());
		customerRepository.save(customer);

		invite.setIsUsed(true);
		invite.setUsedAt(LocalDateTime.now());
		tokenRepository.save(invite);

		try {
			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(invite.getEmail(),
					password);
			Authentication authentication = authenticationManager.authenticate(authToken);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			HttpSession session = request.getSession(true);
			session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
					SecurityContextHolder.getContext());
		}
		catch (Exception e) {
			redirectAttributes.addFlashAttribute("messageSuccess",
					"Account created! Please log in with your email or phone number.");
			return "redirect:/login";
		}

		redirectAttributes.addFlashAttribute("messageSuccess", "Welcome, " + customer.getCustomerName() + "!");
		return "redirect:/customers/" + customer.getCustomerId();
	}

}

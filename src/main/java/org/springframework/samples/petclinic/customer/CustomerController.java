package org.springframework.samples.petclinic.customer;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.appointment.ServiceAppointmentRepository;
import org.springframework.samples.petclinic.user.EmailService;
import org.springframework.samples.petclinic.user.InvitationToken;
import org.springframework.samples.petclinic.user.InvitationTokenRepository;
import org.springframework.samples.petclinic.user.User;
import org.springframework.samples.petclinic.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CustomerController {

	private final CustomerRepository customerRepository;

	private final ServiceAppointmentRepository appointmentRepository;

	private final InvitationTokenRepository invitationTokenRepository;

	private final EmailService emailService;

	private final UserRepository userRepository;

	@Value("${app.base-url}")
	private String baseUrl;

	public CustomerController(CustomerRepository customerRepository, ServiceAppointmentRepository appointmentRepository,
			InvitationTokenRepository invitationTokenRepository, EmailService emailService,
			UserRepository userRepository) {
		this.customerRepository = customerRepository;
		this.appointmentRepository = appointmentRepository;
		this.invitationTokenRepository = invitationTokenRepository;
		this.emailService = emailService;
		this.userRepository = userRepository;
	}

	@GetMapping("/customers/new")
	public String initCreationForm(Map<String, Customer> model) {
		Customer customer = new Customer();
		model.put("customer", customer);
		return "customers/createOrUpdateCustomerForm";
	}

	@PostMapping("/customers/new")
	public String processCreationForm(@Valid Customer customer, BindingResult result,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			return "customers/createOrUpdateCustomerForm";
		}
		customerRepository.save(customer);

		if (customer.getEmail() != null && !customer.getEmail().isBlank()) {
			try {
				Integer invitedBy = null;
				Authentication auth = SecurityContextHolder.getContext().getAuthentication();
				if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
					Optional<User> currentUser = userRepository.findByEmail(auth.getName());
					invitedBy = currentUser.map(User::getId).orElse(null);
				}

				InvitationToken token = new InvitationToken();
				token.setCustomerId(customer.getCustomerId());
				token.setEmail(customer.getEmail());
				token.setToken(UUID.randomUUID().toString());
				token.setInvitedBy(invitedBy);
				token.setExpiresAt(LocalDateTime.now().plusHours(48));
				invitationTokenRepository.save(token);

				String inviteLink = baseUrl + "/invite/accept?token=" + token.getToken();
				emailService.sendInvitation(customer.getEmail(), customer.getCustomerName(), inviteLink);

				redirectAttributes.addFlashAttribute("messageSuccess",
						"Customer saved! Invitation email sent to " + customer.getEmail());
			}
			catch (Exception e) {
				redirectAttributes.addFlashAttribute("messageWarning",
						"Customer saved, but the invitation email could not be sent: " + e.getMessage());
			}
		}
		else {
			redirectAttributes.addFlashAttribute("messageSuccess", "Customer saved successfully.");
		}

		return "redirect:/customers";
	}

	@GetMapping("/customers")
	public String showCustomerList(@RequestParam(defaultValue = "1") int page, Model model) {
		Pageable pageable = PageRequest.of(page - 1, 5);
		Page<Customer> customerPage = customerRepository.findAll(pageable);

		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", customerPage.getTotalPages());
		model.addAttribute("totalItems", customerPage.getTotalElements());
		model.addAttribute("listCustomers", customerPage.getContent());

		return "customers/customerList";
	}

	@GetMapping("/customers/{customerId:\\d+}")
	public ModelAndView showCustomer(@PathVariable("customerId") int customerId) {
		ModelAndView mav = new ModelAndView("customers/customerDetails");
		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
					"Customer with id " + customerId + " not found."));
		mav.addObject(customer);
		mav.addObject("appointments", appointmentRepository.findByCustomerId(customerId));
		return mav;
	}

	@GetMapping("/customers/find")
	public String initFindForm(Map<String, Customer> model) {
		Customer customer = new Customer();
		model.put("customer", customer);
		return "customers/findCustomers";
	}

	@GetMapping("/customers/search")
	public String processFindForm(@RequestParam(defaultValue = "1") int page,
			@RequestParam(required = false) String customerName, Model model) {
		Pageable pageable = PageRequest.of(page - 1, 5);
		Page<Customer> customerPage;

		if (customerName == null || customerName.isEmpty()) {
			customerPage = customerRepository.findAll(pageable);
		}
		else {
			customerPage = customerRepository.findByNameContaining(customerName, pageable);
		}

		if (customerPage.isEmpty()) {
			model.addAttribute("notFound", true);
			model.addAttribute("customerName", customerName);
			return "customers/findCustomers";
		}

		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", customerPage.getTotalPages());
		model.addAttribute("totalItems", customerPage.getTotalElements());
		model.addAttribute("listCustomers", customerPage.getContent());

		return "customers/customerList";
	}

	@GetMapping("/customers/{customerId}/edit")
	public String initUpdateForm(@PathVariable("customerId") int customerId, Model model) {
		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
					"Customer with id " + customerId + " not found."));
		model.addAttribute("customer", customer);
		return "customers/createOrUpdateCustomerForm";
	}

	@PostMapping("/customers/{customerId}/edit")
	public String processUpdateForm(@Valid Customer customer, BindingResult result,
			@PathVariable("customerId") int customerId) {
		if (result.hasErrors()) {
			return "customers/createOrUpdateCustomerForm";
		}
		customer.setCustomerId(customerId);
		customerRepository.save(customer);
		return "redirect:/customers/{customerId}";
	}

}

package org.springframework.samples.petclinic.customer;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.samples.petclinic.appointment.ServiceAppointmentRepository;

import java.util.Map;

@Controller
public class CustomerController {

	private final CustomerRepository customerRepository;

	private final ServiceAppointmentRepository appointmentRepository;

	public CustomerController(CustomerRepository customerRepository,
			ServiceAppointmentRepository appointmentRepository) {
		this.customerRepository = customerRepository;
		this.appointmentRepository = appointmentRepository;
	}

	@GetMapping("/customers/new")
	public String initCreationForm(Map<String, Customer> model) {
		// Instantiate a default object
		Customer customer = new Customer();
		// Add customer to input model so Thymeleaf can bind data to it
		model.put("customer", customer);
		return "customers/createOrUpdateCustomerForm";
	}

	@PostMapping("/customers/new")
	public String processCreationForm(@Valid Customer customer, BindingResult result) {
		if (result.hasErrors()) {
			return "customers/createOrUpdateCustomerForm";
		}
		customerRepository.save(customer);
		return "redirect:/customers";
	}

	@GetMapping("/customers")
	public String showCustomerList(@RequestParam(defaultValue = "1") int page, Model model) {
		// Pagination setup (5 items per page)
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
		// If no search term provided, show all customers
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

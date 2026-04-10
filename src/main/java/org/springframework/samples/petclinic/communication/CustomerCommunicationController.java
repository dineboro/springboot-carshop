package org.springframework.samples.petclinic.communication;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.samples.petclinic.customer.CustomerRepository;
import org.springframework.samples.petclinic.appointment.ServiceAppointmentRepository;
import org.springframework.samples.petclinic.user.UserRepository;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/communications")
public class CustomerCommunicationController {

	private final CustomerCommunicationRepository communicationRepository;

	private final CustomerRepository customerRepository;

	private final ServiceAppointmentRepository appointmentRepository;

	private final UserRepository userRepository;

	public CustomerCommunicationController(CustomerCommunicationRepository communicationRepository,
			CustomerRepository customerRepository, ServiceAppointmentRepository appointmentRepository,
			UserRepository userRepository) {
		this.communicationRepository = communicationRepository;
		this.customerRepository = customerRepository;
		this.appointmentRepository = appointmentRepository;
		this.userRepository = userRepository;
	}

	@GetMapping
	public String showCommunicationList(@RequestParam(defaultValue = "1") int page, Model model) {
		Pageable pageable = PageRequest.of(page - 1, 15);
		Page<CustomerCommunication> commPage = communicationRepository.findAll(pageable);

		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", commPage.getTotalPages());
		model.addAttribute("totalItems", commPage.getTotalElements());
		model.addAttribute("listCommunications", commPage.getContent());
		model.addAttribute("pendingFollowUps", communicationRepository.findPendingFollowUps());

		return "communications/communicationList";
	}

	@GetMapping("/new")
	public String initCreationForm(@RequestParam(required = false) Integer customerId,
			@RequestParam(required = false) Integer appointmentId, Model model,
			@AuthenticationPrincipal UserDetails userDetails) {
		CustomerCommunication comm = new CustomerCommunication();
		comm.setCommunicationDate(LocalDateTime.now());
		comm.setDirection(CustomerCommunication.Direction.Outbound);
		if (customerId != null)
			comm.setCustomerId(customerId);
		if (appointmentId != null)
			comm.setAppointmentId(appointmentId);
		if (userDetails != null) {
			userRepository.findByEmail(userDetails.getUsername()).ifPresent(u -> comm.setContactedBy(u.getId()));
		}
		populateFormModel(model);
		model.addAttribute("communication", comm);
		return "communications/createOrUpdateCommunicationForm";
	}

	@PostMapping("/new")
	public String processCreationForm(@Valid @ModelAttribute("communication") CustomerCommunication comm,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			@AuthenticationPrincipal UserDetails userDetails) {
		if (result.hasErrors()) {
			populateFormModel(model);
			return "communications/createOrUpdateCommunicationForm";
		}
		if (comm.getCommunicationDate() == null)
			comm.setCommunicationDate(LocalDateTime.now());
		if (userDetails != null && comm.getContactedBy() == null) {
			userRepository.findByEmail(userDetails.getUsername()).ifPresent(u -> comm.setContactedBy(u.getId()));
		}
		communicationRepository.save(comm);
		redirectAttributes.addFlashAttribute("messageSuccess", "Communication logged successfully.");
		return "redirect:/communications";
	}

	@GetMapping("/{id:\\d+}")
	public String showCommunication(@PathVariable int id, Model model) {
		CustomerCommunication comm = communicationRepository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Communication not found"));
		model.addAttribute("communication", comm);
		return "communications/communicationDetails";
	}

	@GetMapping("/{id:\\d+}/edit")
	public String initUpdateForm(@PathVariable int id, Model model) {
		CustomerCommunication comm = communicationRepository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Communication not found"));
		populateFormModel(model);
		model.addAttribute("communication", comm);
		return "communications/createOrUpdateCommunicationForm";
	}

	@PostMapping("/{id:\\d+}/edit")
	public String processUpdateForm(@PathVariable int id,
			@Valid @ModelAttribute("communication") CustomerCommunication comm, BindingResult result, Model model,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			populateFormModel(model);
			return "communications/createOrUpdateCommunicationForm";
		}
		comm.setCommunicationId(id);
		communicationRepository.save(comm);
		redirectAttributes.addFlashAttribute("messageSuccess", "Communication updated.");
		return "redirect:/communications/" + id;
	}

	@PostMapping("/{id:\\d+}/delete")
	public String deleteCommunication(@PathVariable int id, RedirectAttributes redirectAttributes) {
		CustomerCommunication comm = communicationRepository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Communication not found"));
		communicationRepository.delete(comm);
		redirectAttributes.addFlashAttribute("messageSuccess", "Communication log deleted.");
		return "redirect:/communications";
	}

	private void populateFormModel(Model model) {
		model.addAttribute("commTypes", CustomerCommunication.CommunicationType.values());
		model.addAttribute("directions", CustomerCommunication.Direction.values());
		model.addAttribute("customers", customerRepository.findAll());
		model.addAttribute("appointments", appointmentRepository.findAll());
	}

}

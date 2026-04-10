package org.springframework.samples.petclinic.appointment;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.samples.petclinic.customer.Customer;
import org.springframework.samples.petclinic.customer.CustomerRepository;
import org.springframework.samples.petclinic.serviceline.ServiceLineRepository;
import org.springframework.samples.petclinic.reminder.AppointmentReminderRepository;

import java.util.List;

@Controller
public class ServiceAppointmentController {

	private final ServiceAppointmentRepository appointmentRepository;

	private final CustomerRepository customerRepository;

	private final ServiceLineRepository serviceLineRepository;

	private final AppointmentReminderRepository reminderRepository;

	public ServiceAppointmentController(ServiceAppointmentRepository appointmentRepository,
			CustomerRepository customerRepository, ServiceLineRepository serviceLineRepository,
			AppointmentReminderRepository reminderRepository) {
		this.appointmentRepository = appointmentRepository;
		this.customerRepository = customerRepository;
		this.serviceLineRepository = serviceLineRepository;
		this.reminderRepository = reminderRepository;
	}

	@GetMapping("/appointments")
	public String showAppointmentList(@RequestParam(defaultValue = "1") int page, Model model) {
		Pageable pageable = PageRequest.of(page - 1, 10);
		Page<ServiceAppointment> appointmentPage = appointmentRepository.findAll(pageable);

		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", appointmentPage.getTotalPages());
		model.addAttribute("totalItems", appointmentPage.getTotalElements());
		model.addAttribute("listAppointments", appointmentPage.getContent());

		return "appointments/appointmentList";
	}

	@GetMapping("/appointments/new")
	public String initCreationForm(@RequestParam Integer customerId, Model model) {
		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

		ServiceAppointment appointment = new ServiceAppointment();
		appointment.setCustomerId(customerId);

		model.addAttribute("appointment", appointment);
		model.addAttribute("customer", customer);
		model.addAttribute("vehicles", customer.getVehicles());
		model.addAttribute("statuses", ServiceAppointment.AppointmentStatus.values());

		return "appointments/createOrUpdateAppointmentForm";
	}

	@PostMapping("/appointments/new")
	public String processCreationForm(@Valid @ModelAttribute("appointment") ServiceAppointment appointment,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			Customer customer = customerRepository.findById(appointment.getCustomerId()).orElse(null);
			model.addAttribute("customer", customer);
			model.addAttribute("vehicles", customer != null ? customer.getVehicles() : List.of());
			model.addAttribute("statuses", ServiceAppointment.AppointmentStatus.values());
			return "appointments/createOrUpdateAppointmentForm";
		}

		appointmentRepository.save(appointment);
		redirectAttributes.addFlashAttribute("messageSuccess", "Appointment scheduled successfully.");
		return "redirect:/customers/" + appointment.getCustomerId();
	}

	@GetMapping("/appointments/{appointmentId:\\d+}")
	public String showAppointment(@PathVariable int appointmentId, Model model) {
		ServiceAppointment appointment = appointmentRepository.findByIdWithDetails(appointmentId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
		Customer customer = customerRepository.findById(appointment.getCustomerId()).orElse(null);
		model.addAttribute("appointment", appointment);
		model.addAttribute("customer", customer);
		model.addAttribute("serviceLines", serviceLineRepository.findByAppointmentId(appointmentId));
		model.addAttribute("reminders", reminderRepository.findByAppointmentId(appointmentId));
		return "appointments/appointmentDetails";
	}

	@GetMapping("/appointments/{appointmentId:\\d+}/edit")
	public String initUpdateForm(@PathVariable int appointmentId, Model model) {
		ServiceAppointment appointment = appointmentRepository.findById(appointmentId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

		Customer customer = customerRepository.findById(appointment.getCustomerId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

		model.addAttribute("appointment", appointment);
		model.addAttribute("customer", customer);
		model.addAttribute("vehicles", customer.getVehicles());
		model.addAttribute("statuses", ServiceAppointment.AppointmentStatus.values());

		return "appointments/createOrUpdateAppointmentForm";
	}

	@PostMapping("/appointments/{appointmentId:\\d+}/edit")
	public String processUpdateForm(@Valid @ModelAttribute("appointment") ServiceAppointment appointment,
			BindingResult result, @PathVariable int appointmentId, Model model, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			Customer customer = customerRepository.findById(appointment.getCustomerId()).orElse(null);
			model.addAttribute("customer", customer);
			model.addAttribute("vehicles", customer != null ? customer.getVehicles() : List.of());
			model.addAttribute("statuses", ServiceAppointment.AppointmentStatus.values());
			return "appointments/createOrUpdateAppointmentForm";
		}

		appointment.setAppointmentId(appointmentId);
		appointmentRepository.save(appointment);
		redirectAttributes.addFlashAttribute("messageSuccess", "Appointment updated successfully.");
		return "redirect:/appointments/" + appointmentId;
	}

	@GetMapping("/appointments/{appointmentId:\\d+}/status")
	public String initStatusUpdateForm(@PathVariable int appointmentId, Model model) {
		ServiceAppointment appointment = appointmentRepository.findByIdWithDetails(appointmentId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
		model.addAttribute("appointment", appointment);
		model.addAttribute("statuses", ServiceAppointment.AppointmentStatus.values());
		return "appointments/updateStatusForm";
	}

	@PostMapping("/appointments/{appointmentId:\\d+}/status")
	public String processStatusUpdate(@PathVariable int appointmentId,
			@RequestParam ServiceAppointment.AppointmentStatus status, @RequestParam(required = false) String notes,
			RedirectAttributes redirectAttributes) {
		ServiceAppointment appointment = appointmentRepository.findById(appointmentId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
		appointment.setStatus(status);
		appointment.setNotes(notes);
		appointmentRepository.save(appointment);
		redirectAttributes.addFlashAttribute("messageSuccess", "Appointment status updated successfully.");
		return "redirect:/appointments/" + appointmentId;
	}

}

package org.springframework.samples.petclinic.reminder;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.samples.petclinic.appointment.ServiceAppointmentRepository;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/appointments/{appointmentId:\\d+}/reminders")
public class AppointmentReminderController {

	private final AppointmentReminderRepository reminderRepository;

	private final ServiceAppointmentRepository appointmentRepository;

	public AppointmentReminderController(AppointmentReminderRepository reminderRepository,
			ServiceAppointmentRepository appointmentRepository) {
		this.reminderRepository = reminderRepository;
		this.appointmentRepository = appointmentRepository;
	}

	@GetMapping("/new")
	public String initCreationForm(@PathVariable int appointmentId, Model model) {
		appointmentRepository.findById(appointmentId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
		AppointmentReminder reminder = new AppointmentReminder();
		reminder.setAppointmentId(appointmentId);

		// Default: 24 hours before appointment
		appointmentRepository.findById(appointmentId)
			.ifPresent(appt -> reminder.setScheduledDate(appt.getAppointmentDate().minusHours(24)));

		model.addAttribute("reminder", reminder);
		model.addAttribute("appointmentId", appointmentId);
		model.addAttribute("reminderTypes", AppointmentReminder.ReminderType.values());
		model.addAttribute("statuses", AppointmentReminder.ReminderStatus.values());
		return "reminders/createOrUpdateReminderForm";
	}

	@PostMapping("/new")
	public String processCreationForm(@PathVariable int appointmentId,
			@Valid @ModelAttribute("reminder") AppointmentReminder reminder, BindingResult result, Model model,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			model.addAttribute("appointmentId", appointmentId);
			model.addAttribute("reminderTypes", AppointmentReminder.ReminderType.values());
			model.addAttribute("statuses", AppointmentReminder.ReminderStatus.values());
			return "reminders/createOrUpdateReminderForm";
		}
		reminder.setAppointmentId(appointmentId);
		reminderRepository.save(reminder);
		redirectAttributes.addFlashAttribute("messageSuccess", "Reminder scheduled.");
		return "redirect:/appointments/" + appointmentId;
	}

	@PostMapping("/{reminderId:\\d+}/cancel")
	public String cancelReminder(@PathVariable int appointmentId, @PathVariable int reminderId,
			RedirectAttributes redirectAttributes) {
		AppointmentReminder reminder = reminderRepository.findById(reminderId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reminder not found"));
		reminder.setStatus(AppointmentReminder.ReminderStatus.Cancelled);
		reminderRepository.save(reminder);
		redirectAttributes.addFlashAttribute("messageSuccess", "Reminder cancelled.");
		return "redirect:/appointments/" + appointmentId;
	}

	@PostMapping("/{reminderId:\\d+}/mark-sent")
	public String markSent(@PathVariable int appointmentId, @PathVariable int reminderId,
			RedirectAttributes redirectAttributes) {
		AppointmentReminder reminder = reminderRepository.findById(reminderId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reminder not found"));
		reminder.setStatus(AppointmentReminder.ReminderStatus.Sent);
		reminder.setSentDate(LocalDateTime.now());
		reminderRepository.save(reminder);
		redirectAttributes.addFlashAttribute("messageSuccess", "Reminder marked as sent.");
		return "redirect:/appointments/" + appointmentId;
	}

	@PostMapping("/{reminderId:\\d+}/delete")
	public String deleteReminder(@PathVariable int appointmentId, @PathVariable int reminderId,
			RedirectAttributes redirectAttributes) {
		AppointmentReminder reminder = reminderRepository.findById(reminderId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reminder not found"));
		reminderRepository.delete(reminder);
		redirectAttributes.addFlashAttribute("messageSuccess", "Reminder deleted.");
		return "redirect:/appointments/" + appointmentId;
	}

}

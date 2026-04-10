package org.springframework.samples.petclinic.servicehistory;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.samples.petclinic.appointment.ServiceAppointmentRepository;
import org.springframework.samples.petclinic.customer.VehicleRepository;
import org.springframework.samples.petclinic.employee.EmployeeRepository;
import org.springframework.samples.petclinic.employee.Employee;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/service-history")
public class VehicleServiceHistoryController {

	private final VehicleServiceHistoryRepository historyRepository;

	private final VehicleRepository vehicleRepository;

	private final ServiceAppointmentRepository appointmentRepository;

	private final EmployeeRepository employeeRepository;

	public VehicleServiceHistoryController(VehicleServiceHistoryRepository historyRepository,
			VehicleRepository vehicleRepository, ServiceAppointmentRepository appointmentRepository,
			EmployeeRepository employeeRepository) {
		this.historyRepository = historyRepository;
		this.vehicleRepository = vehicleRepository;
		this.appointmentRepository = appointmentRepository;
		this.employeeRepository = employeeRepository;
	}

	@GetMapping("/vehicle/{vin}")
	public String showHistoryForVehicle(@PathVariable String vin, Model model) {
		vehicleRepository.findById(vin)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
		model.addAttribute("vin", vin);
		model.addAttribute("vehicle", vehicleRepository.findById(vin).orElse(null));
		model.addAttribute("historyList", historyRepository.findByVinOrderByServiceDateDesc(vin));
		return "service-history/vehicleHistory";
	}

	@GetMapping("/new")
	public String initCreationForm(@RequestParam(required = false) Integer appointmentId,
			@RequestParam(required = false) String vin, Model model) {
		VehicleServiceHistory history = new VehicleServiceHistory();
		history.setServiceDate(LocalDateTime.now());
		if (appointmentId != null)
			history.setAppointmentId(appointmentId);
		if (vin != null)
			history.setVin(vin);
		populateFormModel(model);
		model.addAttribute("history", history);
		return "service-history/createOrUpdateHistoryForm";
	}

	@PostMapping("/new")
	public String processCreationForm(@Valid @ModelAttribute("history") VehicleServiceHistory history,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			populateFormModel(model);
			return "service-history/createOrUpdateHistoryForm";
		}
		historyRepository.save(history);
		redirectAttributes.addFlashAttribute("messageSuccess", "Service history record added.");
		return "redirect:/service-history/vehicle/" + history.getVin();
	}

	@GetMapping("/{id:\\d+}/edit")
	public String initUpdateForm(@PathVariable int id, Model model) {
		VehicleServiceHistory history = historyRepository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "History record not found"));
		populateFormModel(model);
		model.addAttribute("history", history);
		return "service-history/createOrUpdateHistoryForm";
	}

	@PostMapping("/{id:\\d+}/edit")
	public String processUpdateForm(@PathVariable int id,
			@Valid @ModelAttribute("history") VehicleServiceHistory history, BindingResult result, Model model,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			populateFormModel(model);
			return "service-history/createOrUpdateHistoryForm";
		}
		history.setServiceHistoryId(id);
		historyRepository.save(history);
		redirectAttributes.addFlashAttribute("messageSuccess", "History record updated.");
		return "redirect:/service-history/vehicle/" + history.getVin();
	}

	@PostMapping("/{id:\\d+}/delete")
	public String deleteHistory(@PathVariable int id, RedirectAttributes redirectAttributes) {
		VehicleServiceHistory history = historyRepository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "History record not found"));
		String vin = history.getVin();
		historyRepository.delete(history);
		redirectAttributes.addFlashAttribute("messageSuccess", "History record deleted.");
		return "redirect:/service-history/vehicle/" + vin;
	}

	private void populateFormModel(Model model) {
		model.addAttribute("vehicles", vehicleRepository.findAll());
		model.addAttribute("appointments", appointmentRepository.findAll());
		model.addAttribute("technicians",
				employeeRepository.findAll()
					.stream()
					.filter(e -> e.getStatus() == Employee.EmployeeStatus.ACTIVE
							&& e.getRole() == Employee.EmployeeRole.Technician)
					.toList());
	}

}

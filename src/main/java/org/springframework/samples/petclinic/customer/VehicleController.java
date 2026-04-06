package org.springframework.samples.petclinic.customer;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class VehicleController {

	private final VehicleRepository vehicleRepository;

	private final CustomerRepository customerRepository;

	public VehicleController(VehicleRepository vehicleRepository, CustomerRepository customerRepository) {
		this.vehicleRepository = vehicleRepository;
		this.customerRepository = customerRepository;
	}

	@GetMapping("/vehicles/new")
	public String initCreationForm(@RequestParam Integer customerId, Model model) {
		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

		Vehicle vehicle = new Vehicle();
		vehicle.setCustomer(customer);

		model.addAttribute("vehicle", vehicle);
		model.addAttribute("customer", customer);
		model.addAttribute("statuses", Vehicle.VehicleStatus.values());

		return "vehicles/createOrUpdateVehicleForm";
	}

	@PostMapping("/vehicles/new")
	public String processCreationForm(@Valid @ModelAttribute("vehicle") Vehicle vehicle, BindingResult result,
			@RequestParam Integer customerId, Model model, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			Customer customer = customerRepository.findById(customerId).orElse(null);
			model.addAttribute("customer", customer);
			model.addAttribute("statuses", Vehicle.VehicleStatus.values());
			return "vehicles/createOrUpdateVehicleForm";
		}

		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
		vehicle.setCustomer(customer);
		vehicleRepository.save(vehicle);

		redirectAttributes.addFlashAttribute("messageSuccess", "Vehicle added successfully.");
		return "redirect:/customers/" + customerId;
	}

	@GetMapping("/vehicles/{vin}/edit")
	public String initUpdateForm(@PathVariable String vin, Model model) {
		Vehicle vehicle = vehicleRepository.findById(vin)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));

		model.addAttribute("vehicle", vehicle);
		model.addAttribute("customer", vehicle.getCustomer());
		model.addAttribute("statuses", Vehicle.VehicleStatus.values());

		return "vehicles/createOrUpdateVehicleForm";
	}

	@PostMapping("/vehicles/{vin}/edit")
	public String processUpdateForm(@Valid @ModelAttribute("vehicle") Vehicle vehicle, BindingResult result,
			@PathVariable String vin, Model model, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			Vehicle existing = vehicleRepository.findById(vin).orElse(null);
			model.addAttribute("customer", existing != null ? existing.getCustomer() : null);
			model.addAttribute("statuses", Vehicle.VehicleStatus.values());
			return "vehicles/createOrUpdateVehicleForm";
		}

		Vehicle existing = vehicleRepository.findById(vin)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
		vehicle.setVin(vin);
		vehicle.setCustomer(existing.getCustomer());
		vehicleRepository.save(vehicle);

		redirectAttributes.addFlashAttribute("messageSuccess", "Vehicle updated successfully.");
		return "redirect:/customers/" + existing.getCustomer().getCustomerId();
	}

	@PostMapping("/vehicles/{vin}/delete")
	public String deleteVehicle(@PathVariable String vin, RedirectAttributes redirectAttributes) {
		Vehicle vehicle = vehicleRepository.findById(vin)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
		Integer customerId = vehicle.getCustomer().getCustomerId();
		vehicleRepository.delete(vehicle);

		redirectAttributes.addFlashAttribute("messageSuccess", "Vehicle removed successfully.");
		return "redirect:/customers/" + customerId;
	}

}

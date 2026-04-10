package org.springframework.samples.petclinic.serviceline;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.samples.petclinic.appointment.ServiceAppointment;
import org.springframework.samples.petclinic.appointment.ServiceAppointmentRepository;
import org.springframework.samples.petclinic.customer.VehicleRepository;
import org.springframework.samples.petclinic.employee.Employee;
import org.springframework.samples.petclinic.employee.EmployeeRepository;
import org.springframework.samples.petclinic.servicecatalog.ServiceCatalog;
import org.springframework.samples.petclinic.servicecatalog.ServiceCatalogRepository;

import java.time.LocalDateTime;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/appointments/{appointmentId:\\d+}/service-lines")
public class ServiceLineController {

	private final ServiceLineRepository serviceLineRepository;

	private final ServiceLineItemRepository serviceLineItemRepository;

	private final ServiceAppointmentRepository appointmentRepository;

	private final EmployeeRepository employeeRepository;

	private final ServiceCatalogRepository catalogRepository;

	private final VehicleRepository vehicleRepository;

	public ServiceLineController(ServiceLineRepository serviceLineRepository,
			ServiceLineItemRepository serviceLineItemRepository, ServiceAppointmentRepository appointmentRepository,
			EmployeeRepository employeeRepository, ServiceCatalogRepository catalogRepository,
			VehicleRepository vehicleRepository) {
		this.serviceLineRepository = serviceLineRepository;
		this.serviceLineItemRepository = serviceLineItemRepository;
		this.appointmentRepository = appointmentRepository;
		this.employeeRepository = employeeRepository;
		this.catalogRepository = catalogRepository;
		this.vehicleRepository = vehicleRepository;
	}

	private ServiceAppointment getAppointmentOrThrow(int appointmentId) {
		return appointmentRepository.findById(appointmentId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
	}

	@GetMapping("/new")
	public String initCreationForm(@PathVariable int appointmentId, Model model) {
		ServiceAppointment appt = getAppointmentOrThrow(appointmentId);
		ServiceLine serviceLine = new ServiceLine();
		serviceLine.setAppointmentId(appointmentId);
		serviceLine.setVin(appt.getVin());
		serviceLine.setServiceDate(LocalDateTime.now());
		populateFormModel(model, appointmentId);
		model.addAttribute("serviceLine", serviceLine);
		return "service-lines/createOrUpdateServiceLineForm";
	}

	@PostMapping("/new")
	public String processCreationForm(@PathVariable int appointmentId,
			@Valid @ModelAttribute("serviceLine") ServiceLine serviceLine, BindingResult result, Model model,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			populateFormModel(model, appointmentId);
			return "service-lines/createOrUpdateServiceLineForm";
		}
		serviceLine.setAppointmentId(appointmentId);
		recalculate(serviceLine);
		serviceLineRepository.save(serviceLine);
		redirectAttributes.addFlashAttribute("messageSuccess", "Work order added.");
		return "redirect:/appointments/" + appointmentId;
	}

	@GetMapping("/{serviceLineId:\\d+}/edit")
	public String initUpdateForm(@PathVariable int appointmentId, @PathVariable int serviceLineId, Model model) {
		ServiceLine serviceLine = serviceLineRepository.findById(serviceLineId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service line not found"));
		populateFormModel(model, appointmentId);
		model.addAttribute("serviceLine", serviceLine);
		return "service-lines/createOrUpdateServiceLineForm";
	}

	@PostMapping("/{serviceLineId:\\d+}/edit")
	public String processUpdateForm(@PathVariable int appointmentId, @PathVariable int serviceLineId,
			@Valid @ModelAttribute("serviceLine") ServiceLine serviceLine, BindingResult result, Model model,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			populateFormModel(model, appointmentId);
			return "service-lines/createOrUpdateServiceLineForm";
		}
		serviceLine.setServiceLineId(serviceLineId);
		serviceLine.setAppointmentId(appointmentId);
		recalculate(serviceLine);
		serviceLineRepository.save(serviceLine);
		redirectAttributes.addFlashAttribute("messageSuccess", "Work order updated.");
		return "redirect:/appointments/" + appointmentId;
	}

	@PostMapping("/{serviceLineId:\\d+}/delete")
	public String deleteServiceLine(@PathVariable int appointmentId, @PathVariable int serviceLineId,
			RedirectAttributes redirectAttributes) {
		ServiceLine serviceLine = serviceLineRepository.findById(serviceLineId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service line not found"));
		serviceLineRepository.delete(serviceLine);
		redirectAttributes.addFlashAttribute("messageSuccess", "Service line removed.");
		return "redirect:/appointments/" + appointmentId;
	}

	// --- Line Items ---

	@GetMapping("/{serviceLineId:\\d+}/items/new")
	public String initItemCreationForm(@PathVariable int appointmentId, @PathVariable int serviceLineId, Model model) {
		serviceLineRepository.findById(serviceLineId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service line not found"));
		ServiceLineItem item = new ServiceLineItem();
		item.setServiceLineId(serviceLineId);
		item.setQuantity(BigDecimal.ONE);
		model.addAttribute("lineItem", item);
		model.addAttribute("appointmentId", appointmentId);
		model.addAttribute("serviceLineId", serviceLineId);
		model.addAttribute("itemTypes", ServiceLineItem.ItemType.values());
		model.addAttribute("catalogItems", catalogRepository.findByActive(true));
		return "service-lines/createOrUpdateLineItemForm";
	}

	@PostMapping("/{serviceLineId:\\d+}/items/new")
	public String processItemCreationForm(@PathVariable int appointmentId, @PathVariable int serviceLineId,
			@Valid @ModelAttribute("lineItem") ServiceLineItem item, BindingResult result, Model model,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			model.addAttribute("appointmentId", appointmentId);
			model.addAttribute("serviceLineId", serviceLineId);
			model.addAttribute("itemTypes", ServiceLineItem.ItemType.values());
			model.addAttribute("catalogItems", catalogRepository.findByActive(true));
			return "service-lines/createOrUpdateLineItemForm";
		}
		item.setServiceLineId(serviceLineId);
		if (item.getTotalPrice() == null) {
			item.setTotalPrice(item.getUnitPrice().multiply(item.getQuantity()));
		}
		serviceLineItemRepository.save(item);
		redirectAttributes.addFlashAttribute("messageSuccess", "Line item added.");
		return "redirect:/appointments/" + appointmentId;
	}

	@PostMapping("/{serviceLineId:\\d+}/items/{itemId:\\d+}/delete")
	public String deleteLineItem(@PathVariable int appointmentId, @PathVariable int serviceLineId,
			@PathVariable int itemId, RedirectAttributes redirectAttributes) {
		ServiceLineItem item = serviceLineItemRepository.findById(itemId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Line item not found"));
		serviceLineItemRepository.delete(item);
		redirectAttributes.addFlashAttribute("messageSuccess", "Line item removed.");
		return "redirect:/appointments/" + appointmentId;
	}

	private void populateFormModel(Model model, int appointmentId) {
		model.addAttribute("appointmentId", appointmentId);
		model.addAttribute("statuses", ServiceLine.ServiceLineStatus.values());
		model.addAttribute("technicians",
				employeeRepository.findAll()
					.stream()
					.filter(e -> e.getStatus() == Employee.EmployeeStatus.ACTIVE)
					.toList());
		model.addAttribute("catalogItems", catalogRepository.findByActive(true));
		model.addAttribute("vehicles", vehicleRepository.findAll());
	}

	private void recalculate(ServiceLine sl) {
		java.math.BigDecimal labor = sl.getLaborHours() != null && sl.getLaborRate() != null
				? sl.getLaborHours().multiply(sl.getLaborRate()).setScale(2, java.math.RoundingMode.HALF_UP)
				: (sl.getLaborCost() != null ? sl.getLaborCost() : java.math.BigDecimal.ZERO);
		sl.setLaborCost(labor);
		java.math.BigDecimal parts = sl.getPartsCost() != null ? sl.getPartsCost() : java.math.BigDecimal.ZERO;
		sl.setTotalCost(labor.add(parts));
	}

}

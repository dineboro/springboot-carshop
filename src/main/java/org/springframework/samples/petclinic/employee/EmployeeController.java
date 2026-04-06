package org.springframework.samples.petclinic.employee;

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

@Controller
@RequestMapping("/employees")
public class EmployeeController {

	private final EmployeeRepository employeeRepository;

	public EmployeeController(EmployeeRepository employeeRepository) {
		this.employeeRepository = employeeRepository;
	}

	@GetMapping
	public String showEmployeeList(@RequestParam(defaultValue = "1") int page, Model model) {
		Pageable pageable = PageRequest.of(page - 1, 10);
		Page<Employee> employeePage = employeeRepository.findAll(pageable);

		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", employeePage.getTotalPages());
		model.addAttribute("totalItems", employeePage.getTotalElements());
		model.addAttribute("listEmployees", employeePage.getContent());

		return "employees/employeeList";
	}

	@GetMapping("/new")
	public String initCreationForm(Model model) {
		model.addAttribute("employee", new Employee());
		model.addAttribute("roles", Employee.EmployeeRole.values());
		model.addAttribute("statuses", Employee.EmployeeStatus.values());
		return "employees/createOrUpdateEmployeeForm";
	}

	@PostMapping("/new")
	public String processCreationForm(@Valid @ModelAttribute("employee") Employee employee, BindingResult result,
			Model model, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			model.addAttribute("roles", Employee.EmployeeRole.values());
			model.addAttribute("statuses", Employee.EmployeeStatus.values());
			return "employees/createOrUpdateEmployeeForm";
		}
		employeeRepository.save(employee);
		redirectAttributes.addFlashAttribute("messageSuccess", employee.getFullName() + " has been added to the team.");
		return "redirect:/employees";
	}

	@GetMapping("/{employeeId:\\d+}")
	public String showEmployee(@PathVariable int employeeId, Model model) {
		Employee employee = employeeRepository.findById(employeeId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
		model.addAttribute("employee", employee);
		return "employees/employeeDetails";
	}

	@GetMapping("/{employeeId:\\d+}/edit")
	public String initUpdateForm(@PathVariable int employeeId, Model model) {
		Employee employee = employeeRepository.findById(employeeId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
		model.addAttribute("employee", employee);
		model.addAttribute("roles", Employee.EmployeeRole.values());
		model.addAttribute("statuses", Employee.EmployeeStatus.values());
		return "employees/createOrUpdateEmployeeForm";
	}

	@PostMapping("/{employeeId:\\d+}/edit")
	public String processUpdateForm(@Valid @ModelAttribute("employee") Employee employee, BindingResult result,
			@PathVariable int employeeId, Model model, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			model.addAttribute("roles", Employee.EmployeeRole.values());
			model.addAttribute("statuses", Employee.EmployeeStatus.values());
			return "employees/createOrUpdateEmployeeForm";
		}
		employee.setEmployeeId(employeeId);
		employeeRepository.save(employee);
		redirectAttributes.addFlashAttribute("messageSuccess", "Employee updated successfully.");
		return "redirect:/employees/" + employeeId;
	}

	@PostMapping("/{employeeId:\\d+}/delete")
	public String deleteEmployee(@PathVariable int employeeId, RedirectAttributes redirectAttributes) {
		Employee employee = employeeRepository.findById(employeeId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
		employeeRepository.delete(employee);
		redirectAttributes.addFlashAttribute("messageSuccess", employee.getFullName() + " has been removed.");
		return "redirect:/employees";
	}

}

package org.springframework.samples.petclinic.servicecatalog;

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
@RequestMapping("/service-catalog")
public class ServiceCatalogController {

	private final ServiceCatalogRepository serviceCatalogRepository;

	public ServiceCatalogController(ServiceCatalogRepository serviceCatalogRepository) {
		this.serviceCatalogRepository = serviceCatalogRepository;
	}

	@GetMapping
	public String showCatalogList(@RequestParam(defaultValue = "1") int page, Model model) {
		Pageable pageable = PageRequest.of(page - 1, 10);
		Page<ServiceCatalog> catalogPage = serviceCatalogRepository.findAll(pageable);

		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", catalogPage.getTotalPages());
		model.addAttribute("totalItems", catalogPage.getTotalElements());
		model.addAttribute("listServices", catalogPage.getContent());

		return "service-catalog/catalogList";
	}

	@GetMapping("/new")
	public String initCreationForm(Model model) {
		model.addAttribute("serviceCatalog", new ServiceCatalog());
		model.addAttribute("categories", ServiceCatalog.ServiceCategory.values());
		return "service-catalog/createOrUpdateCatalogForm";
	}

	@PostMapping("/new")
	public String processCreationForm(@Valid ServiceCatalog serviceCatalog,
			BindingResult result, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			return "service-catalog/createOrUpdateCatalogForm";
		}
		serviceCatalogRepository.save(serviceCatalog);
		redirectAttributes.addFlashAttribute("messageSuccess",
				"\"" + serviceCatalog.getName() + "\" added to the service catalog.");
		return "redirect:/service-catalog";
	}

	@GetMapping("/{catalogId:\\d+}")
	public String showCatalogItem(@PathVariable int catalogId, Model model) {
		ServiceCatalog item = serviceCatalogRepository.findById(catalogId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
		model.addAttribute("serviceCatalog", item);
		return "service-catalog/catalogDetails";
	}

	@GetMapping("/{catalogId:\\d+}/edit")
	public String initUpdateForm(@PathVariable int catalogId, Model model) {
		ServiceCatalog item = serviceCatalogRepository.findById(catalogId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
		model.addAttribute("serviceCatalog", item);
		model.addAttribute("categories", ServiceCatalog.ServiceCategory.values());
		return "service-catalog/createOrUpdateCatalogForm";
	}

	@PostMapping("/{catalogId:\\d+}/edit")
	public String processUpdateForm(@Valid @ModelAttribute("serviceCatalog") ServiceCatalog serviceCatalog,
			BindingResult result, @PathVariable int catalogId, Model model, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			model.addAttribute("categories", ServiceCatalog.ServiceCategory.values());
			return "service-catalog/createOrUpdateCatalogForm";
		}
		serviceCatalog.setCatalogId(catalogId);
		serviceCatalogRepository.save(serviceCatalog);
		redirectAttributes.addFlashAttribute("messageSuccess", "\"" + serviceCatalog.getName() + "\" updated successfully.");
		return "redirect:/service-catalog/" + catalogId;
	}

	@PostMapping("/{catalogId:\\d+}/delete")
	public String deleteCatalogItem(@PathVariable int catalogId, RedirectAttributes redirectAttributes) {
		ServiceCatalog item = serviceCatalogRepository.findById(catalogId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
		serviceCatalogRepository.delete(item);
		redirectAttributes.addFlashAttribute("messageSuccess", "\"" + item.getName() + "\" removed from the catalog.");
		return "redirect:/service-catalog";
	}

}

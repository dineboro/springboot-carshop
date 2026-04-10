package org.springframework.samples.petclinic.estimate;

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
import org.springframework.samples.petclinic.customer.VehicleRepository;
import org.springframework.samples.petclinic.servicecatalog.ServiceCatalog;
import org.springframework.samples.petclinic.servicecatalog.ServiceCatalogRepository;
import org.springframework.samples.petclinic.user.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/estimates")
public class EstimateController {

	private final EstimateRepository estimateRepository;

	private final EstimateLineItemRepository lineItemRepository;

	private final CustomerRepository customerRepository;

	private final VehicleRepository vehicleRepository;

	private final UserRepository userRepository;

	private final ServiceCatalogRepository catalogRepository;

	public EstimateController(EstimateRepository estimateRepository, EstimateLineItemRepository lineItemRepository,
			CustomerRepository customerRepository, VehicleRepository vehicleRepository, UserRepository userRepository,
			ServiceCatalogRepository catalogRepository) {
		this.estimateRepository = estimateRepository;
		this.lineItemRepository = lineItemRepository;
		this.customerRepository = customerRepository;
		this.vehicleRepository = vehicleRepository;
		this.userRepository = userRepository;
		this.catalogRepository = catalogRepository;
	}

	@GetMapping
	public String showEstimateList(@RequestParam(defaultValue = "1") int page,
			@RequestParam(required = false) String search, Model model) {
		Pageable pageable = PageRequest.of(page - 1, 10);
		Page<Estimate> estimatePage = (search != null && !search.isBlank())
				? estimateRepository.searchByCustomer(search.trim(), pageable) : estimateRepository.findAll(pageable);

		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", estimatePage.getTotalPages());
		model.addAttribute("totalItems", estimatePage.getTotalElements());
		model.addAttribute("listEstimates", estimatePage.getContent());
		model.addAttribute("search", search);

		return "estimates/estimateList";
	}

	@GetMapping("/{estimateId:\\d+}")
	public String showEstimate(@PathVariable int estimateId, Model model) {
		Estimate estimate = estimateRepository.findById(estimateId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estimate not found"));
		model.addAttribute("estimate", estimate);
		model.addAttribute("lineItems", lineItemRepository.findByEstimateId(estimateId));
		model.addAttribute("itemTypes", EstimateLineItem.ItemType.values());
		model.addAttribute("newLineItem", buildNewLineItem(estimateId));
		model.addAttribute("catalogItems", catalogRepository.findByActive(true));
		return "estimates/estimateDetails";
	}

	@GetMapping("/new")
	public String initCreationForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
		Estimate estimate = new Estimate();
		estimate.setEstimateDate(LocalDateTime.now());
		estimate.setValidUntil(LocalDateTime.now().plusDays(30));

		Integer maxNum = estimateRepository.findMaxEstimateNumber();
		estimate.setEstimateNumber("EST-" + String.format("%05d", (maxNum == null ? 0 : maxNum) + 1));

		if (userDetails != null) {
			userRepository.findByEmail(userDetails.getUsername()).ifPresent(u -> estimate.setPreparedBy(u.getId()));
		}

		populateFormModel(model);
		model.addAttribute("estimate", estimate);
		return "estimates/createOrUpdateEstimateForm";
	}

	@PostMapping("/new")
	public String processCreationForm(@Valid @ModelAttribute("estimate") Estimate estimate, BindingResult result,
			Model model, RedirectAttributes redirectAttributes, @AuthenticationPrincipal UserDetails userDetails) {
		if (result.hasErrors()) {
			populateFormModel(model);
			return "estimates/createOrUpdateEstimateForm";
		}
		if (userDetails != null && estimate.getPreparedBy() == null) {
			userRepository.findByEmail(userDetails.getUsername()).ifPresent(u -> estimate.setPreparedBy(u.getId()));
		}
		recalculate(estimate);
		estimateRepository.save(estimate);
		redirectAttributes.addFlashAttribute("messageSuccess",
				"Estimate " + estimate.getEstimateNumber() + " created.");
		return "redirect:/estimates/" + estimate.getEstimateId();
	}

	@GetMapping("/{estimateId:\\d+}/edit")
	public String initUpdateForm(@PathVariable int estimateId, Model model) {
		Estimate estimate = estimateRepository.findById(estimateId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estimate not found"));
		populateFormModel(model);
		model.addAttribute("estimate", estimate);
		return "estimates/createOrUpdateEstimateForm";
	}

	@PostMapping("/{estimateId:\\d+}/edit")
	public String processUpdateForm(@PathVariable int estimateId, @Valid @ModelAttribute("estimate") Estimate estimate,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			populateFormModel(model);
			return "estimates/createOrUpdateEstimateForm";
		}
		estimate.setEstimateId(estimateId);
		recalculate(estimate);
		estimateRepository.save(estimate);
		redirectAttributes.addFlashAttribute("messageSuccess", "Estimate updated successfully.");
		return "redirect:/estimates/" + estimateId;
	}

	@PostMapping("/{estimateId:\\d+}/line-items/new")
	public String addLineItem(@PathVariable int estimateId, @Valid @ModelAttribute("newLineItem") EstimateLineItem item,
			BindingResult result, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("messageError", "Invalid line item data.");
			return "redirect:/estimates/" + estimateId;
		}
		item.setEstimateId(estimateId);
		if (item.getTotalPrice() == null) {
			item.setTotalPrice(item.getUnitPrice().multiply(item.getQuantity()));
		}
		lineItemRepository.save(item);

		// Recalculate estimate totals
		Estimate estimate = estimateRepository.findById(estimateId).orElseThrow();
		recalculateFromItems(estimate, lineItemRepository.findByEstimateId(estimateId));
		estimateRepository.save(estimate);

		redirectAttributes.addFlashAttribute("messageSuccess", "Line item added.");
		return "redirect:/estimates/" + estimateId;
	}

	@PostMapping("/{estimateId:\\d+}/line-items/{itemId:\\d+}/delete")
	public String deleteLineItem(@PathVariable int estimateId, @PathVariable int itemId,
			RedirectAttributes redirectAttributes) {
		EstimateLineItem item = lineItemRepository.findById(itemId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Line item not found"));
		lineItemRepository.delete(item);

		Estimate estimate = estimateRepository.findById(estimateId).orElseThrow();
		recalculateFromItems(estimate, lineItemRepository.findByEstimateId(estimateId));
		estimateRepository.save(estimate);

		redirectAttributes.addFlashAttribute("messageSuccess", "Line item removed.");
		return "redirect:/estimates/" + estimateId;
	}

	private void populateFormModel(Model model) {
		model.addAttribute("statuses", Estimate.EstimateStatus.values());
		model.addAttribute("customers", customerRepository.findAll());
		model.addAttribute("vehicles", vehicleRepository.findAll());
	}

	private EstimateLineItem buildNewLineItem(int estimateId) {
		EstimateLineItem item = new EstimateLineItem();
		item.setEstimateId(estimateId);
		item.setQuantity(BigDecimal.ONE);
		return item;
	}

	private void recalculate(Estimate estimate) {
		BigDecimal labor = estimate.getLaborCost() != null ? estimate.getLaborCost() : BigDecimal.ZERO;
		BigDecimal parts = estimate.getPartsCost() != null ? estimate.getPartsCost() : BigDecimal.ZERO;
		BigDecimal tax = estimate.getTaxAmount() != null ? estimate.getTaxAmount() : BigDecimal.ZERO;
		estimate.setTotalEstimate(labor.add(parts).add(tax));
	}

	private void recalculateFromItems(Estimate estimate, java.util.List<EstimateLineItem> items) {
		BigDecimal labor = BigDecimal.ZERO;
		BigDecimal parts = BigDecimal.ZERO;
		for (EstimateLineItem item : items) {
			if (item.getItemType() == EstimateLineItem.ItemType.Labor) {
				labor = labor.add(item.getTotalPrice());
			}
			else {
				parts = parts.add(item.getTotalPrice());
			}
		}
		estimate.setLaborCost(labor);
		estimate.setPartsCost(parts);
		BigDecimal subtotal = labor.add(parts);
		BigDecimal tax = subtotal.multiply(new BigDecimal("0.07")).setScale(2, java.math.RoundingMode.HALF_UP);
		estimate.setTaxAmount(tax);
		estimate.setTotalEstimate(subtotal.add(tax));
	}

}

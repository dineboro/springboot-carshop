package org.springframework.samples.petclinic.school;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Changed from ModelAndView to match the method signature
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Collection;
import java.util.Map;

@Controller // replace by ???
public class SubscriptionController {

	private final SubscriptionRepository subscriptionRepository; // replace by ???

	public SubscriptionController(SubscriptionRepository subscriptionRepository) {
		this.subscriptionRepository = subscriptionRepository;
	}

	@GetMapping("/pricing")
	public String showPricingTable(Model model) {
		Collection<Subscription> subscriptions = subscriptionRepository.findAll();
		model.addAttribute("subscriptions", subscriptions.stream().toList());
		return "schools/pricing";
	}

	@GetMapping("/subscriptions/new")
	public String initCreationForm(Map<String, Object> model) {
		Subscription subscription = new Subscription();
		model.put("subscription", subscription);
		return "schools/createOrUpdateSubscriptionForm";
	}

	@PostMapping("/subscriptions/new")
	public String processCreationForm(@Valid Subscription subscription, BindingResult result) {
		if (result.hasErrors()) {
			return "schools/createOrUpdateSubscriptionForm";
		}
		subscriptionRepository.save(subscription);
		return "redirect:/pricing";
	}

}

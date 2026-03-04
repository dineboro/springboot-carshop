package org.springframework.samples.petclinic.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

	private final UserRepository userRepository;

	public AdminController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/**
	 * Show pending user approvals
	 */
	@GetMapping("/pending-approvals")
	public String showPendingApprovals(Model model) {
		// Find all unapproved users
		List<User> pendingUsers = userRepository.findByIsApprovedFalse();
		model.addAttribute("pendingUsers", pendingUsers);
		return "admin/pendingApprovals";
	}

	/**
	 * Approve user
	 */
	@PostMapping("/approve/{id}")
	public String approveUser(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new RuntimeException("User not found"));

		user.setIsApproved(true);
		userRepository.save(user);

		// TODO: Send approval email to user

		redirectAttributes.addFlashAttribute("messageSuccess",
			"User " + user.getEmail() + " has been approved");
		return "redirect:/admin/pending-approvals";
	}

	/**
	 * Reject/Delete user
	 */
	@PostMapping("/reject/{id}")
	public String rejectUser(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new RuntimeException("User not found"));

		userRepository.delete(user);

		redirectAttributes.addFlashAttribute("messageSuccess",
			"User registration rejected and deleted");
		return "redirect:/admin/pending-approvals";
	}
}

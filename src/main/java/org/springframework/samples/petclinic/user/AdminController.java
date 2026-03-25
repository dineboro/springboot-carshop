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

	private final UserService userService;

	public AdminController(UserRepository userRepository, UserService userService) {
		this.userRepository = userRepository;
		this.userService = userService;
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
	 * Show approval form with role selection
	 */
	@GetMapping("/approve-form/{id}")
	public String showApprovalForm(@PathVariable Integer id, Model model) {
		User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

		model.addAttribute("user", user);
		return "admin/approveUserForm";
	}

	/**
	 * Approve user AND assign role
	 */
	@PostMapping("/approve/{id}")
	public String approveUserWithRole(@PathVariable Integer id, @RequestParam String roleName,
			RedirectAttributes redirectAttributes) {
		User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

		// Approve user
		user.setIsApproved(true);
		userRepository.save(user);

		// Assign role
		userService.assignRole(id, roleName);

		redirectAttributes.addFlashAttribute("messageSuccess",
				"User " + user.getEmail() + " has been approved as " + roleName);
		return "redirect:/admin/pending-approvals";
	}

	/**
	 * Quick approve with default role selection (alternative method)
	 */
	@PostMapping("/quick-approve/{id}")
	public String quickApproveUser(@PathVariable Integer id, @RequestParam String role,
			RedirectAttributes redirectAttributes) {
		User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

		// Approve user
		user.setIsApproved(true);
		userRepository.save(user);

		// Assign selected role
		userService.assignRole(id, role);

		redirectAttributes.addFlashAttribute("messageSuccess", "User " + user.getEmail() + " approved as " + role);

		return "redirect:/admin/pending-approvals";
	}

	/**
	 * Reject/Delete user
	 */
	@PostMapping("/reject/{id}")
	public String rejectUser(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
		User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

		userRepository.delete(user);

		redirectAttributes.addFlashAttribute("messageSuccess", "User registration rejected and deleted");
		return "redirect:/admin/pending-approvals";
	}

}

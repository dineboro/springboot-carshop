package org.springframework.samples.petclinic.invoice;

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
import org.springframework.samples.petclinic.appointment.ServiceAppointment;
import org.springframework.samples.petclinic.appointment.ServiceAppointmentRepository;
import org.springframework.samples.petclinic.user.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/invoices")
public class InvoiceController {

	private final InvoiceRepository invoiceRepository;

	private final PaymentRepository paymentRepository;

	private final ServiceAppointmentRepository appointmentRepository;

	private final UserRepository userRepository;

	public InvoiceController(InvoiceRepository invoiceRepository, PaymentRepository paymentRepository,
			ServiceAppointmentRepository appointmentRepository, UserRepository userRepository) {
		this.invoiceRepository = invoiceRepository;
		this.paymentRepository = paymentRepository;
		this.appointmentRepository = appointmentRepository;
		this.userRepository = userRepository;
	}

	@GetMapping
	public String showInvoiceList(@RequestParam(defaultValue = "1") int page,
			@RequestParam(required = false) String search, Model model) {
		Pageable pageable = PageRequest.of(page - 1, 10);
		Page<Invoice> invoicePage = (search != null && !search.isBlank())
				? invoiceRepository.searchByCustomer(search.trim(), pageable) : invoiceRepository.findAll(pageable);

		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", invoicePage.getTotalPages());
		model.addAttribute("totalItems", invoicePage.getTotalElements());
		model.addAttribute("listInvoices", invoicePage.getContent());
		model.addAttribute("search", search);

		return "invoices/invoiceList";
	}

	@GetMapping("/{invoiceId:\\d+}")
	public String showInvoice(@PathVariable int invoiceId, Model model) {
		Invoice invoice = invoiceRepository.findById(invoiceId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
		model.addAttribute("invoice", invoice);
		model.addAttribute("payments", paymentRepository.findByInvoiceId(invoiceId));
		model.addAttribute("paymentMethods", Payment.PaymentMethod.values());
		model.addAttribute("newPayment", new Payment());
		return "invoices/invoiceDetails";
	}

	@GetMapping("/new")
	public String initCreationForm(@RequestParam(required = false) Integer appointmentId, Model model,
			@AuthenticationPrincipal UserDetails userDetails) {
		Invoice invoice = new Invoice();
		invoice.setInvoiceDate(LocalDateTime.now());

		if (appointmentId != null) {
			ServiceAppointment appt = appointmentRepository.findById(appointmentId).orElse(null);
			if (appt != null) {
				invoice.setAppointmentId(appointmentId);
				invoice.setCustomerId(appt.getCustomerId());
				invoice.setVin(appt.getVin());
			}
		}

		// Auto-generate invoice number
		Integer maxNum = invoiceRepository.findMaxInvoiceNumber();
		invoice.setInvoiceNumber("INV-" + String.format("%05d", (maxNum == null ? 0 : maxNum) + 1));

		if (userDetails != null) {
			userRepository.findByEmail(userDetails.getUsername()).ifPresent(u -> invoice.setCreatedBy(u.getId()));
		}

		model.addAttribute("invoice", invoice);
		model.addAttribute("statuses", Invoice.InvoiceStatus.values());
		model.addAttribute("appointments", appointmentRepository.findAll());
		return "invoices/createOrUpdateInvoiceForm";
	}

	@PostMapping("/new")
	public String processCreationForm(@Valid @ModelAttribute("invoice") Invoice invoice, BindingResult result,
			Model model, RedirectAttributes redirectAttributes, @AuthenticationPrincipal UserDetails userDetails) {
		if (result.hasErrors()) {
			model.addAttribute("statuses", Invoice.InvoiceStatus.values());
			model.addAttribute("appointments", appointmentRepository.findAll());
			return "invoices/createOrUpdateInvoiceForm";
		}
		recalculate(invoice);
		if (userDetails != null && invoice.getCreatedBy() == null) {
			userRepository.findByEmail(userDetails.getUsername()).ifPresent(u -> invoice.setCreatedBy(u.getId()));
		}
		invoiceRepository.save(invoice);
		redirectAttributes.addFlashAttribute("messageSuccess", "Invoice " + invoice.getInvoiceNumber() + " created.");
		return "redirect:/invoices/" + invoice.getInvoiceId();
	}

	@GetMapping("/{invoiceId:\\d+}/edit")
	public String initUpdateForm(@PathVariable int invoiceId, Model model) {
		Invoice invoice = invoiceRepository.findById(invoiceId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
		model.addAttribute("invoice", invoice);
		model.addAttribute("statuses", Invoice.InvoiceStatus.values());
		model.addAttribute("appointments", appointmentRepository.findAll());
		return "invoices/createOrUpdateInvoiceForm";
	}

	@PostMapping("/{invoiceId:\\d+}/edit")
	public String processUpdateForm(@PathVariable int invoiceId, @Valid @ModelAttribute("invoice") Invoice invoice,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			model.addAttribute("statuses", Invoice.InvoiceStatus.values());
			model.addAttribute("appointments", appointmentRepository.findAll());
			return "invoices/createOrUpdateInvoiceForm";
		}
		invoice.setInvoiceId(invoiceId);
		recalculate(invoice);
		invoiceRepository.save(invoice);
		redirectAttributes.addFlashAttribute("messageSuccess", "Invoice updated successfully.");
		return "redirect:/invoices/" + invoiceId;
	}

	@PostMapping("/{invoiceId:\\d+}/payments/new")
	public String addPayment(@PathVariable int invoiceId, @Valid @ModelAttribute("newPayment") Payment payment,
			BindingResult result, RedirectAttributes redirectAttributes,
			@AuthenticationPrincipal UserDetails userDetails) {
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("messageError", "Invalid payment data.");
			return "redirect:/invoices/" + invoiceId;
		}
		payment.setInvoiceId(invoiceId);
		if (payment.getPaymentDate() == null)
			payment.setPaymentDate(LocalDateTime.now());
		if (userDetails != null && payment.getReceivedBy() == null) {
			userRepository.findByEmail(userDetails.getUsername()).ifPresent(u -> payment.setReceivedBy(u.getId()));
		}
		paymentRepository.save(payment);

		// Update invoice amount_paid and status
		Invoice invoice = invoiceRepository.findById(invoiceId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
		BigDecimal totalPaid = paymentRepository.findByInvoiceId(invoiceId)
			.stream()
			.map(Payment::getAmount)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
		invoice.setAmountPaid(totalPaid);
		if (totalPaid.compareTo(invoice.getTotalAmount()) >= 0) {
			invoice.setStatus(Invoice.InvoiceStatus.Paid);
		}
		else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
			invoice.setStatus(Invoice.InvoiceStatus.Partial);
		}
		invoiceRepository.save(invoice);

		redirectAttributes.addFlashAttribute("messageSuccess",
				"Payment of $" + String.format("%.2f", payment.getAmount()) + " recorded.");
		return "redirect:/invoices/" + invoiceId;
	}

	private void recalculate(Invoice invoice) {
		if (invoice.getSubtotal() == null)
			invoice.setSubtotal(BigDecimal.ZERO);
		if (invoice.getTaxRate() == null)
			invoice.setTaxRate(new BigDecimal("0.0700"));
		BigDecimal tax = invoice.getSubtotal().multiply(invoice.getTaxRate());
		invoice.setTaxAmount(tax.setScale(2, java.math.RoundingMode.HALF_UP));
		invoice.setTotalAmount(invoice.getSubtotal().add(invoice.getTaxAmount()));
	}

}

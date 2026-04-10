package org.springframework.samples.petclinic.invoice;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.samples.petclinic.appointment.ServiceAppointment;
import org.springframework.samples.petclinic.customer.Customer;
import org.springframework.samples.petclinic.customer.Vehicle;
import org.springframework.samples.petclinic.user.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "invoice")
@Getter
@Setter
public class Invoice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "invoice_id")
	private Integer invoiceId;

	@NotEmpty(message = "Invoice number is required")
	@Column(name = "invoice_number", unique = true)
	private String invoiceNumber;

	@NotNull(message = "Appointment is required")
	@Column(name = "appointment_id")
	private Integer appointmentId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "appointment_id", insertable = false, updatable = false)
	private ServiceAppointment appointment;

	@NotNull(message = "Customer is required")
	@Column(name = "customer_id")
	private Integer customerId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", insertable = false, updatable = false)
	private Customer customer;

	@NotEmpty(message = "VIN is required")
	@Column(name = "vin")
	private String vin;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vin", insertable = false, updatable = false)
	private Vehicle vehicle;

	@NotNull(message = "Invoice date is required")
	@Column(name = "invoice_date")
	private LocalDateTime invoiceDate;

	@Column(name = "due_date")
	private LocalDateTime dueDate;

	@Column(name = "subtotal", precision = 10, scale = 2)
	private BigDecimal subtotal = BigDecimal.ZERO;

	@Column(name = "tax_rate", precision = 5, scale = 4)
	private BigDecimal taxRate = new BigDecimal("0.0700");

	@Column(name = "tax_amount", precision = 10, scale = 2)
	private BigDecimal taxAmount = BigDecimal.ZERO;

	@Column(name = "total_amount", precision = 10, scale = 2)
	private BigDecimal totalAmount = BigDecimal.ZERO;

	@Column(name = "amount_paid", precision = 10, scale = 2)
	private BigDecimal amountPaid = BigDecimal.ZERO;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private InvoiceStatus status = InvoiceStatus.Draft;

	@Column(name = "notes", columnDefinition = "TEXT")
	private String notes;

	@NotNull(message = "Creator is required")
	@Column(name = "created_by")
	private Integer createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by", insertable = false, updatable = false)
	private User createdByUser;

	@Column(name = "created_date", insertable = false, updatable = false)
	private LocalDateTime createdDate;

	@Column(name = "updated_date", insertable = false, updatable = false)
	private LocalDateTime updatedDate;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "invoice_id")
	private List<Payment> payments;

	public BigDecimal getBalance() {
		if (totalAmount == null || amountPaid == null)
			return BigDecimal.ZERO;
		return totalAmount.subtract(amountPaid);
	}

	public enum InvoiceStatus {

		Draft, Sent, Paid, Partial, Void

	}

}

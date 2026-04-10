package org.springframework.samples.petclinic.invoice;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.samples.petclinic.user.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@Setter
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "payment_id")
	private Integer paymentId;

	@NotNull(message = "Invoice is required")
	@Column(name = "invoice_id")
	private Integer invoiceId;

	@NotNull(message = "Payment date is required")
	@Column(name = "payment_date")
	private LocalDateTime paymentDate;

	@NotNull(message = "Payment method is required")
	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method")
	private PaymentMethod paymentMethod;

	@NotNull(message = "Amount is required")
	@Column(name = "amount", precision = 10, scale = 2)
	private BigDecimal amount;

	@Column(name = "transaction_id", length = 100)
	private String transactionId;

	@NotNull(message = "Received by is required")
	@Column(name = "received_by")
	private Integer receivedBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "received_by", insertable = false, updatable = false)
	private User receivedByUser;

	@Column(name = "notes", length = 500)
	private String notes;

	@Column(name = "created_date", insertable = false, updatable = false)
	private LocalDateTime createdDate;

	public enum PaymentMethod {

		Cash, Credit_Card, Debit_Card, Check, Bank_Transfer

	}

}

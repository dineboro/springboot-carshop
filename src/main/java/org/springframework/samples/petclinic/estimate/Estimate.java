package org.springframework.samples.petclinic.estimate;

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
@Table(name = "estimate")
@Getter
@Setter
public class Estimate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "estimate_id")
	private Integer estimateId;

	@NotEmpty(message = "Estimate number is required")
	@Column(name = "estimate_number", unique = true)
	private String estimateNumber;

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

	@NotNull(message = "Estimate date is required")
	@Column(name = "estimate_date")
	private LocalDateTime estimateDate;

	@Column(name = "valid_until")
	private LocalDateTime validUntil;

	@Column(name = "description", length = 1024)
	private String description;

	@Column(name = "labor_cost", precision = 10, scale = 2)
	private BigDecimal laborCost = BigDecimal.ZERO;

	@Column(name = "parts_cost", precision = 10, scale = 2)
	private BigDecimal partsCost = BigDecimal.ZERO;

	@Column(name = "tax_amount", precision = 10, scale = 2)
	private BigDecimal taxAmount = BigDecimal.ZERO;

	@Column(name = "total_estimate", precision = 10, scale = 2)
	private BigDecimal totalEstimate = BigDecimal.ZERO;

	@NotNull(message = "Status is required")
	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private EstimateStatus status = EstimateStatus.Draft;

	@Column(name = "approved_date")
	private LocalDateTime approvedDate;

	@Column(name = "converted_to_appointment")
	private Boolean convertedToAppointment = false;

	@Column(name = "appointment_id")
	private Integer appointmentId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "appointment_id", insertable = false, updatable = false)
	private ServiceAppointment appointment;

	@NotNull(message = "Prepared by is required")
	@Column(name = "prepared_by")
	private Integer preparedBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "prepared_by", insertable = false, updatable = false)
	private User preparedByUser;

	@Column(name = "created_date", insertable = false, updatable = false)
	private LocalDateTime createdDate;

	@Column(name = "updated_date", insertable = false, updatable = false)
	private LocalDateTime updatedDate;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "estimate_id")
	private List<EstimateLineItem> lineItems;

	public enum EstimateStatus {

		Draft, Sent, Approved, Rejected, Expired

	}

}

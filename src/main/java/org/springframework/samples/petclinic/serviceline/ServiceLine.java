package org.springframework.samples.petclinic.serviceline;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.samples.petclinic.appointment.ServiceAppointment;
import org.springframework.samples.petclinic.customer.Vehicle;
import org.springframework.samples.petclinic.employee.Employee;
import org.springframework.samples.petclinic.servicecatalog.ServiceCatalog;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_line")
@Getter
@Setter
public class ServiceLine {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "service_line_id")
	private Integer serviceLineId;

	@NotNull(message = "Appointment is required")
	@Column(name = "appointment_id")
	private Integer appointmentId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "appointment_id", insertable = false, updatable = false)
	private ServiceAppointment appointment;

	@Column(name = "service_catalog_id")
	private Integer serviceCatalogId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "service_catalog_id", insertable = false, updatable = false)
	private ServiceCatalog catalogItem;

	@NotEmpty(message = "VIN is required")
	@Column(name = "vin")
	private String vin;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vin", insertable = false, updatable = false)
	private Vehicle vehicle;

	@NotNull(message = "Service date is required")
	@Column(name = "service_date")
	private LocalDateTime serviceDate;

	@NotEmpty(message = "Description is required")
	@Column(name = "service_description", length = 1024)
	private String serviceDescription;

	@Column(name = "labor_hours", precision = 5, scale = 2)
	private BigDecimal laborHours;

	@Column(name = "labor_rate", precision = 8, scale = 2)
	private BigDecimal laborRate;

	@Column(name = "labor_cost", precision = 10, scale = 2)
	private BigDecimal laborCost;

	@Column(name = "parts_cost", precision = 10, scale = 2)
	private BigDecimal partsCost;

	@NotNull(message = "Total cost is required")
	@Column(name = "total_cost", precision = 10, scale = 2)
	private BigDecimal totalCost = BigDecimal.ZERO;

	@Column(name = "notes", length = 1024)
	private String notes;

	@Column(name = "assigned_to")
	private Integer assignedTo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assigned_to", insertable = false, updatable = false)
	private Employee assignedEmployee;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private ServiceLineStatus status = ServiceLineStatus.Open;

	@Column(name = "created_date", insertable = false, updatable = false)
	private LocalDateTime createdDate;

	@Column(name = "updated_date", insertable = false, updatable = false)
	private LocalDateTime updatedDate;

	public BigDecimal getComputedTotalCost() {
		BigDecimal labor = laborCost != null ? laborCost : BigDecimal.ZERO;
		BigDecimal parts = partsCost != null ? partsCost : BigDecimal.ZERO;
		return labor.add(parts);
	}

	public enum ServiceLineStatus {
		Open, In_Progress, Completed, Cancelled
	}

}

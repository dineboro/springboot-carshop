package org.springframework.samples.petclinic.servicehistory;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.samples.petclinic.appointment.ServiceAppointment;
import org.springframework.samples.petclinic.customer.Vehicle;
import org.springframework.samples.petclinic.employee.Employee;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_service_history")
@Getter
@Setter
public class VehicleServiceHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "service_history_id")
	private Integer serviceHistoryId;

	@NotEmpty(message = "VIN is required")
	@Column(name = "vin")
	private String vin;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vin", insertable = false, updatable = false)
	private Vehicle vehicle;

	@NotNull(message = "Appointment is required")
	@Column(name = "appointment_id")
	private Integer appointmentId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "appointment_id", insertable = false, updatable = false)
	private ServiceAppointment appointment;

	@NotNull(message = "Service date is required")
	@Column(name = "service_date")
	private LocalDateTime serviceDate;

	@Column(name = "mileage")
	private Integer mileage;

	@NotEmpty(message = "Service summary is required")
	@Column(name = "service_summary", length = 1024)
	private String serviceSummary;

	@NotNull(message = "Total cost is required")
	@Column(name = "total_cost", precision = 10, scale = 2)
	private BigDecimal totalCost;

	@Column(name = "performed_by")
	private Integer performedBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "performed_by", insertable = false, updatable = false)
	private Employee performedByEmployee;

	@Column(name = "created_date", insertable = false, updatable = false)
	private LocalDateTime createdDate;

}

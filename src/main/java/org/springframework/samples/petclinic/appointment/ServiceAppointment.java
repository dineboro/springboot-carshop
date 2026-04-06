package org.springframework.samples.petclinic.appointment;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.samples.petclinic.customer.Customer;
import org.springframework.samples.petclinic.customer.Vehicle;

import java.time.LocalDateTime;

@Entity
@Table(name = "service_appointment")
@Getter
@Setter
public class ServiceAppointment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "appointment_id")
	private Integer appointmentId;

	@NotNull(message = "Customer is required")
	@Column(name = "customer_id")
	private Integer customerId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", insertable = false, updatable = false)
	private Customer customer;

	@NotEmpty(message = "Vehicle is required")
	@Column(name = "vin")
	private String vin;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vin", insertable = false, updatable = false)
	private Vehicle vehicle;

	@NotNull(message = "Appointment date is required")
	@Column(name = "appointment_date")
	@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime appointmentDate;

	@Column(name = "description", length = 1024)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private AppointmentStatus status = AppointmentStatus.Scheduled;

	@Column(name = "notes", columnDefinition = "TEXT")
	private String notes;

	@Column(name = "created_date", insertable = false, updatable = false)
	private LocalDateTime createdDate;

	@Column(name = "updated_date", insertable = false, updatable = false)
	private LocalDateTime updatedDate;

	public enum AppointmentStatus {

		Scheduled, In_Progress, Completed, Cancelled, No_Show

	}

}

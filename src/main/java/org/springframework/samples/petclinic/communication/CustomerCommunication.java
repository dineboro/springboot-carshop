package org.springframework.samples.petclinic.communication;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.samples.petclinic.appointment.ServiceAppointment;
import org.springframework.samples.petclinic.customer.Customer;
import org.springframework.samples.petclinic.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_communication")
@Getter
@Setter
public class CustomerCommunication {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "communication_id")
	private Integer communicationId;

	@NotNull(message = "Customer is required")
	@Column(name = "customer_id")
	private Integer customerId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", insertable = false, updatable = false)
	private Customer customer;

	@Column(name = "appointment_id")
	private Integer appointmentId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "appointment_id", insertable = false, updatable = false)
	private ServiceAppointment appointment;

	@NotNull(message = "Communication type is required")
	@Enumerated(EnumType.STRING)
	@Column(name = "communication_type")
	private CommunicationType communicationType;

	@NotNull(message = "Direction is required")
	@Enumerated(EnumType.STRING)
	@Column(name = "direction")
	private Direction direction;

	@Column(name = "subject", length = 256)
	private String subject;

	@Column(name = "notes", columnDefinition = "TEXT")
	private String notes;

	@Column(name = "contacted_by")
	private Integer contactedBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contacted_by", insertable = false, updatable = false)
	private User contactedByUser;

	@Column(name = "communication_date")
	private LocalDateTime communicationDate;

	@Column(name = "follow_up_required")
	private Boolean followUpRequired = false;

	@Column(name = "follow_up_date")
	private LocalDateTime followUpDate;

	public enum CommunicationType {

		Phone_Call, Email, SMS, In_Person, Other

	}

	public enum Direction {

		Inbound, Outbound

	}

}

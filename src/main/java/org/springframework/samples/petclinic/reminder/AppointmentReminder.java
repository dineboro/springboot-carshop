package org.springframework.samples.petclinic.reminder;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.samples.petclinic.appointment.ServiceAppointment;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointment_reminder")
@Getter
@Setter
public class AppointmentReminder {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "reminder_id")
	private Integer reminderId;

	@NotNull(message = "Appointment is required")
	@Column(name = "appointment_id")
	private Integer appointmentId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "appointment_id", insertable = false, updatable = false)
	private ServiceAppointment appointment;

	@NotNull(message = "Reminder type is required")
	@Enumerated(EnumType.STRING)
	@Column(name = "reminder_type")
	private ReminderType reminderType;

	@NotNull(message = "Scheduled date is required")
	@Column(name = "scheduled_date")
	private LocalDateTime scheduledDate;

	@Column(name = "sent_date")
	private LocalDateTime sentDate;

	@NotNull(message = "Status is required")
	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private ReminderStatus status = ReminderStatus.Scheduled;

	@Column(name = "message", columnDefinition = "TEXT")
	private String message;

	@Column(name = "created_date", insertable = false, updatable = false)
	private LocalDateTime createdDate;

	public enum ReminderType {

		Email, SMS, Phone_Call

	}

	public enum ReminderStatus {

		Scheduled, Sent, Failed, Cancelled

	}

}

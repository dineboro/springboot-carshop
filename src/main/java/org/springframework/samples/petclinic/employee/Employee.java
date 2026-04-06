package org.springframework.samples.petclinic.employee;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee")
@Getter
@Setter
@SQLDelete(sql = "UPDATE employee SET deleted_at = NOW() WHERE employee_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Employee {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "employee_id")
	private Integer employeeId;

	@NotEmpty(message = "First name is required")
	@Column(name = "first_name")
	private String firstName;

	@NotEmpty(message = "Last name is required")
	@Column(name = "last_name")
	private String lastName;

	@NotNull(message = "Role is required")
	@Enumerated(EnumType.STRING)
	@Column(name = "role")
	private EmployeeRole role;

	@Column(name = "email")
	private String email;

	@Column(name = "phone")
	private String phone;

	@Column(name = "hired_date")
	private LocalDate hiredDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private EmployeeStatus status = EmployeeStatus.ACTIVE;

	@Column(name = "created_date", insertable = false, updatable = false)
	private LocalDateTime createdDate;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	public String getFullName() {
		return firstName + " " + lastName;
	}

	public enum EmployeeRole {

		Technician, Service_Advisor, Manager, Receptionist

	}

	public enum EmployeeStatus {

		ACTIVE, INACTIVE

	}

}

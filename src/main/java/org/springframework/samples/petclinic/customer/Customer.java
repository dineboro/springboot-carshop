package org.springframework.samples.petclinic.customer;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer")
@Getter
@Setter
// Intercept the delete command and turn it into an update
@SQLDelete(sql = "UPDATE customer SET deleted_at = NOW() WHERE customer_id = ?")
// Automatically filter out deleted rows when reading data
@SQLRestriction("deleted_at IS NULL")
public class Customer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "customer_id")
	private Integer customerId;

	@Column(name = "user_id")
	private Integer userId; // Links to users table when customer registers for portal

	@Column(name = "customer_name")
	@NotEmpty(message = "{NotEmpty.customer.name}")
	private String customerName;

	@Column(name = "phone")
	@NotEmpty(message = "{NotEmpty.customer.phone}")
	@Pattern(regexp = "^\\d{10}$", message = "{telephone.invalid}")
	private String phone;

	@Column(name = "email")
	@Email(message = "Please enter a valid email address")
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private CustomerStatus status = CustomerStatus.ACTIVE;

	@Column(name = "created_date", insertable = false, updatable = false)
	private LocalDateTime createdDate;

	@Column(name = "updated_date", insertable = false, updatable = false)
	private LocalDateTime updatedDate;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "customer", fetch = FetchType.EAGER)
	private List<Vehicle> vehicles = new ArrayList<>();

	public void addVehicle(Vehicle vehicle) {
		vehicle.setCustomer(this);
		getVehicles().add(vehicle);
	}

	public enum CustomerStatus {

		ACTIVE, INACTIVE, SUSPENDED

	}

}

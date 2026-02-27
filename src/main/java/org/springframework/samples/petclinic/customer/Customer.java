package org.springframework.samples.petclinic.customer;

import jakarta.persistence.*;
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
@Table(name = "Customer")
@Getter
@Setter
@SQLDelete(sql = "UPDATE Customer SET deleted_at = NOW() WHERE Customer_ID = ?")
@SQLRestriction("deleted_at IS NULL")
public class Customer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Customer_ID")
	private Integer customerId;

	@Column(name = "user_id")
	private Integer userId;

	@Column(name = "CustomerName")
	@NotEmpty(message = "{NotEmpty.customer.name}")
	private String customerName;

	@Column(name = "Phone")
	@NotEmpty(message = "{NotEmpty.customer.phone}")
	@Pattern(regexp = "^\\d{10}$", message = "{telephone.invalid}")
	private String phone;

	@Column(name = "Email")
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(name = "Status")
	private CustomerStatus status = CustomerStatus.ACTIVE;

	@Column(name = "CreatedDate", insertable = false, updatable = false)
	private LocalDateTime createdDate;

	@Column(name = "UpdatedDate", insertable = false, updatable = false)
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

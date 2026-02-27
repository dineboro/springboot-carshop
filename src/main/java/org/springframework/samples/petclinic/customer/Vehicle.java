package org.springframework.samples.petclinic.customer;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "Vehicle")
@Getter
@Setter
@SQLDelete(sql = "UPDATE Vehicle SET deleted_at = NOW() WHERE VIN = ?")
@SQLRestriction("deleted_at IS NULL")
public class Vehicle {

	@Id
	@Column(name = "VIN", length = 17)
	@NotEmpty(message = "{NotEmpty.vehicle.vin}")
	@Size(min = 17, max = 17, message = "{Size.vehicle.vin}")
	private String vin;

	@ManyToOne
	@JoinColumn(name = "Customer_ID")
	private Customer customer;

	@Column(name = "Make")
	@NotEmpty(message = "{NotEmpty.vehicle.make}")
	private String make;

	@Column(name = "Model")
	@NotEmpty(message = "{NotEmpty.vehicle.model}")
	private String model;

	@Column(name = "ModelYear")
	@Min(value = 1900, message = "{Min.vehicle.modelYear}")
	@Max(value = 2026, message = "{Max.vehicle.modelYear}")
	private Integer modelYear;

	@Column(name = "Color")
	private String color;

	@Column(name = "LicensePlate")
	private String licensePlate;

	@Column(name = "CurrentMileage")
	private Integer currentMileage;

	@Enumerated(EnumType.STRING)
	@Column(name = "Status")
	private VehicleStatus status = VehicleStatus.ACTIVE;

	@Column(name = "CreatedDate", insertable = false, updatable = false)
	private LocalDateTime createdDate;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	public enum VehicleStatus {
		ACTIVE, IN_SERVICE, RETIRED
	}

}

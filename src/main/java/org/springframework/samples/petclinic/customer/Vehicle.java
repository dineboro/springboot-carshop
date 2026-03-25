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
@Table(name = "vehicle")
@Getter
@Setter
@SQLDelete(sql = "UPDATE vehicle SET deleted_at = NOW() WHERE vin = ?")
@SQLRestriction("deleted_at IS NULL")
public class Vehicle {

	@Id
	@Column(name = "vin", length = 17)
	@NotEmpty(message = "{NotEmpty.vehicle.vin}")
	@Size(min = 17, max = 17, message = "{Size.vehicle.vin}")
	private String vin;

	@ManyToOne
	@JoinColumn(name = "customer_id")
	private Customer customer;

	@Column(name = "make")
	@NotEmpty(message = "{NotEmpty.vehicle.make}")
	private String make;

	@Column(name = "model")
	@NotEmpty(message = "{NotEmpty.vehicle.model}")
	private String model;

	@Column(name = "model_year")
	@Min(value = 1900, message = "{Min.vehicle.modelYear}")
	@Max(value = 2026, message = "{Max.vehicle.modelYear}")
	private Integer modelYear;

	@Column(name = "color")
	private String color;

	@Column(name = "license_plate")
	private String licensePlate;

	@Column(name = "current_mileage")
	private Integer currentMileage;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private VehicleStatus status = VehicleStatus.ACTIVE;

	@Column(name = "created_date", insertable = false, updatable = false)
	private LocalDateTime createdDate;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	public enum VehicleStatus {

		ACTIVE, IN_SERVICE, RETIRED

	}

}

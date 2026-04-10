package org.springframework.samples.petclinic.servicecatalog;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_catalog")
@Getter
@Setter
public class ServiceCatalog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "service_catalog_id")
	private Integer catalogId;

	@NotEmpty(message = "Service name is required")
	@Column(name = "service_name", length = 256)
	private String name;

	@Column(name = "category", length = 100)
	private String category;

	@Column(name = "description", length = 1024)
	private String description;

	@Column(name = "estimated_duration", precision = 5, scale = 2)
	private BigDecimal defaultLaborHours;

	@Column(name = "standard_labor_rate", precision = 8, scale = 2)
	private BigDecimal laborRate;

	@Column(name = "base_price", precision = 10, scale = 2)
	private BigDecimal defaultPrice;

	@Column(name = "is_active")
	private boolean active = true;

	@Column(name = "created_date", insertable = false, updatable = false)
	private LocalDateTime createdDate;

}

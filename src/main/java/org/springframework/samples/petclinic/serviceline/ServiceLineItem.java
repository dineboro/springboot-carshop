package org.springframework.samples.petclinic.serviceline;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.samples.petclinic.servicecatalog.ServiceCatalog;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_line_item")
@Getter
@Setter
public class ServiceLineItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "service_line_item_id")
	private Integer serviceLineItemId;

	@NotNull(message = "Service line is required")
	@Column(name = "service_line_id")
	private Integer serviceLineId;

	@Column(name = "service_catalog_id")
	private Integer catalogId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "service_catalog_id", insertable = false, updatable = false)
	private ServiceCatalog catalogItem;

	@NotNull(message = "Item type is required")
	@Enumerated(EnumType.STRING)
	@Column(name = "item_type")
	private ItemType itemType;

	@NotEmpty(message = "Description is required")
	@Column(name = "description", length = 500)
	private String description;

	@NotNull(message = "Quantity is required")
	@Column(name = "quantity", precision = 8, scale = 2)
	private BigDecimal quantity = BigDecimal.ONE;

	@NotNull(message = "Unit price is required")
	@Column(name = "unit_price", precision = 10, scale = 2)
	private BigDecimal unitPrice;

	@NotNull(message = "Total price is required")
	@Column(name = "total_price", precision = 10, scale = 2)
	private BigDecimal totalPrice;

	@Column(name = "notes", length = 500)
	private String notes;

	@Column(name = "created_date", insertable = false, updatable = false)
	private LocalDateTime createdDate;

	public enum ItemType {

		Service, Part, Labor

	}

}

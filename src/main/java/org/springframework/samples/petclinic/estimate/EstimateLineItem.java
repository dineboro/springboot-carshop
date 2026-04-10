package org.springframework.samples.petclinic.estimate;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "estimate_line_item")
@Getter
@Setter
public class EstimateLineItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "estimate_line_item_id")
	private Integer estimateLineItemId;

	@NotNull(message = "Estimate is required")
	@Column(name = "estimate_id")
	private Integer estimateId;

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

	public enum ItemType {

		Service, Part, Labor

	}

}

package org.springframework.samples.petclinic.school;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.samples.petclinic.model.NamedEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "locations")
@Getter // Auto-generates getters for all fields
@Setter // Auto-generates setters for all fields
@SQLDelete(sql = "UPDATE locations SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Location extends NamedEntity {

	@ManyToOne
	@JoinColumn(name = "school_id")
	private School school;

	@ManyToOne
	@JoinColumn(name = "parent_location_id")
	private Location parentLocation;

	@Column(name = "description")
	private String description;

	@Column(name = "address")
	private String address;

	@Column(name = "latitude")
	private BigDecimal latitude;

	@Column(name = "longitude")
	private BigDecimal longitude;

	@Enumerated(EnumType.STRING)
	@Column(name = "status_id")
	private LocationStatus status = LocationStatus.ACTIVE;

	@Column(name = "created_at", insertable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", insertable = false, updatable = false)
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	public enum LocationStatus {

		DRAFT, ACTIVE, CLOSED, COMING_SOON;

	}

	// No more manual code below this line!

}

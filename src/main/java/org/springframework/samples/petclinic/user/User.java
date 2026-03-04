package org.springframework.samples.petclinic.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.samples.petclinic.model.BaseEntity;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User extends BaseEntity {

	@Column(name="first_name", nullable = true, length = 50)
	private String firstName;

	@Column(name="last_name", nullable = true, length = 50)
	private String lastName;

	@Column(nullable = false, unique = true, length = 100)
	@NotEmpty(message = "Email is required")
	@Email(message = "Please enter a valid email")
	private String email;

	@Column(name="password_hash", nullable = true, length = 255)
	@NotEmpty(message = "Password is required")
	@Size(min = 8, message = "Password must be at least 8 characters")
	@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$", message = "Password must contain uppercase, lowercase, and number")
	private String password;

	@Column(name="phone", length = 20)
	private String phone;

	// NEW: Approval fields for manager registration
	@Column(name="is_active")
	private Boolean isActive = true;

	@Column(name="is_approved")
	private Boolean isApproved = false;  // Default FALSE - requires admin approval

	@Column(name="created_at")
	private LocalDateTime createdAt;

	@Column(name="updated_at")
	private LocalDateTime updatedAt;

	// Many-to-Many Relationship with Role
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
		name = "user_roles",
		joinColumns = @JoinColumn(name = "user_id"),
		inverseJoinColumns = @JoinColumn(name = "role_id")
	)
	@EqualsAndHashCode.Exclude
	private Set<Role> roles;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}

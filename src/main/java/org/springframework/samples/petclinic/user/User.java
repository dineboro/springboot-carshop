package org.springframework.samples.petclinic.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.samples.petclinic.model.BaseEntity;

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

	// Many-to-Many Relationship with Role
	@ManyToMany(fetch = FetchType.EAGER) // Fetch roles immediately when a user is loaded
	@JoinTable(
		name = "user_roles", // Name of the junction table in MySQL
		joinColumns = @JoinColumn(name = "user_id"), // Column in user_roles that references the 'users' table
		inverseJoinColumns = @JoinColumn(name = "role_id") // Column in user_roles that references the 'roles' table
	)
	@EqualsAndHashCode.Exclude
	private Set<Role> roles;
}

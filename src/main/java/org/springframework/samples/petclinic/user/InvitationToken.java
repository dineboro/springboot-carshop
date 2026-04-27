package org.springframework.samples.petclinic.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customer_invitations")
@Data
@NoArgsConstructor
public class InvitationToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "customer_id", nullable = false)
	private Integer customerId;

	@Column(name = "email", nullable = false)
	private String email;

	@Column(name = "invitation_token", nullable = false, unique = true)
	private String token;

	@Column(name = "invited_by")
	private Integer invitedBy;

	@Column(name = "invited_at")
	private LocalDateTime invitedAt;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(name = "is_used")
	private Boolean isUsed = false;

	@Column(name = "used_at")
	private LocalDateTime usedAt;

	@PrePersist
	protected void onCreate() {
		invitedAt = LocalDateTime.now();
	}

}

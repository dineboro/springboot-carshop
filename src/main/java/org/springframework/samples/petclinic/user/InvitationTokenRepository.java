package org.springframework.samples.petclinic.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationTokenRepository extends JpaRepository<InvitationToken, Integer> {

	Optional<InvitationToken> findByToken(String token);

}

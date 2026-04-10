package org.springframework.samples.petclinic.communication;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CustomerCommunicationRepository extends Repository<CustomerCommunication, Integer> {

	@Transactional(readOnly = true)
	Page<CustomerCommunication> findAll(Pageable pageable);

	@Transactional(readOnly = true)
	Optional<CustomerCommunication> findById(Integer id);

	@Transactional(readOnly = true)
	List<CustomerCommunication> findByCustomerId(Integer customerId);

	@Transactional(readOnly = true)
	List<CustomerCommunication> findByAppointmentId(Integer appointmentId);

	@Transactional(readOnly = true)
	@Query("SELECT c FROM CustomerCommunication c WHERE c.followUpRequired = true ORDER BY c.followUpDate ASC")
	List<CustomerCommunication> findPendingFollowUps();

	void save(CustomerCommunication communication);

	void delete(CustomerCommunication communication);

}

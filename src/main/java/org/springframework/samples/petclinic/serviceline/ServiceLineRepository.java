package org.springframework.samples.petclinic.serviceline;

import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ServiceLineRepository extends Repository<ServiceLine, Integer> {

	@Transactional(readOnly = true)
	List<ServiceLine> findByAppointmentId(Integer appointmentId);

	@Transactional(readOnly = true)
	Optional<ServiceLine> findById(Integer id);

	@Transactional(readOnly = true)
	List<ServiceLine> findByAssignedTo(Integer employeeId);

	@Transactional(readOnly = true)
	List<ServiceLine> findByStatus(ServiceLine.ServiceLineStatus status);

	void save(ServiceLine serviceLine);

	void delete(ServiceLine serviceLine);

}

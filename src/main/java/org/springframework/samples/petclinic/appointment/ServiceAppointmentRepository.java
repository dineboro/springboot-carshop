package org.springframework.samples.petclinic.appointment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ServiceAppointmentRepository extends Repository<ServiceAppointment, Integer> {

	@Transactional(readOnly = true)
	@Query(value = "SELECT a FROM ServiceAppointment a LEFT JOIN FETCH a.vehicle LEFT JOIN FETCH a.customer",
			countQuery = "SELECT COUNT(a) FROM ServiceAppointment a")
	Page<ServiceAppointment> findAll(Pageable pageable);

	@Transactional(readOnly = true)
	List<ServiceAppointment> findAll();

	@Transactional(readOnly = true)
	Optional<ServiceAppointment> findById(Integer id);

	@Transactional(readOnly = true)
	@Query("SELECT a FROM ServiceAppointment a LEFT JOIN FETCH a.vehicle LEFT JOIN FETCH a.customer WHERE a.appointmentId = :id")
	Optional<ServiceAppointment> findByIdWithDetails(Integer id);

	void save(ServiceAppointment appointment);

	@Transactional(readOnly = true)
	@Query("SELECT a FROM ServiceAppointment a LEFT JOIN FETCH a.vehicle WHERE a.customerId = :customerId ORDER BY a.appointmentDate DESC")
	List<ServiceAppointment> findByCustomerId(Integer customerId);

	@Transactional(readOnly = true)
	@Query("SELECT a FROM ServiceAppointment a WHERE a.vin = :vin ORDER BY a.appointmentDate DESC")
	List<ServiceAppointment> findByVin(String vin);

}

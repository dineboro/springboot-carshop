package org.springframework.samples.petclinic.servicehistory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface VehicleServiceHistoryRepository extends Repository<VehicleServiceHistory, Integer> {

	@Transactional(readOnly = true)
	Page<VehicleServiceHistory> findAll(Pageable pageable);

	@Transactional(readOnly = true)
	Optional<VehicleServiceHistory> findById(Integer id);

	@Transactional(readOnly = true)
	List<VehicleServiceHistory> findByVinOrderByServiceDateDesc(String vin);

	@Transactional(readOnly = true)
	List<VehicleServiceHistory> findByAppointmentId(Integer appointmentId);

	void save(VehicleServiceHistory history);

	void delete(VehicleServiceHistory history);

}

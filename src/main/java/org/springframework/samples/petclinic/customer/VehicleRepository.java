package org.springframework.samples.petclinic.customer;

import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends Repository<Vehicle, String> {

	@Transactional(readOnly = true)
	Optional<Vehicle> findById(String vin);

	@Transactional(readOnly = true)
	List<Vehicle> findAll();

	void save(Vehicle vehicle);

	void delete(Vehicle vehicle);

	@Transactional(readOnly = true)
	List<Vehicle> findByCustomer(Customer customer);

}

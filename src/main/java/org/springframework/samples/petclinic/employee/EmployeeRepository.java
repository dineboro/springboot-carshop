package org.springframework.samples.petclinic.employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends Repository<Employee, Integer> {

	@Transactional(readOnly = true)
	Page<Employee> findAll(Pageable pageable);

	@Transactional(readOnly = true)
	List<Employee> findAll();

	@Transactional(readOnly = true)
	Optional<Employee> findById(Integer id);

	void save(Employee employee);

	void delete(Employee employee);

	@Transactional(readOnly = true)
	@Query("SELECT e FROM Employee e WHERE LOWER(e.lastName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :name, '%'))")
	Page<Employee> findByNameContaining(String name, Pageable pageable);

}

package org.springframework.samples.petclinic.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

public interface CustomerRepository extends Repository<Customer, Integer> {

	@Transactional(readOnly = true)
	Collection<Customer> findAll();

	@Transactional(readOnly = true)
	Page<Customer> findAll(Pageable pageable);

	void save(Customer customer);

	@Transactional(readOnly = true)
	Optional<Customer> findById(Integer id);

	@Transactional(readOnly = true)
	@Query("SELECT c FROM Customer c WHERE c.phone = :phone")
	Optional<Customer> findByPhone(String phone);

	@Transactional(readOnly = true)
	@Query("SELECT c FROM Customer c WHERE c.email = :email")
	Optional<Customer> findByEmail(String email);

	@Transactional(readOnly = true)
	@Query("SELECT c FROM Customer c WHERE LOWER(c.customerName) LIKE LOWER(CONCAT('%', :name, '%'))")
	Page<Customer> findByNameContaining(String name, Pageable pageable);

}

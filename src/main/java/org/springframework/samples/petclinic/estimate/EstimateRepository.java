package org.springframework.samples.petclinic.estimate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface EstimateRepository extends Repository<Estimate, Integer> {

	@Transactional(readOnly = true)
	Page<Estimate> findAll(Pageable pageable);

	@Transactional(readOnly = true)
	@Query("SELECT e FROM Estimate e JOIN e.customer c WHERE LOWER(c.customerName) LIKE LOWER(CONCAT('%', :query, '%')) OR c.phone LIKE CONCAT('%', :query, '%')")
	Page<Estimate> searchByCustomer(@Param("query") String query, Pageable pageable);

	@Transactional(readOnly = true)
	Optional<Estimate> findById(Integer id);

	@Transactional(readOnly = true)
	List<Estimate> findByCustomerId(Integer customerId);

	void save(Estimate estimate);

	@Transactional(readOnly = true)
	@Query("SELECT COALESCE(MAX(CAST(SUBSTRING(e.estimateNumber, 5) AS int)), 0) FROM Estimate e WHERE e.estimateNumber LIKE 'EST-%'")
	Integer findMaxEstimateNumber();

}

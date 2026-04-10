package org.springframework.samples.petclinic.invoice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends Repository<Invoice, Integer> {

	@Transactional(readOnly = true)
	Page<Invoice> findAll(Pageable pageable);

	@Transactional(readOnly = true)
	@Query("SELECT i FROM Invoice i JOIN i.customer c WHERE LOWER(c.customerName) LIKE LOWER(CONCAT('%', :query, '%')) OR c.phone LIKE CONCAT('%', :query, '%')")
	Page<Invoice> searchByCustomer(@Param("query") String query, Pageable pageable);

	@Transactional(readOnly = true)
	Optional<Invoice> findById(Integer id);

	@Transactional(readOnly = true)
	Optional<Invoice> findByAppointmentId(Integer appointmentId);

	@Transactional(readOnly = true)
	List<Invoice> findByCustomerId(Integer customerId);

	void save(Invoice invoice);

	@Transactional(readOnly = true)
	@Query("SELECT COALESCE(MAX(CAST(SUBSTRING(i.invoiceNumber, 5) AS int)), 0) FROM Invoice i WHERE i.invoiceNumber LIKE 'INV-%'")
	Integer findMaxInvoiceNumber();

}

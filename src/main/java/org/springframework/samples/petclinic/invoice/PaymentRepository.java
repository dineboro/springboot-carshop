package org.springframework.samples.petclinic.invoice;

import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends Repository<Payment, Integer> {

	@Transactional(readOnly = true)
	List<Payment> findByInvoiceId(Integer invoiceId);

	@Transactional(readOnly = true)
	Optional<Payment> findById(Integer id);

	void save(Payment payment);

	void delete(Payment payment);

}

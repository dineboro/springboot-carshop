package org.springframework.samples.petclinic.serviceline;

import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ServiceLineItemRepository extends Repository<ServiceLineItem, Integer> {

	@Transactional(readOnly = true)
	List<ServiceLineItem> findByServiceLineId(Integer serviceLineId);

	@Transactional(readOnly = true)
	Optional<ServiceLineItem> findById(Integer id);

	void save(ServiceLineItem item);

	void delete(ServiceLineItem item);

}

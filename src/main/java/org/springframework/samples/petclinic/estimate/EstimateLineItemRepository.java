package org.springframework.samples.petclinic.estimate;

import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface EstimateLineItemRepository extends Repository<EstimateLineItem, Integer> {

	@Transactional(readOnly = true)
	List<EstimateLineItem> findByEstimateId(Integer estimateId);

	@Transactional(readOnly = true)
	Optional<EstimateLineItem> findById(Integer id);

	void save(EstimateLineItem item);

	void delete(EstimateLineItem item);

}

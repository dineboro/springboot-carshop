package org.springframework.samples.petclinic.servicecatalog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ServiceCatalogRepository extends Repository<ServiceCatalog, Integer> {

	@Transactional(readOnly = true)
	Page<ServiceCatalog> findAll(Pageable pageable);

	@Transactional(readOnly = true)
	List<ServiceCatalog> findAll();

	@Transactional(readOnly = true)
	List<ServiceCatalog> findByActive(boolean active);

	@Transactional(readOnly = true)
	Optional<ServiceCatalog> findById(Integer id);

	void save(ServiceCatalog serviceCatalog);

	void delete(ServiceCatalog serviceCatalog);

	@Transactional(readOnly = true)
	@Query("SELECT s FROM ServiceCatalog s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
	Page<ServiceCatalog> findByNameContaining(String name, Pageable pageable);

}

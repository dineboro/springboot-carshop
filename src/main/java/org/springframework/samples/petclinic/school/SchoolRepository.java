package org.springframework.samples.petclinic.school;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collection;
import java.util.Optional;

public interface SchoolRepository extends Repository<School, Integer> {
	@Transactional(readOnly = true)
	Collection<School> findAll();

	@Transactional(readOnly = true)
	Page<School> findAll(Pageable pageable);

	void save(School school);

	@Transactional(readOnly = true)
	Optional<School> findById(Integer id);

	@Transactional(readOnly = true)
	@Query("SELECT s FROM School s WHERE s.domain = :domain")
	Optional<School> findByDomain(String domain);
}

package org.springframework.samples.petclinic.reminder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentReminderRepository extends Repository<AppointmentReminder, Integer> {

	@Transactional(readOnly = true)
	Page<AppointmentReminder> findAll(Pageable pageable);

	@Transactional(readOnly = true)
	Optional<AppointmentReminder> findById(Integer id);

	@Transactional(readOnly = true)
	List<AppointmentReminder> findByAppointmentId(Integer appointmentId);

	@Transactional(readOnly = true)
	@Query("SELECT r FROM AppointmentReminder r WHERE r.status = 'Scheduled' AND r.scheduledDate <= :now ORDER BY r.scheduledDate ASC")
	List<AppointmentReminder> findDueReminders(LocalDateTime now);

	void save(AppointmentReminder reminder);

	void delete(AppointmentReminder reminder);

}

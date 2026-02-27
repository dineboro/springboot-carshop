package org.springframework.samples.petclinic.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.samples.petclinic.school.School;
import org.springframework.samples.petclinic.school.SchoolRepository;

import java.util.Optional;

@Component
public class UniqueDomainValidator implements ConstraintValidator<UniqueDomain, School> {

	@Autowired
	private SchoolRepository schoolRepository;

	private static ApplicationContext staticContext;

	@Override
	public boolean isValid(School school, ConstraintValidatorContext context) {
		if (schoolRepository == null && staticContext != null) {
			schoolRepository = staticContext.getBean(SchoolRepository.class);
		}

		if (schoolRepository == null) {
			return true;
		}

		if (school == null || school.getDomain() == null) {
			return true; // Lets @NotEmpty handle null checks
		}

		String domainToCheck = school.getDomain();
		Optional<School> existingSchool = schoolRepository.findByDomain(domainToCheck);

		if (existingSchool.isPresent()) {
			// Duplicate Found: If the ID of the found school is DIFFERENT from the school being validated, it's a conflict.
			// (This allows you to "Update" School #1 without changing its domain)
			if (!existingSchool.get().getId().equals(school.getId())) {

				// This block moves the error from the "Class" level to the specific "domain" field
				// so it appears next to the input box in your form.
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
					.addPropertyNode("domain")
					.addConstraintViolation();

				return false;
			}
		}

		return true;
	}
}

package org.springframework.samples.petclinic.servicecatalog;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ServiceCategoryConverter implements AttributeConverter<ServiceCatalog.ServiceCategory, String> {

	@Override
	public String convertToDatabaseColumn(ServiceCatalog.ServiceCategory category) {
		return category == null ? null : category.name();
	}

	@Override
	public ServiceCatalog.ServiceCategory convertToEntityAttribute(String dbValue) {
		if (dbValue == null || dbValue.isBlank()) return null;
		// Try matching by enum name first (new format: OIL_CHANGE)
		for (ServiceCatalog.ServiceCategory c : ServiceCatalog.ServiceCategory.values()) {
			if (c.name().equalsIgnoreCase(dbValue)) return c;
		}
		// Fall back to matching by label (old format: "Oil Change")
		for (ServiceCatalog.ServiceCategory c : ServiceCatalog.ServiceCategory.values()) {
			if (c.getLabel().equalsIgnoreCase(dbValue)) return c;
		}
		return ServiceCatalog.ServiceCategory.OTHER;
	}

}
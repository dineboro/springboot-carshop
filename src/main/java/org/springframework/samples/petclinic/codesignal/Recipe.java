package org.springframework.samples.petclinic.codesignal;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "recipes")
@Getter
@Setter
public class Recipe {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "recipe_ingredients") // This tells JPA the database column name is
											// "recipe_ingredients"
	@JsonProperty("recipe_ingredients") // This tells Jackson the JSON key name is
										// "recipe_ingredients"
	private String ingredients;

	private String instructions;

	private String type;

	private String category;

	// Spring/Hibernate automatically maps camelCase to snake_case (dietary_preference)
	private String dietaryPreference;

	private String internalNotes;

}

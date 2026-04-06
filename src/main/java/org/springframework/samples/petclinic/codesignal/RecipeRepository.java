package org.springframework.samples.petclinic.codesignal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

	List<Recipe> findByCategoryIgnoreCase(String category);

	List<Recipe> findByTypeIgnoreCase(String type);

}

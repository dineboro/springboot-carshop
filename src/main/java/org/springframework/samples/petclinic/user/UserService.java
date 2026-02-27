package org.springframework.samples.petclinic.user;

public interface UserService {
	/**
	 * Handles business logic for registering a new user(password hashing, role assignments)
	 * @param user The User object containing new user details.
	 * @return The saved User object
	 */
	User registerNewStudent(User user);
}

package org.springframework.samples.petclinic.user;

public interface UserService {
	User registerNewStudent(User user);
	User registerNewManager(User user);  // NEW
	User registerNewCustomer(User user);
	User createEmployee(User user, String roleName, Integer createdBy); // NEW
}

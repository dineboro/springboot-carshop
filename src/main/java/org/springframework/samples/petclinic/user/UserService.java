package org.springframework.samples.petclinic.user;

public interface UserService {
	User registerNewStudent(User user);  // Keep for instructor's demo
	User registerNewUser(User user);     // NEW: General registration (no role, needs approval)
	User registerNewCustomer(User user); // For customer portal
	void assignRole(Integer userId, String roleName); // NEW: Admin assigns role
}

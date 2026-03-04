package org.springframework.samples.petclinic.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the {@link CustomerController}
 */
@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

	private static final int TEST_CUSTOMER_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CustomerRepository customers;

	private Customer customer;

	@BeforeEach
	void setup() {
		// Create a customer to be returned by the mocked repository
		customer = new Customer();
		customer.setCustomerId(TEST_CUSTOMER_ID);
		customer.setCustomerName("John Doe");
		customer.setPhone("5551234567");
		customer.setEmail("john.doe@example.com");
		customer.setStatus(Customer.CustomerStatus.ACTIVE);
	}

	@Test
	void testShowCustomerList() throws Exception {
		// 1. Arrange: Create a "Page" of customers to mock the database response
		// matches the 5 items per page logic in your controller
		Pageable pageable = PageRequest.of(0, 5);
		Page<Customer> customerPage = new PageImpl<>(List.of(customer), pageable, 1);

		// Tell the mock: "When the controller asks for all customers, give them this list"
		given(this.customers.findAll(any(Pageable.class))).willReturn(customerPage);

		// 2. Act & Assert: Perform the GET request and verify the results
		mockMvc.perform(get("/customers").param("page", "1"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("listCustomers"))
			.andExpect(model().attributeExists("totalPages"))
			.andExpect(model().attributeExists("currentPage"))
			.andExpect(view().name("customers/customerList"));
	}

	@Test
	@DisplayName("User clicks \"Add Customer\" -> GET /customers/new")
	void testInitCreationForm() throws Exception {
		mockMvc.perform(get("/customers/new"))
			.andExpect(status().isOk())
			.andExpect(view().name("customers/createOrUpdateCustomerForm"))
			.andExpect(model().attributeExists("customer"));
	}

	@Test
	@DisplayName("Validation Passed -> verify that the controller tells the repository to save() the customer and then redirects us.")
	void testProcessCreationFormSuccess() throws Exception {
		mockMvc.perform(post("/customers/new")
				.param("customerName", "Jane Smith")
				.param("phone", "5559876543")
				.param("email", "jane.smith@example.com"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/customers"));

		// Verify that the repository.save() method was actually called
		verify(customers).save(any(Customer.class));
	}

	@Test
	@DisplayName("Validation Failed -> send an empty name and ensure the controller returns us to the form instead of saving.")
	void testProcessCreationFormHasErrors() throws Exception {
		mockMvc.perform(post("/customers/new")
				.param("customerName", "") // Empty name should trigger @NotEmpty
				.param("phone", ""))  // Empty phone should trigger @NotEmpty
			.andExpect(status().isOk()) // 200 OK because we are re-rendering the form, not redirecting
			.andExpect(model().attributeHasErrors("customer"))
			.andExpect(model().attributeHasFieldErrors("customer", "customerName"))
			.andExpect(model().attributeHasFieldErrors("customer", "phone"))
			.andExpect(view().name("customers/createOrUpdateCustomerForm"));
	}
}

package org.springframework.samples.petclinic.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link CustomerController}
 */
@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CustomerRepository customerRepository;

	private Customer testCustomer;
	private List<Customer> customerList;

	@BeforeEach
	void setUp() {
		// Create test customer
		testCustomer = new Customer();
		testCustomer.setCustomerId(1);
		testCustomer.setCustomerName("John Doe");
		testCustomer.setPhone("5551234567");
		testCustomer.setEmail("john.doe@example.com");
		testCustomer.setStatus(Customer.CustomerStatus.ACTIVE);

		// Create a list of customers for pagination testing
		customerList = new ArrayList<>();
		for (int i = 1; i <= 10; i++) {
			Customer customer = new Customer();
			customer.setCustomerId(i);
			customer.setCustomerName("Customer " + i);
			customer.setPhone("555000000" + i);
			customer.setEmail("customer" + i + "@example.com");
			customer.setStatus(Customer.CustomerStatus.ACTIVE);
			customerList.add(customer);
		}
	}

	@Test
	void testShowCustomerList() throws Exception {
		// Given
		Pageable pageable = PageRequest.of(0, 5);
		Page<Customer> customerPage = new PageImpl<>(customerList.subList(0, 5), pageable, customerList.size());
		given(customerRepository.findAll(any(Pageable.class))).willReturn(customerPage);

		// When & Then
		mockMvc.perform(get("/customers"))
			.andExpect(status().isOk())
			.andExpect(view().name("customers/customerList"))
			.andExpect(model().attributeExists("listCustomers"))
			.andExpect(model().attribute("currentPage", 1))
			.andExpect(model().attribute("totalPages", 2))
			.andExpect(model().attribute("totalItems", 10L))
			.andExpect(model().attribute("listCustomers", hasSize(5)));
	}

	@Test
	void testShowCustomerById() throws Exception {
		// Given
		given(customerRepository.findById(1)).willReturn(Optional.of(testCustomer));

		// When & Then
		mockMvc.perform(get("/customers/1"))
			.andExpect(status().isOk())
			.andExpect(view().name("customers/customerDetails"))
			.andExpect(model().attributeExists("customer"))
			.andExpect(model().attribute("customer", hasProperty("customerName", is("John Doe"))))
			.andExpect(model().attribute("customer", hasProperty("phone", is("5551234567"))))
			.andExpect(model().attribute("customer", hasProperty("email", is("john.doe@example.com"))));
	}

	@Test
	void testShowCustomerNotFound() throws Exception {
		// Given
		given(customerRepository.findById(anyInt())).willReturn(Optional.empty());

		// When & Then
		mockMvc.perform(get("/customers/999"))
			.andExpect(status().isNotFound());
	}

	@Test
	void testInitCreationForm() throws Exception {
		mockMvc.perform(get("/customers/new"))
			.andExpect(status().isOk())
			.andExpect(view().name("customers/createOrUpdateCustomerForm"))
			.andExpect(model().attributeExists("customer"));
	}

	@Test
	void testProcessCreationFormSuccess() throws Exception {
		mockMvc.perform(post("/customers/new")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("customerName", "Jane Smith")
				.param("phone", "5559876543")
				.param("email", "jane.smith@example.com")
				.param("status", "ACTIVE"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/customers"));
	}

	@Test
	void testProcessCreationFormWithErrors() throws Exception {
		// Missing required fields
		mockMvc.perform(post("/customers/new")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("customerName", "")  // Empty name - should fail
				.param("phone", "")         // Empty phone - should fail
				.param("email", "invalid-email"))  // Invalid email
			.andExpect(status().isOk())
			.andExpect(view().name("customers/createOrUpdateCustomerForm"))
			.andExpect(model().attributeHasFieldErrors("customer", "customerName"))
			.andExpect(model().attributeHasFieldErrors("customer", "phone"));
	}

	@Test
	void testInitUpdateForm() throws Exception {
		// Given
		given(customerRepository.findById(1)).willReturn(Optional.of(testCustomer));

		// When & Then
		mockMvc.perform(get("/customers/1/edit"))
			.andExpect(status().isOk())
			.andExpect(view().name("customers/createOrUpdateCustomerForm"))
			.andExpect(model().attributeExists("customer"))
			.andExpect(model().attribute("customer", hasProperty("customerName", is("John Doe"))));
	}

	@Test
	void testProcessUpdateFormSuccess() throws Exception {
		// Given
		given(customerRepository.findById(1)).willReturn(Optional.of(testCustomer));

		// When & Then
		mockMvc.perform(post("/customers/1/edit")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("customerName", "John Doe Updated")
				.param("phone", "5551234567")
				.param("email", "john.updated@example.com")
				.param("status", "ACTIVE"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/customers/1"));
	}

	@Test
	void testInitFindForm() throws Exception {
		mockMvc.perform(get("/customers/find"))
			.andExpect(status().isOk())
			.andExpect(view().name("customers/findCustomers"))
			.andExpect(model().attributeExists("customer"));
	}

	@Test
	void testProcessFindFormByName() throws Exception {
		// Given
		Pageable pageable = PageRequest.of(0, 5);
		List<Customer> searchResults = new ArrayList<>();
		searchResults.add(testCustomer);
		Page<Customer> customerPage = new PageImpl<>(searchResults, pageable, 1);
		given(customerRepository.findByNameContaining(any(), any(Pageable.class))).willReturn(customerPage);

		// When & Then
		mockMvc.perform(get("/customers/search")
				.param("customerName", "John"))
			.andExpect(status().isOk())
			.andExpect(view().name("customers/customerList"))
			.andExpect(model().attributeExists("listCustomers"))
			.andExpect(model().attribute("listCustomers", hasSize(1)));
	}

	@Test
	void testProcessFindFormNoResults() throws Exception {
		// Given
		Pageable pageable = PageRequest.of(0, 5);
		Page<Customer> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);
		given(customerRepository.findByNameContaining(any(), any(Pageable.class))).willReturn(emptyPage);

		// When & Then
		mockMvc.perform(get("/customers/search")
				.param("customerName", "NonexistentCustomer"))
			.andExpect(status().isOk())
			.andExpect(view().name("customers/findCustomers"))
			.andExpect(model().attribute("notFound", true))
			.andExpect(model().attribute("customerName", "NonexistentCustomer"));
	}

	@Test
	void testPhoneNumberValidation() throws Exception {
		// Test with invalid phone number (not 10 digits)
		mockMvc.perform(post("/customers/new")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("customerName", "Test Customer")
				.param("phone", "123")  // Invalid - not 10 digits
				.param("email", "test@example.com")
				.param("status", "ACTIVE"))
			.andExpect(status().isOk())
			.andExpect(view().name("customers/createOrUpdateCustomerForm"))
			.andExpect(model().attributeHasFieldErrors("customer", "phone"));
	}

}

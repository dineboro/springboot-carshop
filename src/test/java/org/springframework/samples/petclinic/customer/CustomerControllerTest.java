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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the {@link CustomerController}
 *
 * Tests follow the Arrange-Act-Assert (AAA) pattern:
 * - Arrange: Set up test data and mock behaviors
 * - Act: Execute the method under test
 * - Assert: Verify the expected outcomes
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
		// Arrange: Create a customer to be returned by the mocked repository
		customer = new Customer();
		customer.setCustomerId(TEST_CUSTOMER_ID);
		customer.setCustomerName("John Doe");
		customer.setPhone("5551234567");
		customer.setEmail("john.doe@example.com");
		customer.setStatus(Customer.CustomerStatus.ACTIVE);
	}

	// =========================================================================
	// TESTS FOR GET /customers (Customer List)
	// =========================================================================

	@Test
	@DisplayName("GET /customers should display paginated customer list")
	void testShowCustomerList() throws Exception {
		// Arrange: Create a "Page" of customers to mock the database response
		// Matches the 5 items per page logic in your controller
		Pageable pageable = PageRequest.of(0, 5);
		Page<Customer> customerPage = new PageImpl<>(List.of(customer), pageable, 1);
		given(this.customers.findAll(any(Pageable.class))).willReturn(customerPage);

		// Act & Assert: Perform the GET request and verify the results
		mockMvc.perform(get("/customers").param("page", "1"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("listCustomers"))
			.andExpect(model().attributeExists("totalPages"))
			.andExpect(model().attributeExists("currentPage"))
			.andExpect(view().name("customers/customerList"));
	}

	// =========================================================================
	// TESTS FOR GET /customers/new (Display Form)
	// =========================================================================

	@Test
	@DisplayName("GET /customers/new should display creation form with empty customer")
	void testInitCreationForm() throws Exception {
		// Act: User clicks "Add Customer" button
		mockMvc.perform(get("/customers/new"))
			// Assert: Form is displayed
			.andExpect(status().isOk())
			.andExpect(view().name("customers/createOrUpdateCustomerForm"))
			.andExpect(model().attributeExists("customer"));
	}

	// =========================================================================
	// TESTS FOR POST /customers/new - VALID INPUT (Success Cases)
	// =========================================================================

	@Test
	@DisplayName("POST /customers/new with VALID data should save and redirect")
	void testProcessCreationFormSuccess() throws Exception {
		// Arrange: Prepare valid customer data
		String validName = "Jane Smith";
		String validPhone = "5559876543";
		String validEmail = "jane.smith@example.com";

		// Act: Submit form with valid data
		mockMvc.perform(post("/customers/new")
				.param("customerName", validName)
				.param("phone", validPhone)
				.param("email", validEmail))
			// Assert: Should redirect to customer list
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/customers"));

		// Assert: Verify that repository.save() was called
		verify(customers).save(any(Customer.class));
	}

	@Test
	@DisplayName("POST /customers/new with VALID data (no email) should succeed")
	void testProcessCreationFormSuccessWithoutEmail() throws Exception {
		// Arrange: Valid data without email (email is optional)
		String validName = "Mike Johnson";
		String validPhone = "5551112222";

		// Act: Submit form without email
		mockMvc.perform(post("/customers/new")
				.param("customerName", validName)
				.param("phone", validPhone)
				.param("status", "ACTIVE"))
			// Assert: Should succeed
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/customers"));

		// Assert: Save was called
		verify(customers).save(any(Customer.class));
	}

	@Test
	@DisplayName("POST /customers/new with all fields valid should succeed")
	void testProcessCreationFormWithAllFieldsValid() throws Exception {
		// Arrange: Complete valid customer data
		String validName = "Sarah Williams";
		String validPhone = "5553334444";
		String validEmail = "sarah@example.com";
		String validStatus = "ACTIVE";

		// Act: Submit complete form
		mockMvc.perform(post("/customers/new")
				.param("customerName", validName)
				.param("phone", validPhone)
				.param("email", validEmail)
				.param("status", validStatus))
			// Assert: Should succeed
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/customers"));

		// Assert: Save was called once
		verify(customers).save(any(Customer.class));
	}

	// =========================================================================
	// TESTS FOR POST /customers/new - INVALID INPUT (Validation Failures)
	// =========================================================================

	@Test
	@DisplayName("POST /customers/new with EMPTY NAME should show validation error")
	void testProcessCreationFormWithEmptyName() throws Exception {
		// Arrange: Empty name (invalid)
		String emptyName = "";
		String validPhone = "5551234567";

		// Act: Submit form with empty name
		mockMvc.perform(post("/customers/new")
				.param("customerName", emptyName)
				.param("phone", validPhone))
			// Assert: Should return to form with errors
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("customer"))
			.andExpect(model().attributeHasFieldErrors("customer", "customerName"))
			.andExpect(view().name("customers/createOrUpdateCustomerForm"));

		// Assert: Save should NOT be called
		verify(customers, never()).save(any(Customer.class));
	}

	@Test
	@DisplayName("POST /customers/new with EMPTY PHONE should show validation error")
	void testProcessCreationFormWithEmptyPhone() throws Exception {
		// Arrange: Empty phone (invalid)
		String validName = "John Doe";
		String emptyPhone = "";

		// Act: Submit form with empty phone
		mockMvc.perform(post("/customers/new")
				.param("customerName", validName)
				.param("phone", emptyPhone))
			// Assert: Should return to form with errors
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("customer"))
			.andExpect(model().attributeHasFieldErrors("customer", "phone"))
			.andExpect(view().name("customers/createOrUpdateCustomerForm"));

		// Assert: Save should NOT be called
		verify(customers, never()).save(any(Customer.class));
	}

	@Test
	@DisplayName("POST /customers/new with BOTH NAME AND PHONE EMPTY should show multiple errors")
	void testProcessCreationFormHasErrors() throws Exception {
		// Arrange: Both required fields empty
		String emptyName = "";
		String emptyPhone = "";

		// Act: Submit form with multiple empty required fields
		mockMvc.perform(post("/customers/new")
				.param("customerName", emptyName)
				.param("phone", emptyPhone))
			// Assert: Should return to form with multiple errors
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("customer"))
			.andExpect(model().attributeHasFieldErrors("customer", "customerName"))
			.andExpect(model().attributeHasFieldErrors("customer", "phone"))
			.andExpect(view().name("customers/createOrUpdateCustomerForm"));

		// Assert: Save should NOT be called
		verify(customers, never()).save(any(Customer.class));
	}

	@Test
	@DisplayName("POST /customers/new with PHONE TOO SHORT should show validation error")
	void testProcessCreationFormWithPhoneTooShort() throws Exception {
		// Arrange: Phone with only 5 digits (invalid - needs 10)
		String validName = "John Doe";
		String shortPhone = "12345";

		// Act: Submit form with phone too short
		mockMvc.perform(post("/customers/new")
				.param("customerName", validName)
				.param("phone", shortPhone))
			// Assert: Should return to form with phone error
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("customer"))
			.andExpect(model().attributeHasFieldErrors("customer", "phone"))
			.andExpect(view().name("customers/createOrUpdateCustomerForm"));

		// Assert: Save should NOT be called
		verify(customers, never()).save(any(Customer.class));
	}

	@Test
	@DisplayName("POST /customers/new with PHONE TOO LONG should show validation error")
	void testProcessCreationFormWithPhoneTooLong() throws Exception {
		// Arrange: Phone with 11 digits (invalid - needs exactly 10)
		String validName = "John Doe";
		String longPhone = "12345678901";

		// Act: Submit form with phone too long
		mockMvc.perform(post("/customers/new")
				.param("customerName", validName)
				.param("phone", longPhone))
			// Assert: Should return to form with phone error
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("customer"))
			.andExpect(model().attributeHasFieldErrors("customer", "phone"))
			.andExpect(view().name("customers/createOrUpdateCustomerForm"));

		// Assert: Save should NOT be called
		verify(customers, never()).save(any(Customer.class));
	}

	@Test
	@DisplayName("POST /customers/new with PHONE WITH DASHES should show validation error")
	void testProcessCreationFormWithPhoneWithDashes() throws Exception {
		// Arrange: Phone in format 555-123-4567 (invalid - only digits allowed)
		String validName = "John Doe";
		String phoneWithDashes = "555-123-4567";

		// Act: Submit form with dashes in phone
		mockMvc.perform(post("/customers/new")
				.param("customerName", validName)
				.param("phone", phoneWithDashes))
			// Assert: Should return to form with phone error
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("customer"))
			.andExpect(model().attributeHasFieldErrors("customer", "phone"))
			.andExpect(view().name("customers/createOrUpdateCustomerForm"));

		// Assert: Save should NOT be called
		verify(customers, never()).save(any(Customer.class));
	}

	@Test
	@DisplayName("POST /customers/new with PHONE WITH SPACES should show validation error")
	void testProcessCreationFormWithPhoneWithSpaces() throws Exception {
		// Arrange: Phone with spaces "555 123 4567" (invalid)
		String validName = "John Doe";
		String phoneWithSpaces = "555 123 4567";

		// Act: Submit form with spaces in phone
		mockMvc.perform(post("/customers/new")
				.param("customerName", validName)
				.param("phone", phoneWithSpaces))
			// Assert: Should return to form with phone error
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("customer"))
			.andExpect(model().attributeHasFieldErrors("customer", "phone"))
			.andExpect(view().name("customers/createOrUpdateCustomerForm"));

		// Assert: Save should NOT be called
		verify(customers, never()).save(any(Customer.class));
	}

	@Test
	@DisplayName("POST /customers/new with PHONE WITH LETTERS should show validation error")
	void testProcessCreationFormWithPhoneWithLetters() throws Exception {
		// Arrange: Phone with letters "555ABC4567" (invalid - only digits allowed)
		String validName = "John Doe";
		String phoneWithLetters = "555ABC4567";

		// Act: Submit form with letters in phone
		mockMvc.perform(post("/customers/new")
				.param("customerName", validName)
				.param("phone", phoneWithLetters))
			// Assert: Should return to form with phone error
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("customer"))
			.andExpect(model().attributeHasFieldErrors("customer", "phone"))
			.andExpect(view().name("customers/createOrUpdateCustomerForm"));

		// Assert: Save should NOT be called
		verify(customers, never()).save(any(Customer.class));
	}

	@Test
	@DisplayName("POST /customers/new with INVALID EMAIL FORMAT should show validation error")
	void testProcessCreationFormWithInvalidEmail() throws Exception {
		// Arrange: Invalid email format
		String validName = "John Doe";
		String validPhone = "5551234567";
		String invalidEmail = "not-an-email";

		// Act: Submit form with invalid email
		mockMvc.perform(post("/customers/new")
				.param("customerName", validName)
				.param("phone", validPhone)
				.param("email", invalidEmail))
			// Assert: Should return to form with email error
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("customer"))
			.andExpect(view().name("customers/createOrUpdateCustomerForm"));

		// Assert: Save should NOT be called
		verify(customers, never()).save(any(Customer.class));
	}

	@Test
	@DisplayName("POST /customers/new with MULTIPLE INVALID FIELDS should show all errors")
	void testProcessCreationFormWithMultipleInvalidFields() throws Exception {
		// Arrange: Multiple invalid fields (empty name, short phone, bad email)
		String emptyName = "";
		String shortPhone = "123";
		String invalidEmail = "bad-email";

		// Act: Submit form with multiple validation errors
		mockMvc.perform(post("/customers/new")
				.param("customerName", emptyName)
				.param("phone", shortPhone)
				.param("email", invalidEmail))
			// Assert: Should return to form with all field errors
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("customer"))
			.andExpect(model().attributeHasFieldErrors("customer", "customerName"))
			.andExpect(model().attributeHasFieldErrors("customer", "phone"))
			.andExpect(view().name("customers/createOrUpdateCustomerForm"));

		// Assert: Save should NOT be called
		verify(customers, never()).save(any(Customer.class));
	}

	// =========================================================================
	// EDGE CASE TESTS
	// =========================================================================

	@Test
	@DisplayName("POST /customers/new with VERY LONG NAME should succeed")
	void testProcessCreationFormWithVeryLongName() throws Exception {
		// Arrange: Very long customer name (should be valid - no max length constraint)
		String veryLongName = "This Is An Extremely Long Customer Name That Goes On And On But Should Still Be Valid";
		String validPhone = "5551234567";

		// Act: Submit form with long name
		mockMvc.perform(post("/customers/new")
				.param("customerName", veryLongName)
				.param("phone", validPhone))
			// Assert: Should succeed
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/customers"));

		// Assert: Save was called
		verify(customers).save(any(Customer.class));
	}

	@Test
	@DisplayName("POST /customers/new with INACTIVE STATUS should succeed")
	void testProcessCreationFormWithInactiveStatus() throws Exception {
		// Arrange: Valid data with INACTIVE status
		String validName = "John Doe";
		String validPhone = "5551234567";

		// Act: Submit form with INACTIVE status
		mockMvc.perform(post("/customers/new")
				.param("customerName", validName)
				.param("phone", validPhone)
				.param("status", "INACTIVE"))
			// Assert: Should succeed (status can be any valid enum value)
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/customers"));

		// Assert: Save was called
		verify(customers).save(any(Customer.class));
	}

	@Test
	@DisplayName("POST /customers/new with SUSPENDED STATUS should succeed")
	void testProcessCreationFormWithSuspendedStatus() throws Exception {
		// Arrange: Valid data with SUSPENDED status
		String validName = "John Doe";
		String validPhone = "5551234567";

		// Act: Submit form with SUSPENDED status
		mockMvc.perform(post("/customers/new")
				.param("customerName", validName)
				.param("phone", validPhone)
				.param("status", "SUSPENDED"))
			// Assert: Should succeed
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/customers"));

		// Assert: Save was called
		verify(customers).save(any(Customer.class));
	}
}

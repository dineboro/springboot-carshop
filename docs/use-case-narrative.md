# Use Case Narrative: Manage User Profile

---

## Use Case ID
UC-001

## Use Case Name
Manage User Profile

## Actor
Registered User (logged-in customer or manager)

## Description
A logged-in user can view and update their profile information, including their email address, password, and personal details. They may also permanently delete their account.

## Preconditions
- The user has a registered account in the weFixcar system.
- The user is authenticated (logged in).
- The user navigates to `/users/profile`.

## Postconditions
- **Edit profile:** The user's updated information is saved to the database. A success flash message is displayed. If the email changed, the security session is updated automatically.
- **Delete account:** The user's `deleted_at` timestamp is set, their email is anonymized, and they are logged out. A farewell flash message is displayed on the home page.

---

## Main Flow — Edit Profile

| Step | Actor Action | System Response |
|------|-------------|-----------------|
| 1 | User clicks **Edit Profile** in the top navigation bar. | System routes the request to `GET /users/profile`. |
| 2 | — | System looks up the authenticated user by their email from the security context. |
| 3 | — | System pre-populates the profile form with the user's current data and renders `users/profile.html`. |
| 4 | User reviews and edits one or more fields (email, password, name, phone, preferred language). | — |
| 5 | User clicks **Save Changes**. | System receives the `POST /users/profile` request. |
| 6 | — | System validates input: checks email format, email uniqueness, and password strength (if a new password was entered). |
| 7 | — | System saves the updated data to the `users` table. If the email changed, the Spring Security session is refreshed. |
| 8 | — | System redirects back to `GET /users/profile` and displays a green success flash message: *"Your profile has been updated successfully."* |

---

## Alternative Flows — Edit Profile

### A1: Invalid Email Format
- At step 6, if the email field does not match a valid email pattern, the system re-displays the form with the error message: *"Please enter a valid email."*

### A2: Email Already Taken
- At step 6, if the new email address already belongs to another account, the system re-displays the form with the error message: *"This email is already taken."*

### A3: Weak Password
- At step 6, if the user entered a new password but it does not meet the strength requirements (minimum 8 characters, at least one uppercase, one lowercase, one digit), the system re-displays the form with the error message: *"Password must be at least 8 characters and must contain uppercase, lowercase, and number."*

---

## Main Flow — Delete Account

| Step | Actor Action | System Response |
|------|-------------|-----------------|
| 1 | User scrolls to the **Danger Zone** section at the bottom of the profile page. | — |
| 2 | User clicks **Delete Account**. | System displays a confirmation modal dialog. |
| 3 | User reads the warning and types their email address into the confirmation field. | System checks whether the typed text matches the user's current email. |
| 4 | User clicks **I understand, delete my account**. | System submits `POST /users/delete`. |
| 5 | — | System sets the user's `deleted_at` timestamp, anonymizes their email (prefixes with `deleted_<id>`), and saves the record. |
| 6 | — | System logs the user out and invalidates their session. |
| 7 | — | System redirects to the home page (`/`) with the flash message: *"Your account has been successfully deleted. We're sorry to see you go!"* |

---

## Alternative Flows — Delete Account

### B1: Confirmation Text Does Not Match
- At step 3, if the user's typed input does not exactly match their email address, the system displays an inline error: *"Email does not match. Please try again."* The form is not submitted.

### B2: User Cancels
- At step 2 or 3, if the user clicks **Cancel** or closes the modal, no action is taken and the user remains on the profile page.

---

## Business Rules
- A user may only edit their own profile; there is no cross-user editing from this use case.
- Account deletion is a soft delete: the record is retained in the database with a `deleted_at` timestamp and an anonymized email. The account cannot be recovered through the UI.
- If the preferred language is updated, the application redirects with the appropriate `?lang=` query parameter so the locale switcher takes effect immediately.

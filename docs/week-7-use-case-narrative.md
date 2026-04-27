# Use Case Narrative: Register a New User with a Default Role

---

## Use Case ID
UC-002

## Use Case Name
Register a New User with a Default Role

## Actor
Guest (unauthenticated visitor)

## Description
A guest visits the weFixcar application and fills out the staff registration form. The system validates their input, hashes their password, and persists a new user account with no role assigned and `isApproved = false`. An administrator must later approve the account and assign a role before the user can log in.

## Preconditions
- The user does not already have an account in the system.
- The user navigates to `/register`.

## Postconditions
- A new `User` record is saved to the `users` table with:
  - A bcrypt-hashed password stored in `password_hash`.
  - `is_active = true`, `is_approved = false`.
  - An empty role set (no entry in `user_roles`).
  - `created_at` and `updated_at` timestamps set by `@PrePersist`.
- The guest is redirected to `/login` with a success flash message informing them their account is pending admin approval.

---

## Main Flow

| Step | Actor Action | System Response |
|------|-------------|-----------------|
| 1 | Guest navigates to `/register`. | System handles `GET /register`, adds an empty `User` object to the model, and renders `auth/register.html`. |
| 2 | Guest fills in email, password, first name, and last name fields. | — |
| 3 | Guest clicks **Register**. | System handles `POST /register` and runs Bean Validation (`@Valid`) on the submitted `User` object. |
| 4 | — | System checks that no existing account shares the submitted email address. |
| 5 | — | `UserServiceImpl.registerNewUser()` encodes the password with `BCryptPasswordEncoder`. |
| 6 | — | Service sets `isActive = true`, `isApproved = false`, and assigns an empty role set. |
| 7 | — | `UserRepository.save(user)` inserts the record; `@PrePersist` sets `created_at` and `updated_at`. |
| 8 | — | Controller adds a flash message: *"Registration successful! Your account is pending admin approval."* and redirects to `GET /login`. |
| 9 | Guest sees the login page. | System displays the green success flash message. |

---

## Alternative Flows

### A1: Bean Validation Failure
- At step 3, if any field fails validation (empty email, invalid email format, missing password, password shorter than 8 characters, password missing uppercase/lowercase/digit), the system re-renders `auth/register.html` with inline field-level error messages. No user record is created.

### A2: Duplicate Email
- At step 4, if `UserRepository.existsByEmail()` returns `true`, `UserServiceImpl` throws a `RuntimeException`. The controller catches it, calls `result.rejectValue("email", "duplicateEmail", "This email address is already registered...")`, and re-renders the form with the error highlighted on the email field.

### A3: Database Error
- At step 7, if the database is unavailable, an unchecked exception propagates to Spring's default error handler and the user sees a generic 500 error page. No partial record is saved.

---

## Business Rules
- Passwords are never stored in plain text; only the bcrypt hash is persisted in `password_hash`.
- All staff accounts (`/register`) start with `isApproved = false`. No role is assigned at registration time — an administrator grants the role during the approval step.
- Student accounts (`/register-student`) follow a separate flow: they are auto-approved and immediately assigned the `STUDENT` role.
- Email addresses must be unique across the `users` table (`UNIQUE` constraint on the `email` column).

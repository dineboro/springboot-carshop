# Sequence Diagram: Register a New User with a Default Role

This diagram covers the staff registration flow (`POST /register`) including validation, password hashing, and persistence.

> Rendered automatically on GitHub. To export as PNG, paste the Mermaid code into [mermaid.live](https://mermaid.live).

---

```mermaid
sequenceDiagram
    actor Guest
    participant Browser
    participant AuthController
    participant UserServiceImpl
    participant PasswordEncoder
    participant UserRepository
    participant DB as Database

    Guest->>Browser: Navigate to /register
    Browser->>AuthController: GET /register
    AuthController->>Browser: Render auth/register.html (empty User form)
    Browser-->>Guest: Display registration form

    Guest->>Browser: Fill in email, password, first name, last name
    Guest->>Browser: Click "Register"
    Browser->>AuthController: POST /register (form data)

    AuthController->>AuthController: Run @Valid Bean Validation on User object

    alt Validation fails (missing/invalid fields)
        AuthController->>Browser: Re-render auth/register.html with field errors
        Browser-->>Guest: Show inline error messages
    else Validation passes
        AuthController->>UserServiceImpl: registerNewUser(user)

        UserServiceImpl->>UserRepository: existsByEmail(user.email)
        UserRepository->>DB: SELECT COUNT(*) FROM users WHERE email = ?
        DB-->>UserRepository: count
        UserRepository-->>UserServiceImpl: true / false

        alt Email already registered
            UserServiceImpl-->>AuthController: throws RuntimeException
            AuthController->>AuthController: result.rejectValue("email", "duplicateEmail", "...")
            AuthController->>Browser: Re-render auth/register.html with email field error
            Browser-->>Guest: Show "This email address is already registered" error
        else Email is unique
            UserServiceImpl->>PasswordEncoder: encode(rawPassword)
            PasswordEncoder-->>UserServiceImpl: bcryptHash

            UserServiceImpl->>UserServiceImpl: user.setPassword(bcryptHash)
            UserServiceImpl->>UserServiceImpl: user.setIsActive(true)
            UserServiceImpl->>UserServiceImpl: user.setIsApproved(false)
            UserServiceImpl->>UserServiceImpl: user.setRoles(emptySet)

            UserServiceImpl->>UserRepository: save(user)
            UserRepository->>DB: INSERT INTO users (email, password_hash, is_active, is_approved, created_at, ...) VALUES (...)
            Note over DB: @PrePersist sets created_at and updated_at
            DB-->>UserRepository: Saved User record
            UserRepository-->>UserServiceImpl: User (with generated id)
            UserServiceImpl-->>AuthController: User

            AuthController->>Browser: redirect:/login (flash "Registration successful! Pending admin approval.")
            Browser-->>Guest: Display login page with green success message
        end
    end
```

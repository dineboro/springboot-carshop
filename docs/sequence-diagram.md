# Sequence Diagram: Manage User Profile

This diagram covers two scenarios: **editing profile information** and **deleting the account**.

> Rendered automatically on GitHub. To export as PNG, paste the Mermaid code into [mermaid.live](https://mermaid.live).

---

## Edit Profile

```mermaid
sequenceDiagram
    actor User
    participant Browser
    participant ProfileController
    participant UserRepository
    participant SecurityContext
    participant DB as Database

    User->>Browser: Click "Edit Profile" in nav
    Browser->>ProfileController: GET /users/profile
    ProfileController->>SecurityContext: Get authenticated email (principal.getName())
    SecurityContext-->>ProfileController: email@example.com
    ProfileController->>UserRepository: findByEmail(email)
    UserRepository->>DB: SELECT * FROM users WHERE email = ?
    DB-->>UserRepository: User record
    UserRepository-->>ProfileController: User object
    ProfileController->>ProfileController: Format phone, compute school slug
    ProfileController->>Browser: Render users/profile.html (pre-filled form)
    Browser-->>User: Display edit profile page

    User->>Browser: Edit fields (email, password, name, etc.)
    User->>Browser: Click "Save Changes"
    Browser->>ProfileController: POST /users/profile (form data)

    ProfileController->>ProfileController: Validate email format (@Email)
    alt Email changed
        ProfileController->>UserRepository: existsByEmail(newEmail)
        UserRepository->>DB: SELECT COUNT(*) FROM users WHERE email = ?
        DB-->>UserRepository: count
        alt Email already taken
            UserRepository-->>ProfileController: true
            ProfileController->>Browser: Re-render form with error "This email is already taken"
            Browser-->>User: Show validation errors
        end
    end

    alt New password entered
        ProfileController->>ProfileController: Check password regex strength
        alt Password too weak
            ProfileController->>Browser: Re-render form with error "Password must contain uppercase, lowercase, and number"
            Browser-->>User: Show validation errors
        end
    end

    ProfileController->>ProfileController: Apply updates to current User entity
    alt Password changed
        ProfileController->>ProfileController: passwordEncoder.encode(newPassword)
    end
    ProfileController->>UserRepository: save(currentUser)
    UserRepository->>DB: UPDATE users SET ... WHERE id = ?
    DB-->>UserRepository: OK

    alt Email changed
        ProfileController->>SecurityContext: Replace authentication token with new email
    end

    ProfileController->>Browser: redirect:/users/profile (with flash "Profile updated successfully")
    Browser-->>User: Display profile page with green success message
```

---

## Delete Account

```mermaid
sequenceDiagram
    actor User
    participant Browser
    participant ProfileController
    participant UserRepository
    participant SecurityContext
    participant DB as Database

    User->>Browser: Click "Delete Account" button
    Browser-->>User: Show confirmation modal

    User->>Browser: Type email address into confirmation field
    Browser->>Browser: Compare input to expected email (client-side)

    alt Input does not match email
        Browser-->>User: Show inline error "Email does not match. Please try again."
        Note over User,Browser: Form is NOT submitted
    else Input matches email
        User->>Browser: Click "I understand, delete my account"
        Browser->>ProfileController: POST /users/delete

        ProfileController->>SecurityContext: Get authenticated email (principal.getName())
        SecurityContext-->>ProfileController: email@example.com
        ProfileController->>UserRepository: findByEmail(email)
        UserRepository->>DB: SELECT * FROM users WHERE email = ?
        DB-->>UserRepository: User record
        UserRepository-->>ProfileController: User object

        ProfileController->>ProfileController: Set deletedAt = now()
        ProfileController->>ProfileController: Anonymize email → "deleted_<id>email@example.com"
        ProfileController->>UserRepository: save(currentUser)
        UserRepository->>DB: UPDATE users SET deleted_at = ?, email = ? WHERE id = ?
        DB-->>UserRepository: OK

        ProfileController->>SecurityContext: Logout (SecurityContextLogoutHandler)
        SecurityContext-->>ProfileController: Session invalidated

        ProfileController->>Browser: redirect:/ (with flash "Account deleted. Sorry to see you go!")
        Browser-->>User: Display home page with farewell message
    end
```

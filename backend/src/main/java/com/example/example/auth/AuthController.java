package com.example.example.auth;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.example.auth.AuthDtos.AuthRequest;
import com.example.example.auth.AuthDtos.AuthResponse;
import com.example.example.auth.AuthDtos.RegisterRequest;
import com.example.example.auth.AuthDtos.UserResponse;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Authentication and user account APIs")
public class AuthController {
    private final AuthService authService;
    private final UserRepository users;

    public AuthController(AuthService authService, UserRepository users) {
        this.authService = authService;
        this.users = users;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Register user",
            description = "Creates a user account and returns the created user profile.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User registered"),
                    @ApiResponse(responseCode = "400", description = "Invalid registration payload")
            })
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login",
            description = "Authenticates with email and password, then returns a bearer token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login succeeded"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials")
            })
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get current user",
            description = "Returns the profile for the authenticated bearer token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Current user returned"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            })
    public UserResponse me(JwtAuthenticationToken authentication) {
        User user = users.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return UserResponse.from(user);
    }

    @GetMapping("/users")
    @Operation(
            summary = "List users",
            description = "Returns all user accounts ordered by id.",
            responses = @ApiResponse(responseCode = "200", description = "Users returned"))
    public List<UserResponse> listUsers() {
        return users.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete user",
            description = "Deletes a user account by id.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "User deleted"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            })
    public void deleteAccount(@PathVariable Long id, JwtAuthenticationToken authentication) {
        authService.deleteAccount(id, authentication.getName());
    }
}

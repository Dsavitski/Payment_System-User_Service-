package com.dsavitskiy.userservice.controller;

import com.dsavitskiy.userservice.dto.UserCreateDto;
import com.dsavitskiy.userservice.dto.UserDisplayDto;
import com.dsavitskiy.userservice.service.UserService;
import com.dsavitskiy.userservice.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDisplayDto> createUser(
        @Valid @RequestBody UserCreateDto userCreateDto) {
        UserDisplayDto createdUser = userService.createUser(userCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDisplayDto> getCurrentUser() {
        UUID userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(userService.findUserById(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDisplayDto> findUserById(@PathVariable UUID id) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();

        if (!SecurityUtil.isAdmin() && !id.equals(currentUserId)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }

        return ResponseEntity.ok(userService.findUserById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDisplayDto> updateUser(
        @PathVariable UUID id,
        @Valid @RequestBody UserCreateDto userCreateDto) {
        return ResponseEntity.ok(userService.updateUser(id, userCreateDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDisplayDto> activateUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.activateUser(id));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDisplayDto> deactivateUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.deactivateUser(id));
    }
}
package be.technifutur.erp_finalproject.controllers;


import be.technifutur.erp_finalproject.enums.UserRole;
import be.technifutur.erp_finalproject.models.dto_request.ChangePasswordRequest;
import be.technifutur.erp_finalproject.models.dto_request.UserRequest;
import be.technifutur.erp_finalproject.models.dto_request.UserUpdateRequest;
import be.technifutur.erp_finalproject.models.dto_response.UserResponse;
import be.technifutur.erp_finalproject.services.userservice.UserService;
import be.technifutur.erp_finalproject.utils.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<PagedModel<UserResponse>> search (
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false)UserRole role,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<UserResponse> page = userService.search(name, email, role, pageable)
                .map(UserResponse::from);

        return ResponseEntity.ok(new PagedModel<>(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(
            @PathVariable Long id
    ) {
        UserResponse response = UserResponse.from(userService.findById(id));

        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<Void> create(
            @Valid @RequestBody UserRequest request
    ) {
        Long id = userService.create(request.toForm());

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(uri).build();
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> archive(
            @PathVariable Long id
    ) {
        userService.archive(id);

        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal JwtUtils.UserSession user,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(user.id(), request.oldPassword(), request.newPassword());

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        userService.update(id, request.toForm());

        return ResponseEntity.noContent().build();
    }
}
package be.technifutur.erp_finalproject.controllers;

import be.technifutur.erp_finalproject.entities.User;
import be.technifutur.erp_finalproject.models.dto_request.LoginRequest;
import be.technifutur.erp_finalproject.models.dto_response.LoginResponse;
import be.technifutur.erp_finalproject.services.authservice.AuthService;
import be.technifutur.erp_finalproject.utils.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        User user = authService.login(request.email(), request.password());

        String token = jwtUtils.generateToken(user);

        LoginResponse response = new LoginResponse(token, user.getEmail(), user.getRole());

        return ResponseEntity.ok(response);

    }
}

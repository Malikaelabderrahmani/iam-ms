package com.example.demo.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.dto.login.AccessTokenResponseDTO;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthService;
import com.example.demo.service.KeycloakService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @GetMapping("/login")
    public ResponseEntity<AccessTokenResponseDTO> login(@RequestParam String username, @RequestParam String password) {
        return authService.login(username, password);

    }
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDTO userDTO) {
        return authService.register(userDTO);
    }
}

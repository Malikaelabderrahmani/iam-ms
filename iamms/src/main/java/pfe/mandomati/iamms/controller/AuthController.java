package pfe.mandomati.iamms.controller;

import pfe.mandomati.iamms.dto.UserDTO;
import pfe.mandomati.iamms.dto.login.AccessTokenResponseDTO;
import pfe.mandomati.iamms.repository.UserRepository;
import pfe.mandomati.iamms.service.AuthService;
import pfe.mandomati.iamms.service.KeycloakService;
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

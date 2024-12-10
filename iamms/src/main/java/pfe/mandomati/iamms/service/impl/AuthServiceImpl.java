package pfe.mandomati.iamms.service.impl;

import pfe.mandomati.iamms.dto.UserDTO;
import pfe.mandomati.iamms.dto.login.AccessTokenResponseDTO;
import pfe.mandomati.iamms.model.User;
import pfe.mandomati.iamms.repository.UserRepository;
import pfe.mandomati.iamms.service.AuthService;
import pfe.mandomati.iamms.service.KeycloakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final KeycloakService keycloakService;
    private final UserRepository userRepository;

    @Override
    public ResponseEntity<AccessTokenResponseDTO> login(String username, String password) {
        try {
            return keycloakService.login(username, password);
        } catch (Exception e) {
            log.error("Login failed for user: {}", username, e);
            throw new RuntimeException("Invalid login credentials or error during login", e);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<String> register(UserDTO userDTO) {
        try {
            ResponseEntity<String> response = keycloakService.registerUser(userDTO);
            if (response.getStatusCode().is2xxSuccessful()) {
                saveUserLocally(userDTO);
            }
            return response;
        } catch (Exception e) {
            log.error("Registration failed for user: {}", userDTO.getUsername(), e);
            throw new RuntimeException("Failed to register user", e);
        }
    }

    private void saveUserLocally(UserDTO userDTO) {
        String defaultRole = "ADMIN";
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setRole(defaultRole);
        userRepository.save(user);
    }
}
package pfe.mandomati.iamms.Service.Impl;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pfe.mandomati.iamms.Repository.UserRepository;
import pfe.mandomati.iamms.Dto.Login.AccessTokenResponseDto;
import pfe.mandomati.iamms.Dto.UserDto;
import pfe.mandomati.iamms.Model.User;
import pfe.mandomati.iamms.Service.AuthService;
import pfe.mandomati.iamms.Service.KeycloakService;
import pfe.mandomati.iamms.Model.Role;
import pfe.mandomati.iamms.Repository.RoleRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final KeycloakService keycloakService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public ResponseEntity<AccessTokenResponseDto> login(String username, String password) {
        try {
            return keycloakService.login(username, password);
        } catch (Exception e) {
            log.error("Login failed for user: {}", username, e);
            throw new RuntimeException("Invalid login credentials or error during login", e);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<String> register(UserDto userDTO) {
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

    private void saveUserLocally(UserDto userDTO) {
        //String defaultRole = "ADMIN";
        Role role = roleRepository.findByName(userDTO.getRole().getName())
        .orElseThrow(() -> new RuntimeException("Role not found"));
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setRole(role);
        user.setFirstName(userDTO.getFirstname());
        user.setLastName(userDTO.getLastname());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword())); // Si vous souhaitez stocker le mot de passe en local (vérifiez la politique de sécurité)
        user.setStatus(userDTO.isStatus()); // Statut de l'utilisateur
        user.setBirthDate(userDTO.getBirthDate());
        user.setAddress(userDTO.getAddress());
        user.setCity(userDTO.getCity());
        user.setCreatedAt(userDTO.getCreatedAt());
        userRepository.save(user);
    }
}

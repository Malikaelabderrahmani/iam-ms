package pfe.mandomati.iamms.Service.Impl;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
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
            // Authentifier l'utilisateur avec Keycloak
            ResponseEntity<AccessTokenResponseDto> keycloakResponse = keycloakService.login(username, password);
            
            if (!keycloakResponse.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Échec de l'authentification");
            }
    
            // Trouver l'utilisateur en base de données
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    
            // Récupérer le rôle en faisant la jointure avec la table Role
            String roleName = roleRepository.findById(user.getRole().getId())
                    .map(Role::getName)
                    .orElse("UNKNOWN_ROLE");
    
            // Construire la réponse avec le token et le rôle
            AccessTokenResponseDto responseDto = AccessTokenResponseDto.builder()
                    .accessToken(keycloakResponse.getBody().getAccessToken()) // Récupération du token Keycloak
                    .roleName(roleName)
                    .build();
    
            return ResponseEntity.ok(responseDto);
    
        } catch (Exception e) {
            log.error("Échec de connexion pour l'utilisateur : {}", username, e);
            throw new RuntimeException("Identifiants invalides ou erreur de connexion", e);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<String> register(UserDto userDTO) {
        try {
            ResponseEntity<String> response = keycloakService.registerUser(userDTO);
            if (response.getStatusCode().is2xxSuccessful()) {
                try {
                    User savedUser = saveUserLocally(userDTO);
                    String responseBody = String.format("User registered successfully with ID: %d", savedUser.getId());
                    return ResponseEntity.status(response.getStatusCode()).body(responseBody);
                } catch (Exception e) {
                    log.error("Failed to save user locally, deleting user from Keycloak: {}", userDTO.getUsername(), e);
                    keycloakService.deleteUserFromKeycloak(userDTO.getUsername());
                    throw new RuntimeException("Failed to save user locally, user deleted from Keycloak", e);
                }
            }
            return response;
        } catch (Exception e) {
            log.error("Registration failed for user: {}", userDTO.getUsername(), e);
            throw new RuntimeException("Failed to register user", e);
        }
    }

    private User saveUserLocally(UserDto userDTO) {
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
        return userRepository.save(user);
    }
}
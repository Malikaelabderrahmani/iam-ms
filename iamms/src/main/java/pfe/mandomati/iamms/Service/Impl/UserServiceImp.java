package pfe.mandomati.iamms.Service.Impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pfe.mandomati.iamms.Model.User;
import pfe.mandomati.iamms.Repository.UserRepository;
import pfe.mandomati.iamms.Service.UserService;
import pfe.mandomati.iamms.Service.KeycloakService;
import pfe.mandomati.iamms.Dto.UserDto;
import pfe.mandomati.iamms.Dto.UsersMsDto;
import pfe.mandomati.iamms.Exception.UserNotFoundException;
import pfe.mandomati.iamms.Exception.KeycloakException;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final KeycloakService keycloakService;

    @Override
    public List<User> getAllUsers() {
        try {
            return userRepository.findAll();
        } catch (Exception e) {
            throw new UserNotFoundException("Failed to retrieve users", e);
        }
    }

    @Override
    public ResponseEntity<UsersMsDto> getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        return ResponseEntity.ok(convertToDto(user));
    }


    @Override
    public ResponseEntity<UsersMsDto> getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with Username: " + username));
    
        return ResponseEntity.ok(convertToDto(user));
    }
    
    @Override
    @Transactional
    public ResponseEntity<String> editUser(String username, UserDto userDto) {
        // Rechercher l'utilisateur dans la base de données par nom d'utilisateur
        User existingUser = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

        try {
            // Mettre à jour l'utilisateur dans Keycloak
            keycloakService.updateUserInKeycloak(username, userDto);
        } catch (Exception e) {
            throw new KeycloakException("Failed to update user in Keycloak", e);
        }

        // Mettre à jour les informations de l'utilisateur dans la base de données
        existingUser.setFirstName(userDto.getFirstname());
        existingUser.setLastName(userDto.getLastname());
        existingUser.setUsername(userDto.getUsername());
        existingUser.setEmail(userDto.getEmail());
        userRepository.save(existingUser);

        return ResponseEntity.ok("User updated successfully !");
    }

    @Override
    @Transactional
    public ResponseEntity<String> deleteUser(String username) {
        // Rechercher l'utilisateur dans la base de données par nom d'utilisateur
        User existingUser = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

        try {
            // Supprimez l'utilisateur de Keycloak en utilisant l'ID
            keycloakService.deleteUserFromKeycloak(username);
        } catch (Exception e) {
            throw new KeycloakException("Failed to delete user from Keycloak", e);
        }

        // Supprimez l'utilisateur de la base de données
        userRepository.deleteById(existingUser.getId());

        return ResponseEntity.ok("User deleted successfully !");
    }

    @Override
    public ResponseEntity<List<UsersMsDto>> findAllByRoleName(String roleName) {
        List<User> users = userRepository.findAllByRoleName(roleName);

        if (users.isEmpty()) {
            throw new UserNotFoundException("Users not found with role: " + roleName);
        }
        
        List<UsersMsDto> usersMsDtos = users.stream()
                .map(user -> new UsersMsDto(
                    user.getId(), 
                    user.getUsername(),
                    user.getPassword(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.isStatus(),
                    user.getBirthDate(),
                    user.getAddress(),
                    user.getCity(),
                    user.getCreatedAt(),
                    new UsersMsDto.Role(user.getRole().getId(), user.getRole().getName())
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(usersMsDtos);
    }


    @Override
    public ResponseEntity<UsersMsDto> findByRoleNameAndId(String roleName, Long id) {
        User user = userRepository.findByRoleNameAndId(roleName, id);

        if (user == null) {
            throw new UserNotFoundException("User not found with ID: " + id);
        }

        return ResponseEntity.ok(convertToDto(user));
    }

    @Override
    public boolean checkUserExistsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public ResponseEntity<UsersMsDto> getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        UsersMsDto userDto = convertToDto(user);
        return ResponseEntity.ok(userDto);
    }

    public ResponseEntity<List<UsersMsDto>> getUsersByFirstnameAndLastname(String firstname, String lastname) {
        List<User> users = userRepository.findByFirstnameAndLastname(firstname, lastname);
        if (users.isEmpty()) {
            return ResponseEntity.status(404).body(Collections.emptyList());
        }
        List<UsersMsDto> userDtos = users.stream()
                .map(this::convertToDto)
                .toList();
        return ResponseEntity.ok(userDtos);
    }

    private UsersMsDto convertToDto(User user) {
        // Implémentez la conversion de User à UsersMsDto
        return new UsersMsDto(
            user.getId(),
            user.getUsername(),
            user.getPassword(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.isStatus(),
            user.getBirthDate(),
            user.getAddress(),
            user.getCity(),
            user.getCreatedAt(),
            new UsersMsDto.Role(user.getRole().getId(), user.getRole().getName())
        );
    }
}
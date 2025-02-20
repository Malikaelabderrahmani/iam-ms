package pfe.mandomati.iamms.Service.Impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pfe.mandomati.iamms.Model.User;
import pfe.mandomati.iamms.Repository.UserRepository;
import pfe.mandomati.iamms.Service.UserService;
import pfe.mandomati.iamms.Service.KeycloakService;
import pfe.mandomati.iamms.Dto.UserDto;
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
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
    }

    @Override
    @Transactional
    public User editUser(String username, UserDto userDto) {
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
        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void deleteUser(String username) {
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
    }
}
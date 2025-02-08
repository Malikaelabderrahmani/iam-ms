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
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    @Transactional
    public User editUser(Long id, UserDto userDto) {
        User existingUser = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        // Update user in Keycloak
        keycloakService.updateUserInKeycloak(id, userDto);
        // Update user in local database
        existingUser.setFirstName(userDto.getFirstname());
        existingUser.setLastName(userDto.getLastname());
        existingUser.setUsername(userDto.getEmail());
        existingUser.setEmail(userDto.getEmail());
        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        // Delete user from Keycloak
        keycloakService.deleteUserFromKeycloak(id);

        // Delete user from the database
        userRepository.deleteById(id);
    }
}

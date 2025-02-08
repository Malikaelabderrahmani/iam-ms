package pfe.mandomati.iamms.Service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import pfe.mandomati.iamms.Config.KeycloakConfig;
import pfe.mandomati.iamms.Repository.UserRepository;
import pfe.mandomati.iamms.Dto.Login.AccessTokenResponseDto;
import pfe.mandomati.iamms.Dto.UserDto;
import pfe.mandomati.iamms.Model.User;
import pfe.mandomati.iamms.Service.KeycloakService;
import pfe.mandomati.iamms.Model.Role;
import org.keycloak.admin.client.resource.UserResource;


@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakServiceImpl implements KeycloakService {

    private final UserRepository userRepository;
    private final KeycloakConfig keycloakConfig;
    private final LocalValidatorFactoryBean defaultValidator;
    //private final String defaultRole = "ADMIN";

    @Override
    public ResponseEntity<AccessTokenResponseDto> login(String username, String password) {
        try {
            AuthzClient authzClient = keycloakConfig.getAuthzClient();
            AccessTokenResponse response = authzClient.obtainAccessToken(username, password);
            return ResponseEntity.ok(AccessTokenResponseDto.toAccessTokenResponseDTO(response));
        } catch (Exception e) {
            log.error("Login failed for user: {}", username, e);
            throw new RuntimeException("Invalid login credentials or error during login", e);
        }
    }

    @Override
    public ResponseEntity<String> registerUser(UserDto userDTO) {
        //check if user already exists
        List<UserRepresentation> users;
        try {
            users = getUser(userDTO.getUsername());
        } catch (Exception e) {
            log.error("Failed to get user from Keycloak: {}", e.getMessage());
            throw new RuntimeException("Failed to get user from Keycloak", e);

        }
        if (!users.isEmpty()) {
            return ResponseEntity.status(409).body("User already exists");
        }

        try {
            UserRepresentation user = getUserRepresentation(userDTO);
            Response response = keycloakConfig.getKeycloakInstance()
                    .realm(keycloakConfig.getRealm())
                    .users()
                    .create(user);
            //if the user already exists
            if (response.getStatus() == 409) {
                return ResponseEntity.status(409).body("User already exists");
            }

            if (response.getStatus() != 201) {
                log.error("Failed to create user in Keycloak: {}", response.getStatusInfo());
                return ResponseEntity.status(500).body("Failed to create user in Keycloak");
            }

            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            assignRoleToUser(userId, userDTO.getRole().getName());
            //saveUserLocally(userDTO, defaultRole);

            return ResponseEntity.ok("User registered successfully");
        } catch (Exception e) {
            log.error("Registration failed for user: {}", userDTO.getUsername(), e);
            throw new RuntimeException("Failed to register user", e);
        }
    }

    private UserRepresentation getUserRepresentation(UserDto userDTO) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstname());
        user.setLastName(userDTO.getLastname());
        user.setEnabled(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(userDTO.getPassword());
        credential.setTemporary(false);
        user.setCredentials(Collections.singletonList(credential));

        return user;
    }

    public void assignRoleToUser(String userId, String roleName) {

            Keycloak keycloak = keycloakConfig.getInstance();
            RealmResource realmResource = keycloak.realm(keycloakConfig.getRealm());

            // Retrieve the client representation
            ClientRepresentation clientRepresentation =
                    realmResource.clients().findByClientId(keycloakConfig.getClientId()).get(0);

            // Get the role resource for the given roleName
            RoleResource roleResource = realmResource.clients()
                    .get(clientRepresentation.getId())
                    .roles()
                    .get(roleName);


            RoleRepresentation roleToAssign;
            try {
                roleToAssign = roleResource.toRepresentation();
            } catch (Exception e) {
                log.error("Role not found, Failed to assign role to user: {}", e.getMessage());
                throw new RuntimeException("Role not found,Failed to assign role to user", e);
            }
            // Assign the saved role representation to the user
            realmResource.users()
                    .get(userId)
                    .roles()
                    .clientLevel(clientRepresentation.getId())
                    .add(Collections.singletonList(roleToAssign));
    }

    @Override
    public List<UserRepresentation> getUser(String userName) {
        Keycloak keycloakInstance = keycloakConfig.getKeycloakInstance();
        String realm = keycloakConfig.getRealm();
        RealmResource realmResource = keycloakInstance.realm(realm);
        UsersResource usersResource = realmResource.users();
        return usersResource.search(userName);
    }


    private void saveUserLocally(UserDto userDTO, Role role) {
        User localUser = new User();
        localUser.setUsername(userDTO.getUsername());
        localUser.setEmail(userDTO.getEmail());
        localUser.setRole(role);
        userRepository.save(localUser);
    }

    @Override
    public void updateUserInKeycloak(Long id, UserDto userDto) {
        Keycloak keycloak = keycloakConfig.getInstance();
        UsersResource usersResource = keycloak.realm(keycloakConfig.getRealm()).users();
        UserRepresentation userRepresentation = usersResource.get(id.toString()).toRepresentation();
        userRepresentation.setEmail(userDto.getEmail());
        userRepresentation.setUsername(userDto.getUsername());
        userRepresentation.setFirstName(userDto.getFirstname());
        userRepresentation.setLastName(userDto.getLastname());
        usersResource.get(id.toString()).update(userRepresentation);
    }

    @Override
    public void deleteUserFromKeycloak(Long id) {
        Keycloak keycloak = keycloakConfig.getInstance();
        UsersResource usersResource = keycloak.realm(keycloakConfig.getRealm()).users();
        usersResource.delete(id.toString());
    }
}
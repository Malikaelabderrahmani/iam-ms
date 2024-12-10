package pfe.mandomati.iamms.service;

import pfe.mandomati.iamms.dto.UserDTO;
import pfe.mandomati.iamms.dto.login.AccessTokenResponseDTO;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface KeycloakService {
    ResponseEntity<AccessTokenResponseDTO> login(String username, String password);
    ResponseEntity<String> registerUser(UserDTO userDTO);
    List<UserRepresentation> getUser(String userName);


}
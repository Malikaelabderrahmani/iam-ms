package pfe.mandomati.iamms.Service;

import org.springframework.http.ResponseEntity;
import org.keycloak.representations.idm.UserRepresentation;
import pfe.mandomati.iamms.Dto.Login.AccessTokenResponseDto;
import pfe.mandomati.iamms.Dto.UserDto;
import java.util.List;

public interface KeycloakService{

    ResponseEntity<AccessTokenResponseDto> login(String username, String password);
    ResponseEntity<String> registerUser(UserDto userDTO);
    List<UserRepresentation> getUser(String userName);
}

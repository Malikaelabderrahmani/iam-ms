package pfe.mandomati.iamms.service;

import pfe.mandomati.iamms.dto.UserDTO;
import pfe.mandomati.iamms.dto.login.AccessTokenResponseDTO;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<AccessTokenResponseDTO> login(String username, String password);
    ResponseEntity<String> register(UserDTO userDTO);
}

package pfe.mandomati.iamms.Service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import pfe.mandomati.iamms.Dto.Login.AccessTokenResponseDto;
import pfe.mandomati.iamms.Dto.UserDto;

public interface AuthService{

    ResponseEntity<AccessTokenResponseDto> login(String username, String password);
    ResponseEntity<String> register(UserDto userDTO);
}

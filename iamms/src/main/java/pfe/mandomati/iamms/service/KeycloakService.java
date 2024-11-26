package com.example.demo.service;

import com.example.demo.dto.UserDTO;
import com.example.demo.dto.login.AccessTokenResponseDTO;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface KeycloakService {
    ResponseEntity<AccessTokenResponseDTO> login(String username, String password);
    ResponseEntity<String> registerUser(UserDTO userDTO);
    List<UserRepresentation> getUser(String userName);


}
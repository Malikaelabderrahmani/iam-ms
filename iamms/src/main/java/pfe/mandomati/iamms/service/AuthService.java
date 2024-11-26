package com.example.demo.service;

import com.example.demo.dto.UserDTO;
import com.example.demo.dto.login.AccessTokenResponseDTO;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<AccessTokenResponseDTO> login(String username, String password);
    ResponseEntity<String> register(UserDTO userDTO);
}

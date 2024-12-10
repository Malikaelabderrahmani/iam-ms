package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {
    private String username;

    private String lastname;

    private String firstname;

    private String email;

    private String phone;

    private String role;

    private String password;
}

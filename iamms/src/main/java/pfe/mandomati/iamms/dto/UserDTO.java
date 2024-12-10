package pfe.mandomati.iamms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {
    private String username;

    private String lastname;

    private String firstname;

    private String email;

    private String role;

    private String password;

    private boolean status = true;

    private String address;

    private LocalDate birthDate;

    private String city;

    private LocalDateTime createdAt;

}

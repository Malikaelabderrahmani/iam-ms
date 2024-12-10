package pfe.mandomati.iamms.dto;

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
}

package pfe.mandomati.iamms.Dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsersMsDto {

    private Long id;
    private String username;
    private String password;
    private String email;
    private String firstname;
    private String lastname;
    private boolean status;
    private LocalDate birthDate;
    private String address;
    private String city;
    private LocalDateTime createdAt;

    private Role role;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @SuperBuilder
    public static class Role {
        private Long id;
        private String name;
    }
}

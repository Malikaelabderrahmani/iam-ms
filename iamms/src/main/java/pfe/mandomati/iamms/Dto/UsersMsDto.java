package pfe.mandomati.iamms.Dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsersMsDto {

    private Long id;

    private String username;

    private String lastname;

    private String firstname;

    private String email;

    private String roleName;

    private String address;

    private LocalDate birthDate;

    private String city;

    private LocalDateTime createdAt;
}

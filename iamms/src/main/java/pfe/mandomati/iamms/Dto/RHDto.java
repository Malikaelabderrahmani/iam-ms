package pfe.mandomati.iamms.Dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;
import pfe.mandomati.iamms.Model.Role;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RHDto {

    private Long id;

    private String username;

    private String lastname;

    private String firstname;

    private String email;

    private Role role;

    private String password;

    private boolean status = true;

    private String address;

    private LocalDate birthDate;

    private String city;

    private LocalDateTime createdAt;

    private String cni;

    private LocalDate hireDate;

    private String cnssNumber;

    private String position;
}

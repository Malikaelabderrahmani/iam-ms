package pfe.mandomati.iamms.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pfe.mandomati.iamms.Dto.Login.AccessTokenResponseDto;
import pfe.mandomati.iamms.Dto.UserDto;
import pfe.mandomati.iamms.Service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public ResponseEntity<AccessTokenResponseDto> login(@RequestBody UserDto userDTO) {
        return authService.login(userDTO.getUsername(), userDTO.getPassword());

    }

    //@PreAuthorize("hasAnyRole('ADMIN', 'ROOT', 'RH')")
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDto userDTO) {
        return authService.register(userDTO);
    }
}

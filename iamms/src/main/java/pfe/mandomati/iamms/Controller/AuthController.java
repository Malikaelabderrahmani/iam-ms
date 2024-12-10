package pfe.mandomati.iamms.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pfe.mandomati.iamms.Dto.Login.AccessTokenResponseDto;
import pfe.mandomati.iamms.Dto.UserDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public ResponseEntity<AccessTokenResponseDto> login(@RequestParam String username, @RequestParam String password) {
        return authService.login(username, password);

    }
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDto userDTO) {
        return authService.register(userDTO);
    }
}

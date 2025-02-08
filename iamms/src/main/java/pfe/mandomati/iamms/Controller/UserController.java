package pfe.mandomati.iamms.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import pfe.mandomati.iamms.Model.User;
import pfe.mandomati.iamms.Service.UserService;

@RestController
@RequiredArgsConstructor
//@RequestPreAuthorize("haseRole('ROLE_ADMIN', 'ROLE_ROOT', 'ROLE_RH')")
@RequestMapping("auth/user")
public class UserController {
    
    @Autowired
    private final UserService userService;

    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("/edit/{id}")
    public User editUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        return userService.editUser(id, userDto);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
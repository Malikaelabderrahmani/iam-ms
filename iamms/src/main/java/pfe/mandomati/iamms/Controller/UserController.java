package pfe.mandomati.iamms.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import pfe.mandomati.iamms.Dto.UserDto;
import lombok.RequiredArgsConstructor;
import pfe.mandomati.iamms.Model.User;
import pfe.mandomati.iamms.Service.UserService;

@RestController
@RequiredArgsConstructor
//@PreAuthorize("hasAnyRole('ADMIN', 'ROOT', 'RH')")
@RequestMapping("auth/user")
public class UserController {
    
    @Autowired
    private final UserService userService;

    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("/edit/{username}")
    public User editUser(@PathVariable String username, @RequestBody UserDto userDto) {
        return userService.editUser(username, userDto);
    }

    @DeleteMapping("/delete/{username}")
    public void deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
    }
}
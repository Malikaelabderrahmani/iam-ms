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
import pfe.mandomati.iamms.Dto.UsersMsDto;
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

    @GetMapping("/get/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/role/{roleName}")
    public List<UsersMsDto> findAllByRoleName(@PathVariable String roleName) {
        if (roleName.equals("admin")) {
            return userService.findAllByRoleName("ADMIN");
        }
        else if (roleName.equals("teacher")) {
            return userService.findAllByRoleName("TEACHER");
        }
        else if (roleName.equals("student")) {
            return userService.findAllByRoleName("STUDENT");
        }
        else if (roleName.equals("parent")){
            return userService.findAllByRoleName("PARENT");
        }
        else {
            throw new RuntimeException("Role not found");
        }
    }

    @GetMapping("/role/{roleName}/{id}")
    public UsersMsDto findByRoleNameAndId(@PathVariable  String roleName, @PathVariable Long id) {
        if (roleName.equals("admin")) {
            return userService.findByRoleNameAndId("ADMIN", id);
        }
        else if (roleName.equals("teacher")) {
            return userService.findByRoleNameAndId("TEACHER", id);
        }
        else if (roleName.equals("student")) {
            return userService.findByRoleNameAndId("STUDENT", id);
        }
        else if (roleName.equals("parent")){
            return userService.findByRoleNameAndId("PARENT", id);
        }
        else {
            throw new RuntimeException("Role not found");
        }
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
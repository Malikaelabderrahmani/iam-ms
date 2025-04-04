package pfe.mandomati.iamms.Service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pfe.mandomati.iamms.Model.User;
import pfe.mandomati.iamms.Dto.UserDto;
import pfe.mandomati.iamms.Dto.UsersMsDto;

@Service
public interface UserService {
    
    List<User> getAllUsers();
    ResponseEntity<UsersMsDto> getUserById(Long id);
    ResponseEntity<UsersMsDto> getUserByUsername(String username);
    ResponseEntity<String> editUser(String username, UserDto userDto);
    ResponseEntity<String> deleteUser(String username);

    ResponseEntity<List<UsersMsDto>> findAllByRoleName(String roleName);
    ResponseEntity<UsersMsDto> findByRoleNameAndId(String roleName, Long id);

    boolean checkUserExistsByEmail(String email);

    ResponseEntity<UsersMsDto> getUserByEmail(String email);
    ResponseEntity<List<UsersMsDto>> getUsersByFirstnameAndLastname(String firstname, String lastname);
    
}
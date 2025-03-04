package pfe.mandomati.iamms.Service;

import java.util.List;

import org.springframework.stereotype.Service;
import pfe.mandomati.iamms.Model.User;
import pfe.mandomati.iamms.Dto.UserDto;
import pfe.mandomati.iamms.Dto.UsersMsDto;

@Service
public interface UserService {
    
    List<User> getAllUsers();
    User getUserById(Long id);
    User editUser(String username, UserDto userDto);
    void deleteUser(String username);

    List<UsersMsDto> findAllByRoleName(String roleName);
    UsersMsDto findByRoleNameAndId(String roleName, Long id);
    
}
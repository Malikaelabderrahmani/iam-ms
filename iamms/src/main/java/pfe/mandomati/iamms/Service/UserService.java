package pfe.mandomati.iamms.Service;

import java.util.List;

import org.springframework.stereotype.Service;
import pfe.mandomati.iamms.Model.User;
import pfe.mandomati.iamms.Dto.UserDto;

@Service
public interface UserService {
    
    List<User> getAllUsers();
    User getUserById(Long id);
    User editUser(Long id, UserDto userDto);
    void deleteUser(String username, Long id);
    
}

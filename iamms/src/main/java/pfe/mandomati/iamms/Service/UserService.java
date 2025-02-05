package pfe.mandomati.iamms.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import pfe.mandomati.iamms.Model.User;

@Service
public interface UserService {
    
    List<User> getAllUsers();
}

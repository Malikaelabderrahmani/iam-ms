package pfe.mandomati.iamms.Service.Impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pfe.mandomati.iamms.Model.User;
import pfe.mandomati.iamms.Repository.UserRepository;
import pfe.mandomati.iamms.Service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService {

    @Autowired
    private final UserRepository userRepository;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    
}

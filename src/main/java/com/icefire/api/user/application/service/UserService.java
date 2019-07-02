package com.icefire.api.user.application.service;

import com.icefire.api.common.application.exception.UserNotCreatedException;
import com.icefire.api.common.infrastructure.security.MyKeyGenerator;
import com.icefire.api.user.application.dto.UserDTO;
import com.icefire.api.user.domain.model.User;
import com.icefire.api.user.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserAssembler userAssembler;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserDTO getUserDTO(String userName) {
        Optional<User> userEntity = userRepository.findByUsername(userName);
        return userEntity.map(user -> userAssembler.toResource(user)).orElse(null);
    }

    public User getUser(String userName) {
        Optional<User> userEntity = userRepository.findByUsername(userName);
        return userEntity.orElse(null);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User getUser(Long id) {
        Optional<User> userEntity = userRepository.findById(id);
        return userEntity.orElse(null);
    }

    public UserDTO addUser(UserDTO userDTO) throws UserNotCreatedException {

        if (getUser(userDTO.getUsername()) != null) {
            throw new UserNotCreatedException("User already exists!");
        }

        User userEntity;
        try {
            User user = new User();
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            user.setUsername(userDTO.getUsername());

            SecretKey secretKey = MyKeyGenerator.keyGenerator();
            byte[] iv = MyKeyGenerator.generatorIV();

            user.setPublicKey(MyKeyGenerator.keyPairGenerator(user.getUsername(), secretKey, iv));
            userEntity = userRepository.save(user);
        } catch (Exception e) {
            throw new UserNotCreatedException(userDTO, e.getCause());
        }

        return userAssembler.toResource(userEntity);
    }

}

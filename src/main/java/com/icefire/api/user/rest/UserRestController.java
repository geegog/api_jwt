package com.icefire.api.user.rest;

import com.icefire.api.common.application.exception.UserNotCreatedException;
import com.icefire.api.user.application.dto.UserDTO;
import com.icefire.api.user.application.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/user")
public class UserRestController {

    private static Logger logger = LoggerFactory.getLogger(UserRestController.class);
    @Autowired
    UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> transfer(@RequestBody UserDTO userDTO) {
        if (StringUtils.isEmpty(userDTO.getUsername()) || StringUtils.isEmpty(userDTO.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Username or Password cannot be empty");
        }
        try {
            return new ResponseEntity<>(userService.addUser(userDTO), HttpStatus.CREATED);
        } catch (UserNotCreatedException e) {
            logger.error("User not created {}, {}", userDTO, e);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

}

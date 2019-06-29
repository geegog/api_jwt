package com.icefire.api.common.application.exception;

import com.icefire.api.user.application.dto.UserDTO;

public class UserNotCreatedException extends Exception  {
    private static final long serialVersionUID = 1L;

    public UserNotCreatedException(){
        super();
    }

    public UserNotCreatedException(String message){
        super(message);
    }

    public UserNotCreatedException(UserDTO userDTO, Throwable cause){
        super(String.format("User not created! (User id: %d)", userDTO.get_id()), cause);
    }

    public UserNotCreatedException(Long id, Throwable cause) {
        super(String.format("User not created! (User id: %d)", id), cause);
    }

    public UserNotCreatedException(String username, Throwable cause) {
        super(String.format("User not created! (User id: %s)", username), cause);
    }
}

package com.icefire.api.common.application.exception;

import com.icefire.api.user.application.dto.UserDTO;

public class UserNotFoundException extends Exception  {
    private static final long serialVersionUID = 1L;

    public UserNotFoundException(){
        super();
    }
    public UserNotFoundException(UserDTO userDTO, Throwable cause){
        super(String.format("User id not found! (User id: %d)", userDTO.get_id()), cause);
    }
    public UserNotFoundException(Long id, Throwable cause) {
        super(String.format("User not found! (User id: %d)", id), cause);
    }

    public UserNotFoundException(String username, Throwable cause) {
        super(String.format("User not found! (User id: %s)", username), cause);
    }
}

package com.icefire.api.common.application.service;

import com.icefire.api.user.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        Optional<com.icefire.api.user.domain.model.User> optionalUser = userRepository.findByUsername(userName);
        if(!optionalUser.isPresent()) {
            throw new UsernameNotFoundException("User not found");
        }

        return new User(optionalUser.get().getUsername(), optionalUser.get().getPassword(), Collections.emptySet());
    }

}

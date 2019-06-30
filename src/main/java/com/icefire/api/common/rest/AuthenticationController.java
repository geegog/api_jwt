package com.icefire.api.common.rest;


import com.icefire.api.common.application.service.MyUserDetailsService;
import com.icefire.api.common.application.dto.AuthResult;
import com.icefire.api.common.infrastructure.security.JwtConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin
public class AuthenticationController {

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private MyUserDetailsService myUserDetailsService;

    private static Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @RequestMapping(value = "/refresh", method = RequestMethod.GET)
    public ResponseEntity<?> refreshAndGetAuthenticationToken(HttpServletRequest request) {
        String authToken = request.getHeader(jwtConfig.getHeader());
        final String token = authToken.replace(jwtConfig.getPrefix(),"");
        String username = jwtConfig.getUsernameFromToken(token);
        User user = (User) myUserDetailsService.loadUserByUsername(username);

        if (jwtConfig.canTokenBeRefreshed(token)) {
            AuthResult result = new AuthResult();
            result.setError(false);
            result.setMessage("Login successful!");
            String refreshedToken = jwtConfig.refreshToken(token);
            return ResponseEntity
                    .ok()
                    .header(jwtConfig.getHeader(), jwtConfig.getPrefix() + refreshedToken).body(result);
        } else {
            return ResponseEntity.badRequest().body(null);
        }
    }

}
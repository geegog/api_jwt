package com.icefire.api.user.application.service;

import com.icefire.api.information.application.dto.DataDTO;
import com.icefire.api.information.rest.RecordRestController;
import com.icefire.api.user.application.dto.UserDTO;
import com.icefire.api.user.domain.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Base64;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Service
public class UserAssembler extends ResourceAssemblerSupport<User, UserDTO> {

    @Autowired
    private UserAssembler userAssembler;

    public UserAssembler() {
        super(UserService.class, UserDTO.class);
    }


    @Override
    public UserDTO toResource(User user) {
        if (user == null)
            return null;
        UserDTO dto = instantiateResource(user);
        dto.set_id(user.getId());
        dto.setCreated(user.getCreated());
        dto.setPassword(user.getPassword());
        dto.setUsername(user.getUsername());
        dto.setUpdated(user.getUpdated());
        if (user.getPublicKey() != null)
            dto.setPublicKey(Base64.getEncoder().encodeToString(user.getPublicKey()));

        dto.add(linkTo(methodOn(RecordRestController.class)
                .allUserRecords(dto.get_id())).withRel("records").withType(HttpMethod.GET.toString()));

        dto.add(linkTo(methodOn(RecordRestController.class)
                .encrypt(new DataDTO())).withRel("encrypt").withType(HttpMethod.POST.toString()));

        return dto;
    }
}

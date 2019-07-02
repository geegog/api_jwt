package com.icefire.api.information.application.service;

import com.icefire.api.information.application.dto.DataDTO;
import com.icefire.api.information.application.dto.RecordDTO;
import com.icefire.api.information.domain.model.Record;
import com.icefire.api.information.rest.RecordRestController;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Service
public class RecordAssembler extends ResourceAssemblerSupport<Record, RecordDTO> {

    public RecordAssembler() {
        super(RecordService.class, RecordDTO.class);
    }


    @Override
    public RecordDTO toResource(Record record) {
        if (record == null)
            return null;
        RecordDTO dto = instantiateResource(record);
        dto.set_id(record.getId());
        dto.setCreated(record.getCreated());
        dto.setUpdated(record.getUpdated());
        dto.setValue(record.getValue());

        dto.add(linkTo(methodOn(RecordRestController.class)
                .allUserRecords(record.getUser().getUsername())).withRel("records").withType(HttpMethod.GET.toString()));

        dto.add(linkTo(methodOn(RecordRestController.class)
                .getARecord(dto.get_id())).withSelfRel().withType(HttpMethod.GET.toString()));

        dto.add(linkTo(methodOn(RecordRestController.class)
                .encrypt(new DataDTO())).withRel("encrypt").withType(HttpMethod.POST.toString()));

        dto.add(linkTo(methodOn(RecordRestController.class)
                .encrypt(new DataDTO(), dto.get_id())).withRel("encrypt_update").withType(HttpMethod.POST.toString()));

        dto.add(linkTo(methodOn(RecordRestController.class)
                .decrypt(new DataDTO(), dto.get_id())).withRel("decrypt").withType(HttpMethod.POST.toString()));

        return dto;
    }
}

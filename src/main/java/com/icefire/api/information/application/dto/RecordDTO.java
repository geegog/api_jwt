package com.icefire.api.information.application.dto;

import com.icefire.api.common.rest.ResourceSupport;
import lombok.*;

import java.time.LocalDateTime;

@Data
public class RecordDTO extends ResourceSupport {

    private Long _id;

    private LocalDateTime created;

    private LocalDateTime updated;

    private String value;
}

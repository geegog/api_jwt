package com.icefire.api.common.application.exception;

import com.icefire.api.information.application.dto.RecordDTO;

public class RecordNotFoundException extends Exception  {
    private static final long serialVersionUID = 1L;

    public RecordNotFoundException(){
        super();
    }
    public RecordNotFoundException(RecordDTO recordDTO, Throwable cause){
        super(String.format("Record not found! (Record id: %d)", recordDTO.get_id()), cause);
    }
    public RecordNotFoundException(Long id, Throwable cause) {
        super(String.format("Record not found! (Record id: %d)", id), cause);
    }
}

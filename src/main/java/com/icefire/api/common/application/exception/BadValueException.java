package com.icefire.api.common.application.exception;

public class BadValueException extends Exception  {
    private static final long serialVersionUID = 1L;

    public BadValueException(){
        super(String.format("Value not found!"));
    }
}

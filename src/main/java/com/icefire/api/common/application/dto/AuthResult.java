package com.icefire.api.common.application.dto;

import lombok.Data;

@Data
public class AuthResult {

	private boolean error;
	private String message;
}

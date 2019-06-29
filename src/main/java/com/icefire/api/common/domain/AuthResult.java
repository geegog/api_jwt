package com.icefire.api.common.domain;

import lombok.Data;

@Data
public class AuthResult {

	private boolean error;
	private String message;
}

package com.caldevsupplychain.common.ws;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ErrorWS {
	private String code;
	private String message;
}
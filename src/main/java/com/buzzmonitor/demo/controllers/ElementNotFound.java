package com.buzzmonitor.demo.controllers;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "element not found")
public class ElementNotFound extends RuntimeException {
	private static final long serialVersionUID = 1L;
}
package com.buzzmonitor.demo.resources;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.buzzmonitor.demo.BuzzProjectApplication;

@RestController
@RequestMapping(value = "/")
public class HomeResource {
	@GetMapping
	public String getHello() {
		return "Hello World!";
	}
}

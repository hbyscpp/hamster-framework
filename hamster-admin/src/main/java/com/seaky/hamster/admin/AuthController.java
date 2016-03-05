package com.seaky.hamster.admin;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class AuthController {
	private static HttpHeaders headers =new HttpHeaders();;
	static
	{
		headers.setContentType(MediaType.TEXT_PLAIN);
	}
	@RequestMapping("/currentUserName")
	public ResponseEntity<String> currentUserName(@AuthenticationPrincipal User user) {
		return new ResponseEntity<String>(user.getUsername(),headers,HttpStatus.OK);
	}
	
}

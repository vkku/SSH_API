package com.example.sshconnection.controller;

import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.sshconnection.service.SSHConnection;

@RestController
public class SSHController {
	
	@Autowired
	SSHConnection sshConnection;

    @GetMapping("/ssh")
    public String helloSpringWeb(@RequestParam String userName,
    		@RequestParam String password, @RequestParam String connectionIP, @RequestParam(required = false) String knownHostsFileName) {
    	
    	JSONObject userJSON = new JSONObject();
    	String validKnownHostsFileName = Optional.ofNullable(knownHostsFileName).orElse("");
    	try {
    		List<String> result = sshConnection.getUniqueUsers(userName, password, connectionIP, validKnownHostsFileName);
    		userJSON.put("users", result);
    	}
    	catch (Exception e) {
			e.printStackTrace();
		}
    	

        return userJSON.toString();

    }

}

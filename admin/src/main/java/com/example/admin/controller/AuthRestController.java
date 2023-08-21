package com.example.admin.controller;

import com.example.admin.model.UserInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthRestController {

    private final static Map<String, UserInfo> userTokens = new HashMap<>();

    static {
        userTokens.put("111", new UserInfo(UUID.randomUUID(), "Ivan Ivanov"));
    }

    @GetMapping("/token")
    public ResponseEntity<UserInfo> validateToken(@RequestParam String token) {

        if (userTokens.containsKey(token)) {
            return new ResponseEntity<>(userTokens.get(token), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

}

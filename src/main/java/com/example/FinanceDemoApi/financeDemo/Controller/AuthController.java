package com.example.FinanceDemoApi.financeDemo.Controller;

import com.example.FinanceDemoApi.financeDemo.Model.TokenRequest;
import com.example.FinanceDemoApi.financeDemo.Model.UserDto;
import com.example.FinanceDemoApi.financeDemo.Service.AuthService;
import com.example.FinanceDemoApi.financeDemo.Utility.ApiResponseStructure;



import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;



    public AuthService getAuthService() {
        return authService;
    }


    @PostMapping("/google")
    public ResponseEntity<?> googleAuth(@RequestBody TokenRequest tokenRequest) {
        String idTokenString = tokenRequest.getIdTokenString();
        return authService.googleSignIn(idTokenString);
    }

    @PostMapping(value = "/register")
    public ResponseEntity<?> registration(@RequestBody UserDto userDto) {
        System.out.println(userDto);
        return authService.registration(userDto);
    }








}

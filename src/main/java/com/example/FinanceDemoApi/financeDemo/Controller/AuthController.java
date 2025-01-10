package com.example.FinanceDemoApi.financeDemo.Controller;
import com.example.FinanceDemoApi.financeDemo.Model.*;
import com.example.FinanceDemoApi.financeDemo.Service.AuthService;
import com.example.FinanceDemoApi.financeDemo.Utility.ApiResponse;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

        return authService.registration(userDto);
    }

    @PostMapping(value = "/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenDto refreshTokenDto) {
        return authService.refreshToken(refreshTokenDto);
    }



    @ExceptionHandler(HttpMessageNotReadableException.class) public ResponseEntity<ApiResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) { ApiResponse apiResponse = new ApiResponse("Bad Request: Request body is missing or malformed.");
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }


}

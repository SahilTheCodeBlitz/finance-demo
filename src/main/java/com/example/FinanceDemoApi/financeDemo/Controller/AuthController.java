package com.example.FinanceDemoApi.financeDemo.Controller;
import com.example.FinanceDemoApi.financeDemo.Dto.RefreshTokenDto;
import com.example.FinanceDemoApi.financeDemo.Dto.TokenRequestDto;
import com.example.FinanceDemoApi.financeDemo.Dto.UserDto;
import com.example.FinanceDemoApi.financeDemo.Service.AuthService;
import jakarta.validation.Valid;
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
    public ResponseEntity<?> googleAuth(@RequestBody TokenRequestDto tokenRequest) {

        return authService.googleSignIn(tokenRequest);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registration(@Valid @RequestBody UserDto userDto) {
        return authService.registration(userDto);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenDto refreshTokenDto) {
        return authService.refreshToken(refreshTokenDto);
    }
}

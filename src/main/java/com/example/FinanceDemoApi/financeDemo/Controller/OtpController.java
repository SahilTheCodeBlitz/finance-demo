package com.example.FinanceDemoApi.financeDemo.Controller;
import com.example.FinanceDemoApi.financeDemo.Dto.OtpDto;
import com.example.FinanceDemoApi.financeDemo.Dto.OtpVerificationDto;
import com.example.FinanceDemoApi.financeDemo.Service.OtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/otp")
public class OtpController {
    private final OtpService otpService;
    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }
    @PostMapping("/generate")
    public ResponseEntity<?> generateOtp(@RequestBody OtpDto otpDto){
        return otpService.generateToken(otpDto);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody OtpVerificationDto otpVerificationDto) {
        return otpService.verifyToken(otpVerificationDto);
    }

}

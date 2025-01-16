package com.example.FinanceDemoApi.financeDemo.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpVerificationDto {
    @NotBlank(message="Otp must be provided")
    private String otp;
    @NotBlank(message ="Id must be provided")
    private Long id;
}

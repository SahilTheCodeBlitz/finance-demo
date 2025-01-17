package com.example.FinanceDemoApi.financeDemo.Dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class OtpDto {

    @NotBlank(message = "Email should be provided")
    String email;

    @NotBlank(message = "Phone should be provided")
    String phone;
}

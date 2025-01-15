package com.example.FinanceDemoApi.financeDemo.Dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class TokenRequestDto {

    private String idTokenString;
}

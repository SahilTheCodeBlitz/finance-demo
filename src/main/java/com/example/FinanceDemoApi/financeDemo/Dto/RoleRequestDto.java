package com.example.FinanceDemoApi.financeDemo.Dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class RoleRequestDto {

    private String role;
}

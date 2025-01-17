package com.example.FinanceDemoApi.financeDemo.Dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class RoleRequestDto {
    @NotBlank(message = "Role must be provided")
    private String role;
}

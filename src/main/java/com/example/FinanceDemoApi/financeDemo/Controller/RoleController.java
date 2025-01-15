package com.example.FinanceDemoApi.financeDemo.Controller;
import com.example.FinanceDemoApi.financeDemo.Dto.RoleRequestDto;
import com.example.FinanceDemoApi.financeDemo.Service.RoleService;
import com.example.FinanceDemoApi.financeDemo.Utility.ApiResponse;
import com.example.FinanceDemoApi.financeDemo.Utility.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/v1/role")
public class RoleController {
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    public RoleController(JwtTokenUtil jwtTokenUtil, RoleService roleService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.roleService = roleService;
    }
    @Autowired
    private RoleService roleService;
    @PutMapping("/updateRole")
    public ResponseEntity<?> updateRole(@RequestHeader("Authorization") String tokenHeader, @RequestBody RoleRequestDto roleRequestDto) {
        String role = roleRequestDto.getRole();
        return roleService.updateRole(tokenHeader, role.toLowerCase());
    }
    @ExceptionHandler(HttpMessageNotReadableException.class) public ResponseEntity<ApiResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) { ApiResponse apiResponse = new ApiResponse("Bad Request: Request body is missing or malformed.");
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }
}

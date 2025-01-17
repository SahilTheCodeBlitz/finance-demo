package com.example.FinanceDemoApi.financeDemo.Controller;
import com.example.FinanceDemoApi.financeDemo.Dto.RoleRequestDto;
import com.example.FinanceDemoApi.financeDemo.Service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/v1/role")
public class RoleController {
    private final RoleService roleService;
    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }
    @PutMapping("/updateRole")
    public ResponseEntity<?> updateRole(@RequestHeader("Authorization") String tokenHeader, @RequestBody RoleRequestDto roleRequestDto) {
        String role = roleRequestDto.getRole();
        return roleService.updateRole(tokenHeader, role.toLowerCase());
    }
}

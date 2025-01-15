package com.example.FinanceDemoApi.financeDemo.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/v1")
public class ResourceController {
    @GetMapping("/free/resource1")
    public String freeResource1() {
        return "Free Resource 1";
    }
    @GetMapping("/free/resource2")
    public String freeResource2() {
        return "Free Resource 2";
    }
    @GetMapping("/premium/resource1")
    public String premiumResource1() {
        return "Premium Resource 1";
    }
    @GetMapping("/premium/resource2")
    public String premiumResource2() {
        return "Premium Resource 2";
    }
    @GetMapping("/premium/resource3")
    public String premiumResource3() {
        return "Premium Resource 3";
    }
}

package com.example.FinanceDemoApi.financeDemo.Controller;
import com.example.FinanceDemoApi.financeDemo.Service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/v1/test/")
public class TestController {
    @Autowired
    private TestService testService;
    @GetMapping("test1")
    public String test1(){
        return testService.method1();
    }
    @GetMapping("test2")
    public String test2(){
        return testService.method2();
    }
    @GetMapping("test3")
    public String test3(){
        return testService.method3();
    }
    @GetMapping("test4")
    public String test4(){
        return testService.method4();
    }
}

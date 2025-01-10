package com.example.FinanceDemoApi.financeDemo.Service;

import com.example.FinanceDemoApi.financeDemo.Utility.JwtTokenUtil;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    public String method1(){
        return "This is the test method1";
    }

    public String method2(){
        return "This is the test method2";
    }

    public String method3(){
        return "This is the test method 3 available without authentication";
    }

    public String method4(){
        return "This is the test method 4 available without authentication";
    }



}

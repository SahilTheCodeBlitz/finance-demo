package com.example.FinanceDemoApi.financeDemo.Exception;

import org.springframework.boot.autoconfigure.security.SecurityProperties;

public class UserAlreadyExist extends RuntimeException{

    public UserAlreadyExist(String m){
        super(m);
    }
}

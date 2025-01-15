package com.example.FinanceDemoApi.financeDemo.Exception;
public class UserAlreadyExist extends RuntimeException{
    public UserAlreadyExist(String m){
        super(m);
    }
}

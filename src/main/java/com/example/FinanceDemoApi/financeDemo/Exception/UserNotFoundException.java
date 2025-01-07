package com.example.FinanceDemoApi.financeDemo.Exception;

public class UserNotFoundException extends RuntimeException{

    public  UserNotFoundException(String message){
        super(message);
    }
}

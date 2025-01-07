package com.example.FinanceDemoApi.financeDemo.Exception;

public class InvalidInputException extends RuntimeException{

    public InvalidInputException(String message){
        super(message);
    }
}

package com.example.FinanceDemoApi.financeDemo.Exception;

public class InvalidTokenException extends RuntimeException{
    public InvalidTokenException(String message) {
        super(message);
    }
}

package com.example.FinanceDemoApi.financeDemo.Utility;

public class ApiResponseStructure<T> {
    private int status;
    private String message;
    private  T data;

    public ApiResponseStructure(int status, String message, T data){
        this.status = status;
        this.message = message;
        this.data = data;
    }
}

package com.example.FinanceDemoApi.financeDemo.Exception;
import com.example.FinanceDemoApi.financeDemo.Controller.AuthController;
import com.example.FinanceDemoApi.financeDemo.Utility.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class AuthExceptionHandler {
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        ApiResponse apiResponse = new ApiResponse("Bad Request: Request body is missing or malformed.");
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }
}

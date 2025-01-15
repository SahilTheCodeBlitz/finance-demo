package com.example.FinanceDemoApi.financeDemo.Utility;
import com.google.gson.Gson;
import lombok.Data;

@Data
public class WrapperClass {
    private String firstName;
    private String lastName;
    private  String phoneNumber;
    private String  email;
    private Long userId;
    private String role;


    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}

package com.example.FinanceDemoApi.financeDemo.Utility;
import com.google.gson.Gson;
import lombok.Data;
@Data
public class ResponseWrapper {
    private String firstName;
    private String lastName;
    private String phone;
    private String email;

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}

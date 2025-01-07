package com.example.FinanceDemoApi.financeDemo.Model;

import com.google.gson.Gson;

public class WrapperClass {

    private String firstName;

    private String lastName;

    private  String phoneNumber;

    private String  email;

    private Long userId;

    public String getFirstName(String firstName) {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName(String lastName) {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber(String contactInfo) {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail(String contactInfo) {
        return email;
    }

//    @Override
//    public String toString() {
//        return "{" +
//                "firstName='" + firstName + '\'' +
//                ", lastName='" + lastName + '\'' +
//                ", phoneNumber='" + phoneNumber + '\'' +
//                ", email='" + email + '\'' +
//                ", userId=" + userId +
//                '}';
//    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getUserId(Long primaryKey) {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}

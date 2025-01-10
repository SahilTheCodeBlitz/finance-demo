package com.example.FinanceDemoApi.financeDemo.Utility;

public class UserPrincipals {

    private final String email;
    private final String firstName;
    private final String lastName;
    private final String phoneNumber;

    private final Long userId;
    public UserPrincipals(String email, String firstName, String lastName, String phoneNumber,Long userId) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Long getUserId(){
        return userId;
    }



}

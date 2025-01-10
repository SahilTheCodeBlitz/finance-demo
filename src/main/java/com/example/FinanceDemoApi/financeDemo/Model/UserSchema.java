package com.example.FinanceDemoApi.financeDemo.Model;
import com.example.FinanceDemoApi.financeDemo.Enums.Roles;
import jakarta.persistence.*;

@Entity
@Table(name = "userss")
public class UserSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long primaryKey;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "created_date", nullable = false)
    private Long createdDate;


    @Column(name = "role", nullable = false)
    private String role = "free"; // Default role is "FREE"

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Getters and Setters
    public Long getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(Long primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Long getCreatedDate(){
        return createdDate;
    }

    public void setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
    }







}
package com.example.FinanceDemoApi.financeDemo.Model;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "userss")
@Data
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
    private String role = "free"; // Default role is "free"

}
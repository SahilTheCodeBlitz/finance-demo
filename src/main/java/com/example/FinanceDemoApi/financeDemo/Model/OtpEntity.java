package com.example.FinanceDemoApi.financeDemo.Model;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.boot.autoconfigure.security.SecurityProperties;

import java.time.LocalDateTime;

@Entity
@Data
public class OtpEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userId")
    private UserSchema userId;
    private String otp;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;


}

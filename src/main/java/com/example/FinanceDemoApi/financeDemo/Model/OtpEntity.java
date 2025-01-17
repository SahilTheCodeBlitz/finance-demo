package com.example.FinanceDemoApi.financeDemo.Model;
import com.example.FinanceDemoApi.financeDemo.Converter.OtpAttributeConverter;
import jakarta.persistence.*;
import lombok.Data;
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

    @Convert(converter = OtpAttributeConverter.class)
    private String otp;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

}

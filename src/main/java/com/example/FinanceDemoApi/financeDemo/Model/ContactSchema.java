package com.example.FinanceDemoApi.financeDemo.Model;
import com.example.FinanceDemoApi.financeDemo.Utility.ContactType;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "contact")
@Data
public class ContactSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uniqueId;

    @ManyToOne
    @JoinColumn(name = "userId")
    private UserSchema userSchema;

    @Column(nullable = false, unique = true)
    String contactInfo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactType contactType;

}

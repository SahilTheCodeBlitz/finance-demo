package com.example.FinanceDemoApi.financeDemo.Model;

import com.example.FinanceDemoApi.financeDemo.Utility.ContactType;
import jakarta.persistence.*;


@Entity
@Table(name = "contact")
public class ContactSchema {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uniqueId;
    @ManyToOne
    @JoinColumn(name = "userId") // Foreign key column
    private UserSchema userSchema;

    @Column(nullable = false, unique = true)
    String contactInfo;

    @Enumerated(EnumType.STRING)  // Store enum as a string in the database
    @Column(nullable = false)     // Ensure contactType is not null
    private ContactType contactType;  // Using the enum type here

    public UserSchema getUserId() {
        return userSchema;
    }

    public void setUserID(UserSchema userSchema) {
        this.userSchema = userSchema;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public ContactType getContactType() {
        return contactType;
    }

    public void setContactType(ContactType contactType) {
        this.contactType = contactType;
    }

    public Long getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(Long uniqueId) {
        this.uniqueId = uniqueId;
    }

}

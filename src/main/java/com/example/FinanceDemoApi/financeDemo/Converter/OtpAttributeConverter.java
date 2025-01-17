package com.example.FinanceDemoApi.financeDemo.Converter;
import com.example.FinanceDemoApi.financeDemo.Utility.EncryptionUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Component
@Converter
public class OtpAttributeConverter implements AttributeConverter<String,String> {
    private final EncryptionUtil encryptionUtil;
    @Autowired
    public OtpAttributeConverter(EncryptionUtil encryptionUtil) {
        this.encryptionUtil = encryptionUtil;
    }
    @Override
    public String convertToDatabaseColumn(String otp) {
        return encryptionUtil.encrypt(otp);
    }
    @Override
    public String convertToEntityAttribute(String encryptedOtp) {
        return encryptionUtil.decrypt(encryptedOtp);
    }
}

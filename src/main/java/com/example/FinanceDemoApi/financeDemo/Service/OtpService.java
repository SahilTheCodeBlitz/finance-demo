package com.example.FinanceDemoApi.financeDemo.Service;
import com.example.FinanceDemoApi.financeDemo.Dto.OtpDto;
import com.example.FinanceDemoApi.financeDemo.Dto.OtpVerificationDto;
import com.example.FinanceDemoApi.financeDemo.Model.ContactSchema;
import com.example.FinanceDemoApi.financeDemo.Model.OtpEntity;
import com.example.FinanceDemoApi.financeDemo.Repository.ContactRepository;
import com.example.FinanceDemoApi.financeDemo.Repository.OtpEntityRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class OtpService {
    private final ContactRepository contactRepository;
    private final OtpEntityRepository otpEntityRepository;

    public OtpService(ContactRepository contactRepository, OtpEntityRepository otpEntityRepository) {
        this.contactRepository = contactRepository;
        this.otpEntityRepository = otpEntityRepository;
    }
    public ResponseEntity<Object> generateToken(OtpDto otpDto) {

        // Validate that either email or phone is provided
        if (otpDto.getEmail() == null && otpDto.getPhone() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Optional<ContactSchema> contactSchema = Optional.empty();

        // Search for the contact based on provided phone or email
        if (otpDto.getPhone() != null && otpDto.getEmail() == null) {
            contactSchema = contactRepository.findByContactInfo(otpDto.getPhone());
        } else if (otpDto.getEmail() != null && otpDto.getPhone() == null) {
            contactSchema = contactRepository.findByContactInfo(otpDto.getEmail());
        } else {
            // If both email and phone are provided, search by email
            contactSchema = contactRepository.findByContactInfo(otpDto.getEmail());
        }

        // If no user is found with the provided phone or email
        if (contactSchema.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Generate a 6-digit OTP
        String plainOtp = String.format("%06d", new SecureRandom().nextInt(999999));


        // Find existing OTP entry for the user, if exists
        Optional<OtpEntity> existingOtpEntity = otpEntityRepository.findByUserId(contactSchema.get().getUserSchema());

        OtpEntity otpEntity;

        if (existingOtpEntity.isPresent()) {
            // If OTP already exists for this user, update it with the new hashed OTP and timestamp
            otpEntity = existingOtpEntity.get();
            otpEntity.setOtp(plainOtp);
            otpEntity.setTimestamp(LocalDateTime.now()); // Set current timestamp
        } else {
            // If no OTP entry exists for this user, create a new OTP entity
            otpEntity = new OtpEntity();
            otpEntity.setOtp(plainOtp);
            otpEntity.setUserId(contactSchema.get().getUserSchema());
            otpEntity.setTimestamp(LocalDateTime.now()); // Set current timestamp
        }

        // Save  OTP in the database
        otpEntityRepository.save(otpEntity);

        // Return success response with plain OTP
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of(
                        "status", "success",
                        "message", "OTP generated successfully",
                        "otp", plainOtp, // Return plain OTP here
                        "id", otpEntity.getId(),
                        "timestamp", otpEntity.getTimestamp()
                ));
    }

    public ResponseEntity<Object> verifyToken(OtpVerificationDto otpVerificationDto) {

        // Check if the OTP entry exists
        Optional<OtpEntity> otpEntityOptional = otpEntityRepository.findById(otpVerificationDto.getId());

        if (otpEntityOptional.isEmpty()) {
            // If the provided ID does not exist
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Retrieve the OTP entity
        OtpEntity otpEntity = otpEntityOptional.get();

        // Check if the OTP has expired (e.g., after 300 seconds)
        if (Duration.between(otpEntity.getTimestamp(), LocalDateTime.now()).getSeconds() > 300) {
            // If OTP has expired
            return ResponseEntity.status(HttpStatus.GONE).build();

        }

        // Hash the user-provided OTP for comparison
        try {
            // Validate the OTP by comparing hashes
            if (otpEntity.getOtp().equals(otpVerificationDto.getOtp())) {
                // If the OTP matches
                return ResponseEntity.status(HttpStatus.OK).build();
            } else {
                // If the OTP does not match
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

            }
        } catch (Exception e) {
            // Handle hashing failure
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        }
    }
}

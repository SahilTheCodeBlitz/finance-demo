package com.example.FinanceDemoApi.financeDemo.Service;
import com.example.FinanceDemoApi.financeDemo.Dto.RefreshTokenDto;
import com.example.FinanceDemoApi.financeDemo.Dto.TokenRequestDto;
import com.example.FinanceDemoApi.financeDemo.Dto.UserDto;
import com.example.FinanceDemoApi.financeDemo.Exception.InvalidTokenException;
import com.example.FinanceDemoApi.financeDemo.Exception.UserAlreadyExist;
import com.example.FinanceDemoApi.financeDemo.Model.*;
import com.example.FinanceDemoApi.financeDemo.Repository.ContactRepository;
import com.example.FinanceDemoApi.financeDemo.Repository.UserRepository;
import com.example.FinanceDemoApi.financeDemo.Utility.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;

@Service
@Transactional
public class AuthService {
    @Value("${app.google.clientId}")
    private  String CLIENT_ID;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    public AuthService(UserRepository userRepository,ContactRepository contactRepository,JwtTokenUtil jwtTokenUtil) {
        this.userRepository=userRepository;
        this.contactRepository=contactRepository;
        this.jwtTokenUtil=jwtTokenUtil;
    }

    public ResponseEntity<?> googleSignIn(TokenRequestDto tokenRequestDto) {
        try {
            // Check if the token is null or empty
            if (tokenRequestDto.getIdTokenString() == null || tokenRequestDto.getIdTokenString().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse("Bad Request: ID token is missing or empty."));
            }

            // Verify the token
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), jsonFactory)
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();
            GoogleIdToken idToken = verifier.verify(tokenRequestDto.getIdTokenString().trim());


            // Check if the token is valid
            if (idToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse("Unauthorized: Token is invalid or expired"));
            }

            // Extract payload
            GoogleIdToken.Payload payload = idToken.getPayload();

            // Check for existing user
            Optional<ContactSchema> contactSchemaEmail = contactRepository.findByContactInfo((String) payload.get("email"));
            Optional<ContactSchema> contactSchemaPhone = contactRepository.findByContactInfo((String) payload.get("phoneNumber"));

            if (contactSchemaEmail.isPresent()) {
                // Process existing email user
                Optional<UserSchema> userSchema = Optional.ofNullable(contactSchemaEmail.get().getUserSchema());

                WrapperClass wrapperClass = new WrapperClass();
                wrapperClass.setUserId(userSchema.get().getPrimaryKey());
                wrapperClass.setEmail(contactSchemaEmail.get().getContactInfo());
                wrapperClass.setFirstName(userSchema.get().getFirstName());
                wrapperClass.setLastName(userSchema.get().getLastName());
                wrapperClass.setRole(userSchema.get().getRole());

                // Find the phone number for the same user
                Optional<ContactSchema> phone = contactRepository.findByUserSchema_PrimaryKeyAndContactType(
                        userSchema.get().getPrimaryKey(),
                        ContactType.PHONE
                );

                // Set phone number if available
                phone.ifPresent(contact -> wrapperClass.setPhoneNumber(contact.getContactInfo()));

                return ResponseEntity.ok()
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateToken(wrapperClass))
                        .header("Refresh-Token", jwtTokenUtil.generateRefreshToken(wrapperClass))
                        .body(wrapperClass.toString());

            } else if (contactSchemaPhone.isPresent()) {
                // Process existing phone user

                Optional<UserSchema> userSchema  = Optional.ofNullable(contactSchemaPhone.get().getUserSchema());

                WrapperClass wrapperClass = new WrapperClass();
                wrapperClass.setUserId(userSchema.get().getPrimaryKey());
                wrapperClass.setFirstName(userSchema.get().getFirstName());
                wrapperClass.setLastName(userSchema.get().getLastName());
                wrapperClass.setPhoneNumber(contactSchemaPhone.get().getContactInfo());
                wrapperClass.setRole(userSchema.get().getRole());

                // Find the email for the same user
                Optional<ContactSchema> email = contactRepository.findByUserSchema_PrimaryKeyAndContactType(
                        userSchema.get().getPrimaryKey(),
                        ContactType.EMAIL
                );

                // Set phone number if available
                email.ifPresent(contact -> wrapperClass.setPhoneNumber(contact.getContactInfo()));

                return ResponseEntity.ok()
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateToken(wrapperClass))
                        .header("Refresh-Token", jwtTokenUtil.generateRefreshToken(wrapperClass))
                        .body(wrapperClass.toString());

            } else {
                // User not registered
                ResponseWrapper responseWrapper = new ResponseWrapper();
                responseWrapper.setEmail((String) payload.get("email"));
                responseWrapper.setPhone((String) payload.get("phoneNumber"));
                responseWrapper.setFirstName((String) payload.get("given_name"));
                responseWrapper.setLastName((String) payload.get("family_name"));

                return new ResponseEntity<>(responseWrapper, HttpStatus.NOT_FOUND);
            }
        }
        catch (GeneralSecurityException | IOException e) {
            // Handle exceptions thrown during verification
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse("Unauthorized: Token verification failed."));
        }
        catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse("Unauthorized: Token is invalid or expired"));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("An unexpected error occurred."));
        }
    }

    @Transactional
    public ResponseEntity<?> registration(UserDto userDto) {
        try {

            // Check if user exists in the database with this email or phone
            Optional<ContactSchema> contactOptionalEmail = contactRepository.findByContactInfo(userDto.getEmail());
            Optional<ContactSchema> contactOptionalPhone = contactRepository.findByContactInfo(userDto.getPhone());

            WrapperClass wrapperClass = new WrapperClass();

            if (contactOptionalEmail.isEmpty() && contactOptionalPhone.isEmpty()) {
                // User does not exist, create a new user
                UserSchema userSchema = new UserSchema();
                userSchema.setFirstName(userDto.getFirstName());
                userSchema.setLastName(userDto.getLastName());
                userSchema.setCreatedDate(System.currentTimeMillis());
                userSchema = userRepository.save(userSchema);
                // Save email and phone contact
                saveContact(userSchema, userDto.getEmail(), ContactType.EMAIL);
                saveContact(userSchema, userDto.getPhone(), ContactType.PHONE);
                // Prepare response
                wrapperClass.setUserId(userSchema.getPrimaryKey());
                wrapperClass.setEmail(userDto.getEmail());
                wrapperClass.setFirstName(userSchema.getFirstName());
                wrapperClass.setLastName(userSchema.getLastName());
                wrapperClass.setPhoneNumber(userDto.getPhone());
                wrapperClass.setRole(userSchema.getRole());

                return ResponseEntity.status(HttpStatus.CREATED)
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateToken(wrapperClass))
                        .header("Refresh-Token", jwtTokenUtil.generateRefreshToken(wrapperClass))
                        .body(wrapperClass.toString());

            } else if (contactOptionalEmail.isPresent() && contactOptionalPhone.isEmpty()) {
                // User with the same email already registered
                return new ResponseEntity<>(new ApiResponse("User with this email already registered"), HttpStatus.CONFLICT);

            } else if (contactOptionalPhone.isPresent() && contactOptionalEmail.isEmpty()) {
                // User with the same phone already registered
                return new ResponseEntity<>(new ApiResponse("User with this phone already registered"), HttpStatus.CONFLICT);

            } else {
                // User exists with both email and phone, fetch details
                Optional<UserSchema> existingUserSchema = Optional.ofNullable(contactOptionalEmail.get().getUserSchema());
                Optional<ContactSchema> contactSchemaPhone = contactRepository.findByContactInfo(userDto.getPhone());

                wrapperClass.setUserId(existingUserSchema.get().getPrimaryKey());
                wrapperClass.setEmail(contactOptionalEmail.get().getContactInfo());
                wrapperClass.setFirstName(existingUserSchema.get().getFirstName());
                wrapperClass.setLastName(existingUserSchema.get().getLastName());
                wrapperClass.setPhoneNumber(contactSchemaPhone.get().getContactInfo());
                wrapperClass.setRole(existingUserSchema.get().getRole());

                return ResponseEntity.status(HttpStatus.OK)
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateToken(wrapperClass))
                        .header("Refresh-Token", jwtTokenUtil.generateRefreshToken(wrapperClass))
                        .body(wrapperClass.toString());
            }
        } catch (UserAlreadyExist e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse("Error occurred while processing the request"), HttpStatus.UNAUTHORIZED);
        }
    }
    // Helper method to save contact information
    private void saveContact(UserSchema userSchema, String contactInfo, ContactType contactType) {
        ContactSchema contact = new ContactSchema();
        contact.setUserSchema(userSchema);
        contact.setContactInfo(contactInfo);
        contact.setContactType(contactType);
        contactRepository.save(contact);
    }

    // method for refreshing the access  token when it is expired
    public ResponseEntity<?> refreshToken(RefreshTokenDto refreshTokenDto) {
        // Validate that the token is provided in the request body
        if (refreshTokenDto == null || refreshTokenDto.getToken() == null || refreshTokenDto.getToken().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse("Refresh token is missing or invalid."));
        }
        try {
            // Parse and validate the token in a single step
            Claims claims = jwtTokenUtil.parseAndValidateToken(refreshTokenDto.getToken());

            // Generate a new access token
            WrapperClass wrapper = new WrapperClass();
            wrapper.setUserId(claims.get("uniqueId", Double.class).longValue());
            wrapper.setFirstName(claims.get("firstName", String.class));
            wrapper.setLastName(claims.get("lastName", String.class));
            wrapper.setEmail(claims.getSubject());
            wrapper.setRole(claims.get("role", String.class));
            wrapper.setPhoneNumber(claims.get("phoneNumber", String.class));

            return ResponseEntity.ok()
                    .header("Authorization", "Bearer " + jwtTokenUtil.generateToken(wrapper))
                    .header("Refresh-Token", refreshTokenDto.getToken())
                    .body(new ApiResponse("Access token refreshed successfully"));

        } catch (ExpiredJwtException ex) {
            // Handle token expiration specifically
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse("Refresh token has expired. Please login again."));

        } catch (MalformedJwtException | SignatureException ex) {
            // Handle invalid token structure or signature issues
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse("Invalid token format or signature."));

        } catch (Exception ex) {
            // General exception handling
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse("Invalid or expired refresh token."));
        }
    }
}

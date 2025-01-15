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
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.LoggerFactory;
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
import org.slf4j.Logger;
@Service
@Transactional
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    @Value("${app.google.clientId}")
    private  String CLIENT_ID;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    public ResponseEntity<?> googleSignIn(TokenRequestDto tokenRequestDto) {
        try {
            // Check if the token is null or empty
            String idTokenString = tokenRequestDto.getIdTokenString();

            if (idTokenString == null || idTokenString.trim().isEmpty()) {
                ApiResponse apiResponse = new ApiResponse("Bad Request: ID token is missing or empty.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
            }


            // Verify the token
            idTokenString = idTokenString.trim();
            HttpTransport transport = new NetHttpTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();
            GoogleIdToken idToken = verifier.verify(idTokenString);


            // Check if the token is valid
            if (idToken == null) {
                ApiResponse apiResponse = new ApiResponse("Unauthorized: Token is invalid or expired");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
            }

            // Extract payload
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = (String) payload.get("email");
            String phone = (String) payload.get("phoneNumber");

            // Check for existing user
            Optional<ContactSchema> contactSchemaEmail = contactRepository.findByContactInfo(email);
            Optional<ContactSchema> contactSchemaPhone = contactRepository.findByContactInfo(phone);

            if (contactSchemaEmail.isPresent()) {
                // Process existing email user
                return processExistingUser(contactSchemaEmail.get());
            } else if (contactSchemaPhone.isPresent()) {
                // Process existing phone user
                return processExistingUser(contactSchemaPhone.get());
            } else {
                // User not registered
                ResponseWrapper responseWrapper = new ResponseWrapper();
                responseWrapper.setEmail(email);
                responseWrapper.setPhone(phone);
                responseWrapper.setFirstName((String) payload.get("given_name"));
                responseWrapper.setLastName((String) payload.get("family_name"));

                return new ResponseEntity<>(responseWrapper, HttpStatus.NOT_FOUND);
            }
        }
        catch (GeneralSecurityException | IOException e) {
            // Handle exceptions thrown during verification
            ApiResponse apiResponse = new ApiResponse("Unauthorized: Token verification failed.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
        }
        catch (InvalidTokenException e) {
            ApiResponse apiResponse = new ApiResponse("Unauthorized: Token is invalid or expired");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
        }
        catch (Exception e) {
            ApiResponse apiResponse = new ApiResponse("Unauthorized: Token verification failed.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
        }
    }

    private ResponseEntity<?> processExistingUser(ContactSchema contactSchema) {
        UserSchema userSchema = contactSchema.getUserSchema();

        WrapperClass wrapperClass = new WrapperClass();
        wrapperClass.setUserId(userSchema.getPrimaryKey());
        wrapperClass.setEmail(contactSchema.getContactInfo());
        wrapperClass.setFirstName(userSchema.getFirstName());
        wrapperClass.setLastName(userSchema.getLastName());
        wrapperClass.setPhoneNumber(contactSchema.getContactInfo());
        wrapperClass.setRole(userSchema.getRole());

        String jwtAccess = jwtTokenUtil.generateToken(wrapperClass);
        String jwtRefresh = jwtTokenUtil.generateRefreshToken(wrapperClass);

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + jwtAccess)
                .header("Refresh-Token", jwtRefresh)
                .body(wrapperClass.toString());
    }

    @Transactional
    public ResponseEntity<?> registration(UserDto userDto) {
        try {

            // extracting the email and phone data from the payload
            String email = userDto.getEmail();
            String phone = userDto.getPhone();

            // Check if user exists in the database with this email or phone
            Optional<ContactSchema> contactOptionalEmail = contactRepository.findByContactInfo(email);
            Optional<ContactSchema> contactOptionalPhone = contactRepository.findByContactInfo(phone);

            WrapperClass wrapperClass = new WrapperClass();

            if (contactOptionalEmail.isEmpty() && contactOptionalPhone.isEmpty()) {
                // User does not exist, create a new user
                UserSchema userSchema = new UserSchema();
                userSchema.setFirstName(userDto.getFirstName());
                userSchema.setLastName(userDto.getLastName());
                userSchema.setCreatedDate(System.currentTimeMillis());
                userSchema = userRepository.save(userSchema);
                // Save email and phone contact
                saveContact(userSchema, email, ContactType.EMAIL);
                saveContact(userSchema, phone, ContactType.PHONE);
                // Prepare response
                wrapperClass.setUserId(userSchema.getPrimaryKey());
                wrapperClass.setEmail(email);
                wrapperClass.setFirstName(userSchema.getFirstName());
                wrapperClass.setLastName(userSchema.getLastName());
                wrapperClass.setPhoneNumber(phone);
                wrapperClass.setRole(userSchema.getRole());

                String jwtAccess = jwtTokenUtil.generateToken(wrapperClass);
                String jwtRefresh = jwtTokenUtil.generateRefreshToken(wrapperClass);

                return ResponseEntity.status(HttpStatus.CREATED)
                        .header("Authorization", "Bearer " + jwtAccess)
                        .header("Refresh-Token", jwtRefresh)
                        .body(wrapperClass.toString());

            } else if (contactOptionalEmail.isPresent() && contactOptionalPhone.isEmpty()) {
                // User with the same email already registered
                ApiResponse apiResponse = new ApiResponse("User with this email already registered");
                return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);

            } else if (contactOptionalPhone.isPresent() && contactOptionalEmail.isEmpty()) {
                // User with the same phone already registered
                ApiResponse apiResponse = new ApiResponse("User with this phone already registered");
                return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);

            } else {
                // User exists with both email and phone, fetch details
                Optional<UserSchema> existingUserSchema = Optional.ofNullable(contactOptionalEmail.get().getUserSchema());
                Optional<ContactSchema> contactSchemaPhone = contactRepository.findByContactInfo(phone);

                wrapperClass.setUserId(existingUserSchema.get().getPrimaryKey());
                wrapperClass.setEmail(contactOptionalEmail.get().getContactInfo());
                wrapperClass.setFirstName(existingUserSchema.get().getFirstName());
                wrapperClass.setLastName(existingUserSchema.get().getLastName());
                wrapperClass.setPhoneNumber(contactSchemaPhone.get().getContactInfo());
                wrapperClass.setRole(existingUserSchema.get().getRole());

                String jwtAccess = jwtTokenUtil.generateToken(wrapperClass);
                String jwtRefresh = jwtTokenUtil.generateRefreshToken(wrapperClass);

                return ResponseEntity.status(HttpStatus.OK)
                        .header("Authorization", "Bearer " + jwtAccess)
                        .header("Refresh-Token", jwtRefresh)
                        .body(wrapperClass.toString());
            }
        } catch (UserAlreadyExist e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>("Error occurred while processing the request", HttpStatus.UNAUTHORIZED);
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
            ApiResponse apiResponse = new ApiResponse("Refresh token is missing or invalid.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
        }

        // extracting the token that is send by the client
        String token = refreshTokenDto.getToken();

        try {
            // Parse and validate the token in a single step
            Claims claims = jwtTokenUtil.parseAndValidateToken(token);

            // Extract necessary data from the token
            String email = claims.getSubject();
            String role = claims.get("role", String.class);
            String firstName = claims.get("firstName", String.class);
            String lastName = claims.get("lastName", String.class);
            String phoneNumber = claims.get("phoneNumber", String.class);
            Long uniqueId = claims.get("uniqueId", Double.class).longValue();

            // Generate a new access token
            WrapperClass wrapper = new WrapperClass();
            wrapper.setUserId(uniqueId);
            wrapper.setFirstName(firstName);
            wrapper.setLastName(lastName);
            wrapper.setEmail(email);
            wrapper.setRole(role);
            wrapper.setPhoneNumber(phoneNumber);

            String newAccessToken = jwtTokenUtil.generateToken(wrapper);

            ApiResponse apiResponse = new ApiResponse("Access token refreshed successfully");

            return ResponseEntity.ok()
                    .header("Authorization", "Bearer " + newAccessToken)
                    .header("Refresh-Token", token)
                    .body(apiResponse);

        } catch (ExpiredJwtException ex) {
            // Handle token expiration specifically
            ApiResponse apiResponse = new ApiResponse("Refresh token has expired. Please login again.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);

        } catch (MalformedJwtException | SignatureException ex) {
            // Handle invalid token structure or signature issues
            ApiResponse apiResponse = new ApiResponse("Invalid token format or signature.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);

        } catch (Exception ex) {
            // General exception handling
            ApiResponse apiResponse = new ApiResponse("Invalid or expired refresh token.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponse);
        }
    }
}

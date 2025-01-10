package com.example.FinanceDemoApi.financeDemo.Service;

import com.example.FinanceDemoApi.financeDemo.Exception.InvalidTokenException;
import com.example.FinanceDemoApi.financeDemo.Exception.UserAlreadyExist;

import com.example.FinanceDemoApi.financeDemo.Model.*;
import com.example.FinanceDemoApi.financeDemo.Repository.ContactRepository;
import com.example.FinanceDemoApi.financeDemo.Repository.UserRepository;
import com.example.FinanceDemoApi.financeDemo.Utility.ApiResponse;
import com.example.FinanceDemoApi.financeDemo.Utility.ContactType;
import com.example.FinanceDemoApi.financeDemo.Utility.JwtTokenUtil;

import com.example.FinanceDemoApi.financeDemo.Utility.ResponseWrapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import java.util.HashMap;
import java.util.Map;
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


    @Transactional
    public ResponseEntity<?> googleSignIn(String idTokenString){
        try{

            if (idTokenString == null || idTokenString.trim().isEmpty()) {
                ApiResponse apiResponse = new ApiResponse("Bad Request: ID token is missing or empty.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(apiResponse);
            }

            idTokenString = idTokenString.trim();
            HttpTransport transport = new NetHttpTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            System.out.println(idToken);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                String email = (String) payload.get("email");
                String phone = (String) payload.get("phoneNumber");

                Optional<ContactSchema> contactSchemaEmail = contactRepository.findByContactInfo(email);
                Optional<ContactSchema> contactSchemaPhone = contactRepository.findByContactInfo(phone);

                // if user already registered with email
                if (contactSchemaEmail.isPresent()){
                    System.out.println("User with this email already exist");

                    Optional<UserSchema> userSchema  = Optional.ofNullable(contactSchemaEmail.get().getUserId());

                    WrapperClass wrapperClass = new WrapperClass();


                    wrapperClass.setUserId(userSchema.get().getPrimaryKey());
                    wrapperClass.setEmail(contactSchemaEmail.get().getContactInfo());
                    wrapperClass.setFirstName(userSchema.get().getFirstName());
                    wrapperClass.setLastName(userSchema.get().getLastName());
                    wrapperClass.setPhoneNumber(contactSchemaEmail.get().getContactInfo());
                    wrapperClass.setRole(userSchema.get().getRole());

                    String jwt = jwtTokenUtil.generateToken(wrapperClass);


//

                    return ResponseEntity.ok()
                            .header("Authorization", "Bearer " + jwt)
                            .body(wrapperClass.toString()); // user already registered with email



                }
                else if (contactSchemaPhone.isPresent()){
                    // if user has already registered with phone
                    Optional<UserSchema> userSchema  = Optional.ofNullable(contactSchemaPhone.get().getUserId());

                    WrapperClass wrapperClass = new WrapperClass();


                    wrapperClass.setUserId(userSchema.get().getPrimaryKey());
                    wrapperClass.setEmail(contactSchemaEmail.get().getContactInfo());
                    wrapperClass.setFirstName(userSchema.get().getFirstName());
                    wrapperClass.setLastName(userSchema.get().getLastName());
                    wrapperClass.setPhoneNumber(contactSchemaEmail.get().getContactInfo());
                    wrapperClass.setRole(userSchema.get().getRole());

                    String jwt = jwtTokenUtil.generateToken(wrapperClass);

                    System.out.println("jwt token = "+jwt);

                    return ResponseEntity.ok()
                            .header("Authorization", "Bearer " + jwt)
                            .body(wrapperClass.toString()); // user already registered with email
                }
                else {
                    // user has not registered we will fetch the payload  and send payload
                    // data to frontend

                    ResponseWrapper responseWrapper = new ResponseWrapper();

                    String firstName = (String) payload.get("given_name");
                    String lastName = (String) payload.get("family_name");

                    responseWrapper.setEmail(email);
                    responseWrapper.setPhone(phone);
                    responseWrapper.setFirstName(firstName);
                    responseWrapper.setLastName(lastName);

//                    ApiResponse apiResponse = new ApiResponse("User has not registered");

                    return new ResponseEntity<>(responseWrapper,HttpStatus.NOT_FOUND);
                }

            }
            else {
                 throw new InvalidTokenException("Invalid ID token");
            }
        }
        catch (InvalidTokenException e) {
            // Handle invalid token scenario
            ApiResponse apiResponse = new ApiResponse("Unauthorized. Token is invalid or expired");
            return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
        }
        catch (Exception e) {
            //LOG.error("Token validation failed: {}", idTokenString, e);
            ApiResponse apiResponse = new ApiResponse("Internal server error. Token verification failed.");
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<?> registration(UserDto userDto) {
        try {
            // Validating the request body
            if (userDto == null
                    || userDto.getEmail() == null || userDto.getEmail().trim().isEmpty()
                    || userDto.getFirstName() == null || userDto.getFirstName().trim().isEmpty()
                    || userDto.getLastName() == null || userDto.getLastName().trim().isEmpty()
                    || userDto.getPhone() == null || userDto.getPhone().trim().isEmpty()) {

                ApiResponse apiResponse = new ApiResponse("Bad Request: All fields (email, firstName, lastName, phone) are required.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(apiResponse);
            }

            // validating the phone number
            if (userDto.getPhone() == null || !userDto.getPhone().matches("\\d{10}")) {
                ApiResponse apiResponse = new ApiResponse("Bad Request: The phone number entered is not valid");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(apiResponse);
            }



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

                String jwt = jwtTokenUtil.generateToken(wrapperClass);

                return ResponseEntity.status(HttpStatus.CREATED)
                        .header("Authorization", "Bearer " + jwt)
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
                Optional<UserSchema> existingUserSchema = Optional.ofNullable(contactOptionalEmail.get().getUserId());
                Optional<ContactSchema> contactSchemaPhone = contactRepository.findByContactInfo(phone);

                wrapperClass.setUserId(existingUserSchema.get().getPrimaryKey());
                wrapperClass.setEmail(contactOptionalEmail.get().getContactInfo());
                wrapperClass.setFirstName(existingUserSchema.get().getFirstName());
                wrapperClass.setLastName(existingUserSchema.get().getLastName());
                wrapperClass.setPhoneNumber(contactSchemaPhone.get().getContactInfo());
                wrapperClass.setRole(existingUserSchema.get().getRole());

                String jwt = jwtTokenUtil.generateToken(wrapperClass);

                String refreshToken = jwtTokenUtil.generateRefreshToken(wrapperClass);


                Map<String, String> tokens = new HashMap<>();
                tokens.put("accessToken", jwt);
                tokens.put("refreshToken", refreshToken);

                System.out.println(refreshToken);

                return ResponseEntity.ok()
                        .header("Authorization", "Bearer " + jwt)
                        .body(tokens);

//                return ResponseEntity.status(HttpStatus.OK)
//                        .header("Authorization", "Bearer " + jwt)
//                        .body(wrapperClass.toString());
            }

        } catch (UserAlreadyExist e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {

            return new ResponseEntity<>("Error occurred while processing the request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Helper method to save contact information
    private void saveContact(UserSchema userSchema, String contactInfo, ContactType contactType) {
        ContactSchema contact = new ContactSchema();
        contact.setUserID(userSchema);
        contact.setContactInfo(contactInfo);
        contact.setContactType(contactType);
        contactRepository.save(contact);
    }

    // method for refreshing the access  token when it is expired
    public ResponseEntity<?>refreshToken(RefreshTokenDto refreshTokenDto){

        // token validation do whether user has sent token in body or not

        String token = refreshTokenDto.getToken();

        System.out.println(token);

        try {
            if (jwtTokenUtil.isTokenExpired(token)) {
                // If the refresh token has expired, send a 401 Unauthorized response
                ApiResponse apiResponse = new ApiResponse("Refresh token has expired. Please login again.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
            }

            // Validate and extract claims from the refresh token
            Claims refreshClaims = jwtTokenUtil.extractRefreshClaims(token);
            String email = refreshClaims.getSubject();
            System.out.println(email);
            String role = refreshClaims.get("role", String.class);
            System.out.println(role);
            String firstName = refreshClaims.get("firstName",String.class);
            System.out.println(firstName);
            String lastName = refreshClaims.get("lastName",String.class);
            System.out.println(lastName);
            String phoneNumber = refreshClaims.get("phoneNumber",String.class);
            System.out.println(phoneNumber);
            Double id = refreshClaims.get("uniqueId",Double.class);

            Long uniqueId = id.longValue();

            System.out.println(id);

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
                    .body(apiResponse);
        } catch (ExpiredJwtException ex) {
            // Handle token expiration specifically
            ApiResponse apiResponse = new ApiResponse("Refresh token has expired. Please login again.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);

        } catch (MalformedJwtException ex) {
            // Handle invalid token structure
            ApiResponse apiResponse = new ApiResponse("Invalid token format.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);

        } catch (Exception ex) {
            // General exception handling
            ApiResponse apiResponse = new ApiResponse("Invalid or expired refresh token.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponse);
        }

    }


}

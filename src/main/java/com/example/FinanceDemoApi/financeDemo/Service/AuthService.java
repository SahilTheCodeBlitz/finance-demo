package com.example.FinanceDemoApi.financeDemo.Service;
import com.example.FinanceDemoApi.financeDemo.Exception.InvalidInputException;
import com.example.FinanceDemoApi.financeDemo.Exception.InvalidTokenException;
import com.example.FinanceDemoApi.financeDemo.Exception.UserAlreadyExist;
import com.example.FinanceDemoApi.financeDemo.Exception.UserNotFoundException;
import com.example.FinanceDemoApi.financeDemo.Model.ContactSchema;
import com.example.FinanceDemoApi.financeDemo.Model.UserDto;
import com.example.FinanceDemoApi.financeDemo.Model.UserSchema;
import com.example.FinanceDemoApi.financeDemo.Model.WrapperClass;
import com.example.FinanceDemoApi.financeDemo.Repository.ContactRepository;
import com.example.FinanceDemoApi.financeDemo.Repository.UserRepository;
import com.example.FinanceDemoApi.financeDemo.Utility.ContactType;
import com.example.FinanceDemoApi.financeDemo.Utility.JwtTokenUtil;
import com.example.FinanceDemoApi.financeDemo.Utility.ResponseWrapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ott.InvalidOneTimeTokenException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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







    public ResponseEntity<?> googleSignIn(String idTokenString){
        try{

            if (idTokenString == null || idTokenString.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Bad Request: ID token is missing or empty.");
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

                    String jwt = jwtTokenUtil.generateToken(email);

                    System.out.println(wrapperClass);

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

                    String jwt = jwtTokenUtil.generateToken(email);

                    System.out.println(wrapperClass);

                    return ResponseEntity.ok()
                            .header("Authorization", "Bearer " + jwt)
                            .body(wrapperClass.toString()); // user already registered with email
                }
                else {
                    return new ResponseEntity<>("User not registered" ,HttpStatus.NOT_FOUND);
                }

            }
            else {
                 throw new InvalidTokenException("Invalid ID token");
            }
        }
        catch (InvalidTokenException e) {
            // Handle invalid token scenario
            return new ResponseEntity<>("\t\n" +
                    "Unauthorized. Token is invalid or expired", HttpStatus.UNAUTHORIZED);
        }
        catch (Exception e) {
            //LOG.error("Token validation failed: {}", idTokenString, e);
            return new ResponseEntity<>("\t\n" +
                    "\t\n" +
                    "Internal server error. Token verification failed.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> registration(UserDto userDto) {
        try {
            // Validating the request body
            if (userDto == null
                    || userDto.getEmail() == null || userDto.getEmail().trim().isEmpty()
                    || userDto.getFirstName() == null || userDto.getFirstName().trim().isEmpty()
                    || userDto.getLastName() == null || userDto.getLastName().trim().isEmpty()
                    || userDto.getPhone() == null || userDto.getPhone().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Bad Request: All fields (email, firstName, lastName, phone) are required.");
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

                String jwt = jwtTokenUtil.generateToken(email);

                return ResponseEntity.status(HttpStatus.CREATED)
                        .header("Authorization", "Bearer " + jwt)
                        .body(wrapperClass.toString());

            } else if (contactOptionalEmail.isPresent() && contactOptionalPhone.isEmpty()) {
                // User with the same email already registered
                return new ResponseEntity<>("User with this email already registered", HttpStatus.CONFLICT);
            } else if (contactOptionalPhone.isPresent() && contactOptionalEmail.isEmpty()) {
                // User with the same phone already registered
                return new ResponseEntity<>("User with this phone already registered", HttpStatus.CONFLICT);
            } else {
                // User exists with both email and phone, fetch details
                Optional<UserSchema> existingUserSchema = Optional.ofNullable(contactOptionalEmail.get().getUserId());
                Optional<ContactSchema> contactSchemaPhone = contactRepository.findByContactInfo(phone);

                wrapperClass.setUserId(existingUserSchema.get().getPrimaryKey());
                wrapperClass.setEmail(contactOptionalEmail.get().getContactInfo());
                wrapperClass.setFirstName(existingUserSchema.get().getFirstName());
                wrapperClass.setLastName(existingUserSchema.get().getLastName());
                wrapperClass.setPhoneNumber(contactSchemaPhone.get().getContactInfo());

                String jwt = jwtTokenUtil.generateToken(email);

                return ResponseEntity.status(HttpStatus.OK)
                        .header("Authorization", "Bearer " + jwt)
                        .body(wrapperClass.toString());
            }

        } catch (UserAlreadyExist e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception for debugging
            return new ResponseEntity<>("Error occurred while processing the request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Helper method to save contact information
    private void saveContact(UserSchema userSchema, String contactInfo, ContactType contactType) {
        ContactSchema contact = new ContactSchema();
        contact.setUserID(userSchema); // foreign key
        contact.setContactInfo(contactInfo);
        contact.setContactType(contactType);
        contactRepository.save(contact);
    }




}

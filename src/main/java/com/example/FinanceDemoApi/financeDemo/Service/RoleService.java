package com.example.FinanceDemoApi.financeDemo.Service;


import com.example.FinanceDemoApi.financeDemo.Model.UserSchema;
import com.example.FinanceDemoApi.financeDemo.Model.WrapperClass;
import com.example.FinanceDemoApi.financeDemo.Repository.UserRepository;
import com.example.FinanceDemoApi.financeDemo.Utility.ApiResponse;
import com.example.FinanceDemoApi.financeDemo.Utility.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class RoleService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private static final List<String> VALID_ROLES = Arrays.asList("free", "premium", "gold", "silver");


    public ResponseEntity<?> updateRole(String tokenHeader, String newRole) {
        // Check if the token is provided and follows the 'Bearer <token>' format
//        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
//            ApiResponse apiResponse = new ApiResponse("Invalid Token");
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
//        }

        if (!VALID_ROLES.contains(newRole)){
            ApiResponse apiResponse = new ApiResponse("The incorrect value for string received in body ");
            return new ResponseEntity<>(apiResponse,HttpStatus.BAD_REQUEST);
        }

        String token = tokenHeader.substring(7);  // Remove the "Bearer " prefix

        try {
            // Extract claims from the token
            Claims claims = jwtTokenUtil.extractAllClaims(token);

            Double uniqueId = claims.get("uniqueId", Double.class);

            Long id = uniqueId.longValue();

            // Find user in the database
            Optional<UserSchema> userOptional = userRepository.findById(id);
            if (userOptional.isEmpty()) {
                ApiResponse apiResponse = new ApiResponse("User does not exist in the database");
                return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
            }


            // Update the user's role
            UserSchema user = userOptional.get();
            user.setRole(newRole);
            userRepository.save(user);

            String firstName = claims.get("firstName", String.class);
            String lastName = claims.get("lastName", String.class);
            String phoneNumber = claims.get("phoneNumber", String.class);
            String sub = claims.getSubject();


            // Generate a new JWT token with updated role
            WrapperClass wrapperClass = new WrapperClass();

            System.out.println((String) claims.get("sub").toString());
            wrapperClass.setRole(newRole);
            wrapperClass.setEmail(sub.toString());
            wrapperClass.setPhoneNumber(phoneNumber);
            wrapperClass.setFirstName(firstName);
            wrapperClass.setLastName(lastName);
            wrapperClass.setUserId(id);

            System.out.println(wrapperClass);


            String newToken = jwtTokenUtil.generateToken(wrapperClass);

            String newRefreshToken = jwtTokenUtil.generateRefreshToken(wrapperClass);

            // Send response with updated role and new token
            ApiResponse apiResponse = new ApiResponse("User role has updated to " + newRole);
            return ResponseEntity.ok()
                    .header("Authorization", "Bearer " + newToken)
                    .header("Refresh-Token", newRefreshToken)
                    .body(apiResponse);

        }
//        }catch (ExpiredJwtException ex) {
//            // Handle token expiration specifically
//            ApiResponse apiResponse = new ApiResponse("Access token has expired");
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
//        }
        catch (Exception e) {
            ApiResponse apiResponse = new ApiResponse("Internal Server Error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

}

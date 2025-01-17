package com.example.FinanceDemoApi.financeDemo.Service;
import com.example.FinanceDemoApi.financeDemo.Model.UserSchema;
import com.example.FinanceDemoApi.financeDemo.Utility.WrapperClass;
import com.example.FinanceDemoApi.financeDemo.Repository.UserRepository;
import com.example.FinanceDemoApi.financeDemo.Utility.ApiResponse;
import com.example.FinanceDemoApi.financeDemo.Utility.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
@Service
public class RoleService {
    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;
    @Autowired
    public RoleService(UserRepository userRepository , JwtTokenUtil jwtTokenUtil){
        this.userRepository=userRepository;
        this.jwtTokenUtil=jwtTokenUtil;
    }
    private static final List<String> VALID_ROLES = Arrays.asList("free", "premium", "gold", "silver");
    public ResponseEntity<?> updateRole(String tokenHeader, String newRole) {
        // validating that user has entered role from above valid_role
        if (!VALID_ROLES.contains(newRole)){
            return new ResponseEntity<>(new ApiResponse("The incorrect value for string received in body "),HttpStatus.BAD_REQUEST);
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
                return new ResponseEntity<>(new ApiResponse("User does not exist in the database"), HttpStatus.NOT_FOUND);
            }
            // Update the user's role
            UserSchema user = userOptional.get();
            user.setRole(newRole);
            userRepository.save(user);

            // Generate a new JWT tokens with updated role
            WrapperClass wrapperClass = new WrapperClass();
            wrapperClass.setRole(newRole);
            wrapperClass.setEmail( claims.getSubject().toString());
            wrapperClass.setPhoneNumber(claims.get("phoneNumber", String.class));
            wrapperClass.setFirstName(claims.get("firstName", String.class));
            wrapperClass.setLastName(claims.get("lastName", String.class));
            wrapperClass.setUserId(id);

            return ResponseEntity.ok()
                    .header("Authorization", "Bearer " + jwtTokenUtil.generateToken(wrapperClass))
                    .header("Refresh-Token", jwtTokenUtil.generateRefreshToken(wrapperClass))
                    .body(new ApiResponse("User role has updated to " + newRole));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body( new ApiResponse("Internal Server Error"));
        }
    }
}

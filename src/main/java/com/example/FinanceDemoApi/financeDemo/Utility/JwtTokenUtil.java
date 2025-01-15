package com.example.FinanceDemoApi.financeDemo.Utility;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.UUID;
@Component
public class JwtTokenUtil {
    @Value("${jwt.secret}")
    private String SECRET_KEY;
    @Value("${jwt.secretKeyAccess}")
    private String refreshSecretKey;

    // method for generating the access token
    public String generateToken(WrapperClass wrapper) {

        // fetching the data
        String email = wrapper.getEmail();
        String firstName = wrapper.getFirstName();
        String lastName = wrapper.getLastName();
        String phoneNumber = wrapper.getPhoneNumber();
        Long uniqueId = wrapper.getUserId();
        String role = wrapper.getRole();

        // returning jwt access token
        return Jwts.builder()
                .setSubject(email)
                .claim("firstName", firstName)
                .claim("lastName", lastName)
                .claim("phoneNumber", phoneNumber)
                .claim("uniqueId",uniqueId)
                .claim("role",role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 2))// 2 days
                .setIssuer("FinanceDemoApp")
                .setId(UUID.randomUUID().toString())
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // method for extracting claims from the token
    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY) // Secret key to validate the signature
                    .build()
                    .parseClaimsJws(token) // Parsing the JWT token
                    .getBody(); // Returns the claims from the body
        } catch (JwtException e) {
            throw new IllegalArgumentException("Invalid or expired token", e); // Handle invalid token scenario
        }
    }


    // method for generating refresh token
    public String generateRefreshToken(WrapperClass wrapper) {
        // extracting data
        String email = wrapper.getEmail();
        String firstName = wrapper.getFirstName();
        String lastName = wrapper.getLastName();
        String phoneNumber = wrapper.getPhoneNumber();
        Long uniqueId = wrapper.getUserId();
        String role = wrapper.getRole();

        // returning jwt refresh token
        return Jwts.builder()
                .setSubject(email)
                .claim("firstName",firstName)
                .claim("lastName",lastName)
                .claim("phoneNumber",phoneNumber)
                .claim("uniqueId", uniqueId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 20))// 20 days
                .setIssuer("FinanceDemoApp")
                .setId(UUID.randomUUID().toString())
                .signWith(SignatureAlgorithm.HS256, refreshSecretKey) // Use a different secret key for refresh tokens
                .compact();
    }

    // method for getting the claims from the refresh token
    public Claims extractRefreshClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(refreshSecretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new IllegalArgumentException("Invalid or expired refresh token", e);
        }
    }

    // method for parsing and validating the token
    public Claims parseAndValidateToken(String token) throws ExpiredJwtException, MalformedJwtException, SignatureException {
        return Jwts.parser()
                .setSigningKey(refreshSecretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

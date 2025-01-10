package com.example.FinanceDemoApi.financeDemo.Utility;


import com.example.FinanceDemoApi.financeDemo.Model.WrapperClass;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenUtil {

  // getting error for loading key from application.properties will solve late
//    private final String SECRET_KEY = "e2ca8d2d7887849c287bdf1f61ff503ba4ec95c7ddee3307c624837afa70373b";

    // method for access token generation
//    @Value("${jwt.secret}")
//    private String secretKey;

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.secretKeyAccess}")
    private String refreshSecretKey;

    @Value("${jwt.refreshTokenExpiration}")
    private long refreshTokenExpiration;

    public String generateToken(WrapperClass wrapper) {
        String email = wrapper.getEmail();
        String firstName = wrapper.getFirstName();
        String lastName = wrapper.getLastName();
        String phoneNumber = wrapper.getPhoneNumber();
        Long uniqueId = wrapper.getUserId();
        String role = wrapper.getRole();



        return Jwts.builder()
                .setSubject(email)
                .claim("firstName", firstName)
                .claim("lastName", lastName)
                .claim("phoneNumber", phoneNumber)
                .claim("uniqueId",uniqueId)
                .claim("role",role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
                .setIssuer("FinanceDemoApp")
                .setId(UUID.randomUUID().toString())
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY) // Use the same secret key to validate the signature
                    .build()
                    .parseClaimsJws(token) // Parse the JWT token
                    .getBody(); // Return the claims from the body
        } catch (JwtException e) {
            throw new IllegalArgumentException("Invalid or expired token", e); // Handle invalid token scenario
        }
    }









    // method for refresh token generation



    // Generate refresh token
//    public String generateRefreshToken(String username) {
//        Date now = new Date();
//        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration); // Set expiration for the refresh token
//
//        return Jwts.builder()
//                .setSubject(username) // Set username as the subject
//                .setIssuedAt(now) // Set current time as the issued time
//                .setIssuer("FinanceDemoApp")
//                .setId(UUID.randomUUID().toString())
//                .setExpiration(expiryDate) // Set expiration time
//                .signWith(SignatureAlgorithm.HS256,refreshSecretKey ) // Sign the token with your secret key
//                .compact(); // Build the JWT token and return it as a String
//    }

    // this is for the refresh token
//    public Claims validateToken(String token, boolean isRefreshToken) throws JwtException {
//
//        String accessTokenSecret=SECRET_KEY;
//        String secretKey = isRefreshToken ? refreshSecretKey : accessTokenSecret;
//
//        return Jwts.parser()
//                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }




}

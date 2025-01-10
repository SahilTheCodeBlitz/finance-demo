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


    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.secretKeyAccess}")
    private String refreshSecretKey;

//    @Value("${jwt.refreshTokenExpiration}")
//    private long refreshTokenExpiration;

    // method for generating the jwt token
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
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 40))// 40 seconds
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


    // method for generating refresh token

    public String generateRefreshToken(WrapperClass wrapper) {

        String email = wrapper.getEmail();
        String firstName = wrapper.getFirstName();
        String lastName = wrapper.getLastName();
        String phoneNumber = wrapper.getPhoneNumber();
        Long uniqueId = wrapper.getUserId();
        String role = wrapper.getRole();

        return Jwts.builder()
                .setSubject(email)
                .claim("firstName",firstName)
                .claim("lastName",lastName)
                .claim("phoneNumber",phoneNumber)
                .claim("uniqueId", uniqueId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60)) // Longer expiration time
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

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = extractRefreshClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true; // If there's an error in extraction, consider it expired
        }
    }












}

package com.example.FinanceDemoApi.financeDemo.Filters;
import com.example.FinanceDemoApi.financeDemo.Utility.SetJsonResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Value("${jwt.secret}")
    private String SECRET_KEY;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    Claims claims = Jwts.parser()
                            .setSigningKey(SECRET_KEY)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();

                    String username = claims.getSubject();
                    String role =(claims.get("role", String.class));

                    if (username != null & role != null) {
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(username, null,  List.of(authority));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (io.jsonwebtoken.ExpiredJwtException e) {
                    SetJsonResponse setJsonResponse = new SetJsonResponse();
                    setJsonResponse.setJsonErrorResponse(response,HttpServletResponse.SC_UNAUTHORIZED,"Expired Jwt token");
                    return;
                }
                catch (JwtException e) {
                    SetJsonResponse setJsonResponse = new SetJsonResponse();
                    setJsonResponse.setJsonErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                    return;
                }
            }
            filterChain.doFilter(request, response);
    }
}

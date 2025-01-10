    package com.example.FinanceDemoApi.financeDemo.Filters;

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


//        private final String SECRET_KEY = "e2ca8d2d7887849c287bdf1f61ff503ba4ec95c7ddee3307c624837afa70373b";


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

    //
                    String username = claims.getSubject();

                    String role =(claims.get("role", String.class));

                    if (username != null & role != null) {
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                        // Set authentication
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(username, null,  List.of(authority));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }

    //                // Extract required details from the token
    //                String email = claims.getSubject();
    //                String firstName = claims.get("firstName", String.class);
    //                String lastName = claims.get("lastName", String.class);
    //                String phoneNumber = claims.get("phoneNumber", String.class);
    //                Long userId = claims.get("userId",Long.class);
    //
    //                System.out.println(email);
    //                System.out.println(firstName);


    //                // Check if the email is not null
    //                if (email != null) {
    //
    //                    UserPrincipals userPrincipals = new UserPrincipals(email,firstName,lastName,phoneNumber,userId);
    //
    //                    // Create a CustomAuthenticationToken
    //                    CustomAuthenticationToken authenticationToken = new CustomAuthenticationToken(userPrincipals, token);
    //
    //                    // Set the authentication in the security context
    //                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    //                }
                } catch (io.jsonwebtoken.ExpiredJwtException e) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("JWT token has expired");
                    return;
                } catch (JwtException e) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Invalid JWT token");
                    return;

                }
            }

            filterChain.doFilter(request, response);
        }






    }

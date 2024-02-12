package fr.utc.sr03.chat.security;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class JwtTokenProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${api.security.token.signatureSecretKey}")
    private String secretKey;

    @Value("${api.security.token.validityInMilliseconds}")
    private long validityInMilliseconds;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createSimpleToken(String id, String role) {
        // Username
        Claims claims = Jwts.claims().setSubject(id);

        // Role
        if (role != null && !role.isEmpty()) {
            claims.put("role", role);
        }

        // Dates de creation et d'expiration
        Date now = new Date();
        Date expiration = new Date(now.getTime() + validityInMilliseconds);

        // Build JWT
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Optional<Authentication> getAuthentication(String token) {
        if (isTokenValid(token)) {
            Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();

            // Recup username
            String username = claims.getSubject();

            // Recup authorities (role)
            List<? extends GrantedAuthority> authorities = new ArrayList<>();
            if (claims.get("role") != null) {
                authorities = Stream.of(claims.get("role").toString())
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            }

            // Spring AuthenticationToken
            return Optional.of(new UsernamePasswordAuthenticationToken(username, "", authorities));
        }

        return Optional.empty();
    }

    private boolean isTokenValid(String token){
        if (token != null && !token.isEmpty()) {
            try {
                Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
                return true;
            } catch (ExpiredJwtException e) {
                LOGGER.error("Le token a expire");
            } catch (Exception e) {
                LOGGER.error("Erreur lors de la validation du token : " + e.getMessage());
            }
        }

        return false;
    }
}
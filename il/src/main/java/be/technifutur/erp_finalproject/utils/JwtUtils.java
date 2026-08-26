package be.technifutur.erp_finalproject.utils;

import be.technifutur.erp_finalproject.configs.JwtProperties;
import be.technifutur.erp_finalproject.entities.User;
import io.jsonwebtoken.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtils {

    private final SecretKey key;
    private final long validitySeconds;
    private final JwtParser parser;

    public JwtUtils(JwtProperties properties) {
        var secret = properties.secret().getBytes(StandardCharsets.UTF_8);
        this.key = new SecretKeySpec(secret, "HmacSHA256");
        this.validitySeconds = properties.expiration();
        this.parser = Jwts.parser().verifyWith(key).build();
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .signWith(key)
                .subject(user.getEmail())
                .claim("id", user.getId())
                .claim("ROLE", user.getAuthorities()
                        .stream()
                        .map((GrantedAuthority::getAuthority))
                        .toList())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + validitySeconds * 1000L))
                .compact();
    }

    public Claims getClaims(String token) {
        return parser.parseSignedClaims(token).getPayload();
    }

    public Long getId(String token) {
        return getClaims(token).get("id", Long.class);
    }

    public UserSession getUser (String token){
        Claims claims = getClaims(token);
        return new UserSession(claims.get("id", Long.class), claims.getSubject());
    }

    public Collection<? extends GrantedAuthority> getAuthorities(String token) {
        List<?> roles = getClaims(token).get("ROLE", List.class);
        return roles == null
                ? List.of()
                : roles.stream()
                        .map(Object::toString)
                        .map(SimpleGrantedAuthority::new)
                        .toList();
    }

    public boolean isValid (String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public record UserSession(
            Long id,
            String email
    ){
        public UserSession(User user){
            this(user.getId(), user.getEmail());
        }
    }
}
package utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtManager {
    private String key;

    public JwtManager(String key) {
        this.key = key;
    }

    public String createToken(String userIdString, String role) {
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .claim("id", userIdString)
                .claim("role", role)
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }

    public Jws<Claims> validateToken(String token) throws Exception {
        return Jwts.parser().setSigningKey(key).parseClaimsJws(token);
    }
}

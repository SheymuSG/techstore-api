package cl.techstore.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // Llave secreta generada dinámicamente para firmar los tokens
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    // Expiración: 1 hora (3600000 milisegundos)
    private final long tiempoExpiracion = 3600000;

    public String generarToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + tiempoExpiracion))
                .signWith(key)
                .compact();
    }

    public String extraerUsername(String token) {
        return getClaims(token).getSubject();
    }

    public boolean validarToken(String token) {
        try {
            return !getClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
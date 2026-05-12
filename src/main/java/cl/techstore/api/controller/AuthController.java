package cl.techstore.api.controller;

import cl.techstore.api.dto.LoginRequest;
import cl.techstore.api.dto.LoginResponse;
import cl.techstore.api.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Spring Security valida automáticamente si las credenciales coinciden con el usuario en memoria
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales incorrectas");
        }

        // Si pasa, generamos el token y armamos la respuesta exacta que pide el ejercicio
        String jwt = jwtUtil.generarToken(loginRequest.getUsername());
        LoginResponse response = new LoginResponse(jwt, "Bearer", "3600");
        
        return ResponseEntity.ok(response);
    }
}
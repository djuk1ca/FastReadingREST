package com.example.demo.auth;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import com.example.demo.api.dto.*;
import com.example.demo.api.error.ConflictException;
import com.example.demo.model.User;
import com.example.demo.repositories.UserRepository;


@Service
public class AuthService {
	
	@Autowired
    UserRepository userRepo;
	
	@Autowired
    PasswordEncoder encoder;
	
	@Autowired
    JwtEncoder jwtEncoder;

    private final String issuer;
    private final long expSeconds;

    public AuthService(
        @Value("${app.jwt.issuer}") String issuer,
        @Value("${app.jwt.exp-seconds}") long expSeconds
    ) {
        this.issuer = issuer;
        this.expSeconds = expSeconds;
    }

    public UserResponse register(RegisterRequest req) {
        userRepo.findByEmail(req.email()).ifPresent(u -> { throw new ConflictException("Email already in use"); });
        userRepo.findByUsername(req.username()).ifPresent(u -> { throw new ConflictException("Username already in use"); });

        User u = new User();
        u.setUsername(req.username());
        u.setEmail(req.email());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setRole("USER");

        u = userRepo.save(u);
        return new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getRole());
    }

    public TokenResponse login(LoginRequest req) {
        User u = userRepo.findByEmail(req.identifier())
                .or(() -> userRepo.findByUsername(req.identifier()))
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));

        if (!encoder.matches(req.password(), u.getPasswordHash())) {
            throw new BadCredentialsException("Bad credentials");
        }

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(issuer)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(expSeconds))
            .subject(String.valueOf(u.getId()))
            .claim("username", u.getUsername())
            .claim("email", u.getEmail())
            .claim("role", u.getRole()) // "ADMIN" ili "USER"
            .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new TokenResponse(token, expSeconds);
    }
}
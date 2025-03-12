package sample.bpm.be.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import sample.bpm.be.entity.RefreshToken;
import sample.bpm.be.entity.User;
import sample.bpm.be.model.AuthRequest;
import sample.bpm.be.model.AuthResponse;
import sample.bpm.be.model.RegisterRequest;
import sample.bpm.be.repository.UserRepository;
import sample.bpm.be.security.JwtTokenProvider;
import sample.bpm.be.service.RefreshTokenService;

import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider,
                          RefreshTokenService refreshTokenService, UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ✅ Register User
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username is already taken!");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(request.getRoles())
                .build();

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully!");
    }

    // ✅ Login and return Access & Refresh Token
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String accessToken = jwtTokenProvider.generateAccessToken(request.getUsername());

        // ✅ Generate refresh token
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken.getToken()));
    }

    // ✅ Refresh JWT Access Token using Refresh Token
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestParam String refreshToken) {
        // ✅ Validate the refresh token
        if (!jwtTokenProvider.validateToken(refreshToken, true)) {
            return ResponseEntity.status(403).body(null);
        }

        // ✅ Find refresh token in DB
        Optional<RefreshToken> tokenOpt = refreshTokenService.findByToken(refreshToken);

        if (tokenOpt.isPresent() && tokenOpt.get().getExpiryDate().after(new Date())) {
            String accessToken = jwtTokenProvider.generateAccessToken(tokenOpt.get().getUser().getUsername());
            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
        } else {
            return ResponseEntity.status(403).body(null);
        }
    }
}

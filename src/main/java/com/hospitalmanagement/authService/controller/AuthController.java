package com.hospitalmanagement.authService.controller;

import com.hospitalmanagement.authService.model.user.User;
import com.hospitalmanagement.authService.service.UserService;
import com.hospitalmanagement.authService.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        return userService.saveUser(user);
    }

    @GetMapping("/user/{username}")
    public User getUserByUsername(@PathVariable String username) {
        return userService.findByUsername(username);
    }

    @PostMapping("/signup")
    public String signup(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.saveUser(user);
        return "User registered successfully updated message";
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
        );
        String token = jwtUtil.generateToken(user.getUsername(), userService.getRoleByUsername(user.getUsername()));


        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(60*60) // max age
                .sameSite("strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Login successful");
    }


    @GetMapping("/verify")
//    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String token, HttpServletRequest) {
    public ResponseEntity<?> verifyToken(HttpServletRequest request) {
//        http only token, so read it from the cookies
//        otherwise can send and recieve in headers also
        String token = null;
        if(request.getCookies() != null) {
            for(Cookie cookie: request.getCookies()) {
                if("jwt".equalsIgnoreCase(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        try {
            // Remove "Bearer " prefix if present
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

//            // Validate and parse claims
            Claims claims = jwtUtil.extractClaims(token);

            // Extract user details
            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            if(jwtUtil.isTokenValid(token)) {
                System.out.println("Valid token found.");
            } else {
                System.out.println("Invalid token found.");
            }

            // Respond with user details
            return ResponseEntity.ok().body("Token is valid. User: " + username + ", Role: " + role);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid or expired token");
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Invalidate the cookie
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0) // Set max-age to 0 to delete the cookie
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("Logout successful");
    }

}

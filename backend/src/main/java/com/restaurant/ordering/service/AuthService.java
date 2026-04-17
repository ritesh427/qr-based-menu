package com.restaurant.ordering.service;

import com.restaurant.ordering.dto.AuthRequest;
import com.restaurant.ordering.dto.AuthResponse;
import com.restaurant.ordering.exception.BadRequestException;
import com.restaurant.ordering.repository.UserRepository;
import com.restaurant.ordering.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        return userRepository.findByUsername(request.username())
                .map(user -> new AuthResponse(
                        jwtService.generateToken(user.getUsername(), user.getRestaurantId(), user.getRole().name()),
                        user.getUsername(),
                        user.getRestaurantId()
                ))
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));
    }
}

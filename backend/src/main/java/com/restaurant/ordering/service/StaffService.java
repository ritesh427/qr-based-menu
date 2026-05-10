package com.restaurant.ordering.service;

import java.util.List;

import com.restaurant.ordering.dto.StaffCreateRequest;
import com.restaurant.ordering.dto.StaffResponse;
import com.restaurant.ordering.entity.AppUser;
import com.restaurant.ordering.exception.BadRequestException;
import com.restaurant.ordering.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StaffService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StaffService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<StaffResponse> getStaff(Long restaurantId) {
        return userRepository.findByRestaurantIdOrderByUsernameAsc(restaurantId).stream()
                .map(this::toResponse)
                .toList();
    }

    public StaffResponse createStaff(Long restaurantId, StaffCreateRequest request) {
        String username = request.username().trim();
        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Username already exists: " + username);
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setRestaurantId(restaurantId);
        return toResponse(userRepository.save(user));
    }

    private StaffResponse toResponse(AppUser user) {
        return new StaffResponse(user.getId(), user.getUsername(), user.getRole(), user.getRestaurantId());
    }
}

package com.example.carsharingapp.service.user;

import com.example.carsharingapp.dto.user.UserRegistrationRequestDto;
import com.example.carsharingapp.dto.user.UserResponseDto;
import com.example.carsharingapp.exception.RegistrationException;
import com.example.carsharingapp.mapper.UserMapper;
import com.example.carsharingapp.model.Role;
import com.example.carsharingapp.model.User;
import com.example.carsharingapp.repository.RoleRepository;
import com.example.carsharingapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new RegistrationException(
                    "User already exists with email: " + requestDto.getEmail()
            );
        }
        User user = userMapper.toModel(requestDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role role = roleRepository.findByName(Role.Roles.ROLE_CUSTOMER)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Role is not found: " + Role.Roles.ROLE_CUSTOMER)
                );
        user.setRoles(Set.of(role));

        userRepository.save(user);

        return userMapper.toDto(user);
    }
}

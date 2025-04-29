package com.example.carsharingapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.carsharingapp.dto.user.UpdateUserProfileRequestDto;
import com.example.carsharingapp.dto.user.UpdateUserRoleRequestDto;
import com.example.carsharingapp.dto.user.UserRegistrationRequestDto;
import com.example.carsharingapp.dto.user.UserResponseDto;
import com.example.carsharingapp.exception.RegistrationException;
import com.example.carsharingapp.mapper.UserMapper;
import com.example.carsharingapp.model.Role;
import com.example.carsharingapp.model.User;
import com.example.carsharingapp.repository.RoleRepository;
import com.example.carsharingapp.repository.UserRepository;
import com.example.carsharingapp.service.user.UserServiceImpl;
import com.example.carsharingapp.utils.TestDataUtil;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserServiceImpl userService;

    @Test
    @DisplayName("register with new email returns expected UserResponseDto")
    void register_withValidRequest_returnsDto() throws RegistrationException {
        UserRegistrationRequestDto req = TestDataUtil.createUserRegistrationRequestDto();
        User userModel = TestDataUtil.user(
                1L,
                req.getEmail(),
                req.getFirstName(),
                req.getLastName(),
                req.getPassword(),
                TestDataUtil.role(1L, Role.Roles.ROLE_CUSTOMER));
        UserResponseDto expected = TestDataUtil.mapToUserResponseDto(userModel);

        when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(userMapper.toModel(req)).thenReturn(userModel);
        when(passwordEncoder.encode(req.getPassword())).thenReturn("encodedPwd");
        when(roleRepository.findByName(Role.Roles.ROLE_CUSTOMER))
                .thenReturn(Optional.of(TestDataUtil.role(1L, Role.Roles.ROLE_CUSTOMER)));
        when(userMapper.toDto(userModel)).thenReturn(expected);

        UserResponseDto actual = userService.register(req);

        assertThat(actual).isEqualTo(expected);
        verify(userRepository).existsByEmail(req.getEmail());
        verify(userMapper).toModel(req);
        verify(passwordEncoder).encode(req.getPassword());
        verify(roleRepository).findByName(Role.Roles.ROLE_CUSTOMER);
        verify(userRepository).save(userModel);
        verify(userMapper).toDto(userModel);
    }

    @Test
    @DisplayName("register with existing email throws RegistrationException")
    void register_existingEmail_throwsException() {
        UserRegistrationRequestDto req = TestDataUtil.createUserRegistrationRequestDto();
        when(userRepository.existsByEmail(req.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(req))
                .isInstanceOf(RegistrationException.class)
                .hasMessage("User already exists with email: " + req.getEmail());
        verify(userRepository).existsByEmail(req.getEmail());
    }

    @Test
    @DisplayName("updateRole with valid id returns updated UserResponseDto")
    void updateRole_withValidId_returnsDto() {
        long userId = 2L;
        UpdateUserRoleRequestDto req = TestDataUtil.createUpdateUserRoleRequestDto();
        User existing = TestDataUtil.user(
                userId,
                "a@x.com",
                "First",
                "Last",
                "pwd",
                TestDataUtil.role(1L, Role.Roles.ROLE_CUSTOMER));
        Role newRole = TestDataUtil.role(2L, req.getRole());
        UserResponseDto expected = TestDataUtil.mapToUserResponseDto(
                existing.setRoles(Set.of(newRole))
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(existing));
        when(roleRepository.findByName(req.getRole())).thenReturn(Optional.of(newRole));
        when(userMapper.toDto(existing)).thenReturn(expected);

        UserResponseDto actual = userService.updateRole(userId, req);

        assertThat(actual).isEqualTo(expected);
        verify(userRepository).findById(userId);
        verify(roleRepository).findByName(req.getRole());
    }

    @Test
    @DisplayName("getProfile with existing email returns UserResponseDto")
    void getProfile_withValidEmail_returnsDto() {
        String email = "user@domain.com";
        User user = TestDataUtil.user(
                3L,
                email,
                "John",
                "Doe",
                "pwd",
                TestDataUtil.role(1L, Role.Roles.ROLE_CUSTOMER));
        UserResponseDto expected = TestDataUtil.mapToUserResponseDto(user);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(expected);

        UserResponseDto actual = userService.getProfile(email);

        assertThat(actual).isEqualTo(expected);
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("updateProfile with valid email updates and returns UserResponseDto")
    void updateProfile_withValidEmail_returnsDto() {
        String email = "jane@doe.com";
        UpdateUserProfileRequestDto req = TestDataUtil.createUpdateUserProfileRequestDto();
        User user = TestDataUtil.user(
                4L,
                email,
                "OldFirst",
                "OldLast",
                "pwd",
                TestDataUtil.role(1L, Role.Roles.ROLE_CUSTOMER));
        UserResponseDto expected = TestDataUtil.mapToUserResponseDto(
                user.setFirstName(req.getFirstName()).setLastName(req.getLastName())
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        doAnswer(inv -> {
            TestDataUtil.applyProfileUpdate(req, user);
            return null;
        }).when(userMapper).updateProfileFromDto(req, user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(expected);

        UserResponseDto actual = userService.updateProfile(email, req);

        assertThat(actual).isEqualTo(expected);
        verify(userRepository).findByEmail(email);
        verify(userMapper).updateProfileFromDto(req, user);
        verify(userRepository).save(user);
    }
}

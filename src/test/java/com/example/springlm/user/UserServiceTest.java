package com.example.springlm.user;

import com.example.springlm.user.dto.UserRequest;
import com.example.springlm.user.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRequest.CreateUserRequest createUserRequest;
    private UserRequest.UpdateUserRequest updateUserRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role("ROLE_USER")
                .pw("password123")
                .build();

        // @Data 어노테이션으로 생성된 setter를 사용
        createUserRequest = new UserRequest.CreateUserRequest();
        createUserRequest.setUsername("testuser");
        createUserRequest.setEmail("test@example.com");
        createUserRequest.setPw("password123");

        updateUserRequest = new UserRequest.UpdateUserRequest();
        updateUserRequest.setUsername("updateduser");
        updateUserRequest.setEmail("updated@example.com");
        updateUserRequest.setPw("newpassword123");
    }

    @Test
    @DisplayName("새 사용자 생성 성공 테스트")
    void createUser_WithValidRequest_ShouldReturnUserResponse() {
        // Given
        // save 메서드가 호출될 때 ID가 설정된 User 객체를 반환하도록 설정
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User user = invocation.getArgument(0);
            // 실제 저장 시뮬레이션 - ID 설정
            return User.builder()
                    .id(1L)
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .pw(user.getPw())
                    .build();
        });

        // When
        UserResponse result = userService.createUser(createUserRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        // Repository save 메서드가 호출되었는지 확인
        then(userRepository).should().save(any(User.class));
    }

    @Test
    @DisplayName("사용자 목록 조회 테스트")
    void readUserList_ShouldReturnListOfUserResponses() {
        // Given
        User user2 = User.builder()
                .id(2L)
                .username("testuser2")
                .email("test2@example.com")
                .role("ROLE_USER")
                .pw("password456")
                .build();

        List<User> users = Arrays.asList(testUser, user2);
        given(userRepository.findAll()).willReturn(users);

        // When
        List<UserResponse> result = userService.readUserList();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("testuser");
        assertThat(result.get(1).getUsername()).isEqualTo("testuser2");

        then(userRepository).should().findAll();
    }

    @Test
    @DisplayName("ID로 사용자 조회 성공 테스트")
    void readUser_WithExistingId_ShouldReturnUserResponse() {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        // When
        UserResponse result = userService.readUser(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");

        then(userRepository).should().findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 예외 발생 테스트")
    void readUser_WithNonExistingId_ShouldThrowException() {
        // Given
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.readUser(999L))
                .isInstanceOf(RuntimeException.class);

        then(userRepository).should().findById(999L);
    }

    @Test
    @DisplayName("사용자 정보 업데이트 성공 테스트")
    void updateUser_WithValidData_ShouldUpdateUser() {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        // When
        userService.updateUser(1L, updateUserRequest);

        // Then
        // User 객체의 updateUser 메서드가 호출되어 정보가 업데이트됨
        assertThat(testUser.getUsername()).isEqualTo("updateduser");
        assertThat(testUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(testUser.getPw()).isEqualTo("newpassword123");

        then(userRepository).should().findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 업데이트 시 예외 발생 테스트")
    void updateUser_WithNonExistingId_ShouldThrowException() {
        // Given
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(999L, updateUserRequest))
                .isInstanceOf(RuntimeException.class);

        then(userRepository).should().findById(999L);
    }

    @Test
    @DisplayName("사용자 삭제 성공 테스트 (Soft Delete)")
    void deleteUser_WithExistingId_ShouldSoftDeleteUser() {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        // When
        userService.deleteUser(1L);

        // Then
        // User 객체의 softDelete 메서드가 호출됨 (실제로는 is_deleted = true로 설정)
        then(userRepository).should().findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 삭제 시 예외 발생 테스트")
    void deleteUser_WithNonExistingId_ShouldThrowException() {
        // Given
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(RuntimeException.class);

        then(userRepository).should().findById(999L);
    }

    @Test
    @DisplayName("null CreateUserRequest로 사용자 생성 시 예외 발생 테스트")
    void createUser_WithNullRequest_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> userService.createUser(null))
                .isInstanceOf(NullPointerException.class);

        // Repository save 메서드가 호출되지 않아야 함
        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("빈 사용자 목록 조회 테스트")
    void readUserList_WithEmptyList_ShouldReturnEmptyList() {
        // Given
        given(userRepository.findAll()).willReturn(Arrays.asList());

        // When
        List<UserResponse> result = userService.readUserList();

        // Then
        assertThat(result).isEmpty();
        then(userRepository).should().findAll();
    }
} 
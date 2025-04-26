package com.example.springlm.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository 테스트")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .role("USER")
                .pw("password123")
                .build();
        testUser = userRepository.save(testUser);
        entityManager.flush();
    }

    @Test
    @DisplayName("사용자 저장 및 조회 테스트")
    void saveAndFindUser() {
        // given - setUp에서 이미 생성됨

        // when
        entityManager.clear();
        Optional<User> foundUser = userRepository.findById(testUser.getId());

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.get().getRole()).isEqualTo("USER");
    }

    @Test
    @DisplayName("사용자명으로 사용자 조회 테스트")
    void findByUsername() {
        // given - setUp에서 이미 생성됨
        entityManager.clear();

        // when
        Optional<User> foundUser = userRepository.findByUsername("testuser");

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("존재하지 않는 사용자명으로 조회 시 빈 Optional 반환")
    void findByUsername_NotFound() {
        // when
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");

        // then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("이메일로 사용자 조회 테스트")
    void findByEmail() {
        // given - setUp에서 이미 생성됨
        entityManager.clear();

        // when
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회 시 빈 Optional 반환")
    void findByEmail_NotFound() {
        // when
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("사용자 정보 수정 테스트")
    void updateUser() {
        // given - setUp에서 이미 생성됨
        entityManager.clear();

        // when
        User foundUser = userRepository.findById(testUser.getId()).orElseThrow();
        foundUser.updatOauthUser("updateduser", "updated@example.com");
        userRepository.save(foundUser);
        entityManager.flush();
        entityManager.clear();

        // then
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getUsername()).isEqualTo("updateduser");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    @DisplayName("사용자 삭제 테스트 (Soft Delete)")
    void deleteUser() {
        // given - setUp에서 이미 생성됨
        entityManager.clear();

        // when
        userRepository.deleteById(testUser.getId());
        entityManager.flush();
        entityManager.clear();

        // then (Soft Delete이므로 실제로는 조회되지 않음)
        Optional<User> foundUser = userRepository.findById(testUser.getId());
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("중복 이메일 사용자 저장 테스트")
    void saveDuplicateEmail() {
        // given - setUp에서 이미 testUser가 생성됨
        User user2 = User.builder()
                .username("user2")
                .email("test@example.com")  // 동일한 이메일
                .role("USER")
                .pw("password456")
                .build();

        // when & then
        // JPA는 기본적으로 중복 제약조건을 검증하지 않으므로 저장은 성공
        // 실제 비즈니스 로직에서 중복 검증이 필요
        User savedUser2 = userRepository.save(user2);
        assertThat(savedUser2.getId()).isNotNull();
    }

    @Test
    @DisplayName("여러 사용자 저장 및 조회 테스트")
    void saveMultipleUsers() {
        // given
        User user2 = User.builder()
                .username("user2")
                .email("user2@example.com")
                .role("ADMIN")
                .pw("password456")
                .build();
        
        User user3 = User.builder()
                .username("user3")
                .email("user3@example.com")
                .role("USER")
                .pw("password789")
                .build();

        // when
        List<User> savedUsers = userRepository.saveAll(List.of(user2, user3));
        entityManager.flush();
        entityManager.clear();

        // then
        List<User> allUsers = userRepository.findAll();
        assertThat(allUsers).hasSize(3); // setUp의 testUser + 2명
        assertThat(allUsers)
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder("testuser", "user2", "user3");
    }
}

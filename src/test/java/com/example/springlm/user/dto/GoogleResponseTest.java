package com.example.springlm.user.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GoogleReponse DTO 테스트")
class GoogleResponseTest {

    private Map<String, Object> validGoogleAttributes;

    @BeforeEach
    void setUp() {
        // 유효한 Google OAuth2 응답 데이터 설정
        validGoogleAttributes = new HashMap<>();
        validGoogleAttributes.put("sub", "google123456");
        validGoogleAttributes.put("name", "Test User");
        validGoogleAttributes.put("email", "test@gmail.com");
        validGoogleAttributes.put("picture", "https://example.com/profile.jpg");
        validGoogleAttributes.put("email_verified", true);
        validGoogleAttributes.put("locale", "ko");
    }

    @Test
    @DisplayName("유효한 Google 응답 데이터로 GoogleReponse 생성 테스트")
    void createGoogleResponse_WithValidAttributes_ShouldReturnCorrectData() {
        // When
        GoogleReponse googleResponse = new GoogleReponse(validGoogleAttributes);

        // Then
        assertThat(googleResponse.getProviderId()).isEqualTo("google123456");
        assertThat(googleResponse.getEmail()).isEqualTo("test@gmail.com");
        assertThat(googleResponse.getName()).isEqualTo("Test User");
        assertThat(googleResponse.getProvider()).isEqualTo("google");
    }

    @Test
    @DisplayName("Google 응답에서 providerId 추출 테스트")
    void getProviderId_ShouldReturnSubValue() {
        // Given
        GoogleReponse googleResponse = new GoogleReponse(validGoogleAttributes);

        // When
        String providerId = googleResponse.getProviderId();

        // Then
        assertThat(providerId).isEqualTo("google123456");
    }

    @Test
    @DisplayName("Google 응답에서 email 추출 테스트")
    void getEmail_ShouldReturnEmailValue() {
        // Given
        GoogleReponse googleResponse = new GoogleReponse(validGoogleAttributes);

        // When
        String email = googleResponse.getEmail();

        // Then
        assertThat(email).isEqualTo("test@gmail.com");
    }

    @Test
    @DisplayName("Google 응답에서 name 추출 테스트")
    void getName_ShouldReturnNameValue() {
        // Given
        GoogleReponse googleResponse = new GoogleReponse(validGoogleAttributes);

        // When
        String name = googleResponse.getName();

        // Then
        assertThat(name).isEqualTo("Test User");
    }

    @Test
    @DisplayName("provider는 항상 'google'을 반환하는지 테스트")
    void getProvider_ShouldAlwaysReturnGoogle() {
        // Given
        GoogleReponse googleResponse = new GoogleReponse(validGoogleAttributes);

        // When
        String provider = googleResponse.getProvider();

        // Then
        assertThat(provider).isEqualTo("google");
    }

    @Test
    @DisplayName("null attributes로 GoogleReponse 생성 시 예외 발생하지 않음 테스트")
    void createGoogleResponse_WithNullAttributes_ShouldNotThrowException() {
        // When & Then
        assertThatCode(() -> new GoogleReponse(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("빈 attributes로 GoogleReponse 생성 테스트")
    void createGoogleResponse_WithEmptyAttributes_ShouldHandleGracefully() {
        // Given
        Map<String, Object> emptyAttributes = new HashMap<>();

        // When
        GoogleReponse googleResponse = new GoogleReponse(emptyAttributes);

        // Then
        assertThat(googleResponse.getProvider()).isEqualTo("google");
        // 빈 맵에서는 null이 반환될 수 있음
        assertThat(googleResponse.getProviderId()).isNull();
        assertThat(googleResponse.getEmail()).isNull();
        assertThat(googleResponse.getName()).isNull();
    }

    @Test
    @DisplayName("동일한 데이터로 생성된 두 GoogleReponse 객체의 데이터 일치 테스트")
    void createMultipleGoogleResponse_WithSameData_ShouldReturnSameValues() {
        // When
        GoogleReponse response1 = new GoogleReponse(validGoogleAttributes);
        GoogleReponse response2 = new GoogleReponse(validGoogleAttributes);

        // Then
        assertThat(response1.getProviderId()).isEqualTo(response2.getProviderId());
        assertThat(response1.getEmail()).isEqualTo(response2.getEmail());
        assertThat(response1.getName()).isEqualTo(response2.getName());
        assertThat(response1.getProvider()).isEqualTo(response2.getProvider());
    }
} 
package com.example.springlm.integration;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 환경변수 테스트
 * .env 파일 로딩 및 Spring 프로퍼티 바인딩 검증
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("환경변수 설정 테스트 - .env 파일 로딩 및 프로퍼티 바인딩 검증")
class EnvironmentConfigTest {

    @Autowired
    private Environment environment;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    private static final String TEST_ENV_FILE = ".env.test";
    private static Path testEnvPath;

    @BeforeAll
    static void setUpTestEnvFile() throws IOException {
        // 테스트용 .env 파일 생성
        testEnvPath = Paths.get(TEST_ENV_FILE);
        
        String envContent = """
                # Test Environment Variables
                JWT_SECRET_KEY=test_jwt_secret_key_for_testing_64_chars_minimum_required_for_hs512_algorithm
                GOOGLE_CLIENT_ID=test_google_client_id_for_integration_test
                GOOGLE_CLIENT_SECRET=test_google_client_secret_for_integration_test
                
                # Database Configuration
                DB_HOST=localhost
                DB_PORT=3306
                DB_NAME=testdb
                DB_USERNAME=testuser
                DB_PASSWORD=testpass
                
                # Application Configuration
                APP_NAME=SpringLM Integration Test
                APP_VERSION=1.0.0-TEST
                APP_ENVIRONMENT=test
                """;
        
        Files.writeString(testEnvPath, envContent);
        System.out.println("Created test .env file: " + testEnvPath.toAbsolutePath());
    }

    @AfterAll
    static void cleanUpTestEnvFile() throws IOException {
        // 테스트용 .env 파일 삭제
        if (Files.exists(testEnvPath)) {
            Files.delete(testEnvPath);
            System.out.println("Deleted test .env file: " + testEnvPath.toAbsolutePath());
        }
    }

    @Test
    @Order(1)
    @DisplayName("Spring Environment 기본 설정 확인")
    void testSpringEnvironmentBasicConfig() {
        // Spring Environment가 정상적으로 주입되었는지 확인
        assertThat(environment).isNotNull();
        
        // 활성 프로파일 확인
        String[] activeProfiles = environment.getActiveProfiles();
        assertThat(activeProfiles).contains("test");
        
        // 기본 Spring Boot 프로퍼티 확인
        String applicationName = environment.getProperty("spring.application.name");
        System.out.println("Application Name: " + applicationName);
        
        // 테스트 프로파일 전용 프로퍼티 확인
        String datasourceUrl = environment.getProperty("spring.datasource.url");
        assertThat(datasourceUrl).contains("testdb");
        System.out.println("Datasource URL: " + datasourceUrl);
    }

    @Test
    @Order(2)
    @DisplayName("JWT 설정 프로퍼티 바인딩 검증")
    void testJWTConfigPropertyBinding() {
        // @Value로 주입된 JWT 설정 확인
        assertThat(jwtSecret).isNotNull();
        assertThat(jwtSecret).isNotEmpty();
        assertThat(jwtSecret).isEqualTo("test_jwt_secret_key_for_testing_64_chars_minimum_required_for_hs512_algorithm");
        
        // Environment를 통한 JWT 설정 확인
        String jwtSecretFromEnv = environment.getProperty("jwt.secret");
        assertThat(jwtSecretFromEnv).isEqualTo(jwtSecret);
        
        // JWT 만료 시간 설정 확인
        String jwtExpiration = environment.getProperty("jwt.expiration");
        assertThat(jwtExpiration).isNotNull();
        System.out.println("JWT Expiration: " + jwtExpiration + "ms");
    }

    @Test
    @Order(3)
    @DisplayName("OAuth2 Google 설정 프로퍼티 바인딩 검증")
    void testOAuth2GoogleConfigPropertyBinding() {
        // @Value로 주입된 Google OAuth2 설정 확인
        assertThat(googleClientId).isNotNull();
        assertThat(googleClientId).isEqualTo("test_google_client_id");
        
        assertThat(googleClientSecret).isNotNull();
        assertThat(googleClientSecret).isEqualTo("test_google_client_secret");
        
        // Environment를 통한 OAuth2 설정 확인
        String clientIdFromEnv = environment.getProperty("spring.security.oauth2.client.registration.google.client-id");
        String clientSecretFromEnv = environment.getProperty("spring.security.oauth2.client.registration.google.client-secret");
        
        assertThat(clientIdFromEnv).isEqualTo(googleClientId);
        assertThat(clientSecretFromEnv).isEqualTo(googleClientSecret);
        
        // Redirect URI 설정 확인
        String redirectUri = environment.getProperty("spring.security.oauth2.client.registration.google.redirect-uri");
        assertThat(redirectUri).contains("localhost:8080");
        System.out.println("OAuth2 Redirect URI: " + redirectUri);
    }

    @Test
    @Order(4)
    @DisplayName("Dotenv 라이브러리 직접 테스트")
    void testDotenvLibraryDirect() {
        // Dotenv 라이브러리로 테스트 .env 파일 직접 로딩
        Dotenv dotenv = Dotenv.configure()
                .filename(TEST_ENV_FILE)
                .ignoreIfMissing()
                .load();
        
        // .env 파일에서 값 읽기
        String jwtSecretFromDotenv = dotenv.get("JWT_SECRET_KEY");
        String googleClientIdFromDotenv = dotenv.get("GOOGLE_CLIENT_ID");
        String googleClientSecretFromDotenv = dotenv.get("GOOGLE_CLIENT_SECRET");
        
        assertThat(jwtSecretFromDotenv).isEqualTo("test_jwt_secret_key_for_testing_64_chars_minimum_required_for_hs512_algorithm");
        assertThat(googleClientIdFromDotenv).isEqualTo("test_google_client_id_for_integration_test");
        assertThat(googleClientSecretFromDotenv).isEqualTo("test_google_client_secret_for_integration_test");
        
        // 추가 환경변수 확인
        String appName = dotenv.get("APP_NAME");
        String appVersion = dotenv.get("APP_VERSION");
        String appEnvironment = dotenv.get("APP_ENVIRONMENT");
        
        assertThat(appName).isEqualTo("SpringLM Integration Test");
        assertThat(appVersion).isEqualTo("1.0.0-TEST");
        assertThat(appEnvironment).isEqualTo("test");
        
        System.out.println("=== Dotenv Properties ===");
        System.out.println("App Name: " + appName);
        System.out.println("App Version: " + appVersion);
        System.out.println("App Environment: " + appEnvironment);
    }

    @Test
    @Order(5)
    @DisplayName("데이터베이스 설정 프로퍼티 검증")
    void testDatabaseConfigProperties() {
        // H2 테스트 데이터베이스 설정 확인 (test 프로파일)
        String datasourceUrl = environment.getProperty("spring.datasource.url");
        String datasourceDriver = environment.getProperty("spring.datasource.driver-class-name");
        String datasourceUsername = environment.getProperty("spring.datasource.username");
        
        assertThat(datasourceUrl).contains("h2:mem:testdb");
        assertThat(datasourceDriver).isEqualTo("org.h2.Driver");
        assertThat(datasourceUsername).isEqualTo("sa");
        
        // JPA 설정 확인
        String ddlAuto = environment.getProperty("spring.jpa.hibernate.ddl-auto");
        String showSql = environment.getProperty("spring.jpa.show-sql");
        
        assertThat(ddlAuto).isEqualTo("create-drop");
        assertThat(showSql).isEqualTo("false");
        
        System.out.println("=== Database Configuration ===");
        System.out.println("Datasource URL: " + datasourceUrl);
        System.out.println("Driver: " + datasourceDriver);
        System.out.println("DDL Auto: " + ddlAuto);
        System.out.println("Show SQL: " + showSql);
    }

    @Test
    @Order(6)
    @DisplayName("로깅 설정 프로퍼티 검증")
    void testLoggingConfigProperties() {
        // 로깅 레벨 설정 확인
        String rootLogLevel = environment.getProperty("logging.level.root");
        String springlmLogLevel = environment.getProperty("logging.level.com.example.springlm");
        String securityLogLevel = environment.getProperty("logging.level.org.springframework.security");
        
        assertThat(rootLogLevel).isEqualTo("WARN");
        assertThat(springlmLogLevel).isEqualTo("INFO");
        assertThat(securityLogLevel).isEqualTo("WARN");
        
        // 로깅 패턴 확인
        String consolePattern = environment.getProperty("logging.pattern.console");
        assertThat(consolePattern).isNotNull();
        assertThat(consolePattern).contains("%d{HH:mm:ss.SSS}");
        
        System.out.println("=== Logging Configuration ===");
        System.out.println("Root Log Level: " + rootLogLevel);
        System.out.println("SpringLM Log Level: " + springlmLogLevel);
        System.out.println("Security Log Level: " + securityLogLevel);
    }

    @Test
    @Order(7)
    @DisplayName("환경별 프로파일 설정 검증")
    void testProfileSpecificConfiguration() {
        // 현재 활성 프로파일 확인
        String[] activeProfiles = environment.getActiveProfiles();
        assertThat(activeProfiles).contains("test");
        
        // 기본 프로파일 확인
        String[] defaultProfiles = environment.getDefaultProfiles();
        System.out.println("Default Profiles: " + String.join(", ", defaultProfiles));
        
        // 프로파일별 프로퍼티 소스 확인
        for (String profile : activeProfiles) {
            System.out.println("Active Profile: " + profile);
        }
        
        // test 프로파일 전용 설정이 올바르게 적용되었는지 확인
        assertThat(environment.acceptsProfiles("test")).isTrue();
        assertThat(environment.acceptsProfiles("local")).isFalse();
        assertThat(environment.acceptsProfiles("prod")).isFalse();
    }

    @Test
    @Order(8)
    @DisplayName("시스템 프로퍼티 및 환경변수 통합 테스트")
    void testSystemPropertiesAndEnvironmentVariables() {
        // Java 시스템 프로퍼티 확인
        String javaVersion = System.getProperty("java.version");
        String osName = System.getProperty("os.name");
        
        assertThat(javaVersion).isNotNull();
        assertThat(osName).isNotNull();
        
        System.out.println("=== System Information ===");
        System.out.println("Java Version: " + javaVersion);
        System.out.println("OS Name: " + osName);
        
        // Spring Boot에서 시스템 프로퍼티 접근
        String javaVersionFromSpring = environment.getProperty("java.version");
        assertThat(javaVersionFromSpring).isEqualTo(javaVersion);
        
        // 환경변수 확인 (존재할 경우)
        String pathEnv = System.getenv("PATH");
        if (pathEnv != null) {
            assertThat(pathEnv).isNotEmpty();
            System.out.println("PATH Environment Variable exists");
        }
    }

    @Test
    @Order(9)
    @DisplayName("프로퍼티 우선순위 테스트")
    void testPropertyPrecedence() {
        // Spring Boot의 프로퍼티 우선순위 테스트
        // 1. application-test.yml의 값이 우선 적용되는지 확인
        
        String jwtSecret = environment.getProperty("jwt.secret");
        assertThat(jwtSecret).isEqualTo("test_jwt_secret_key_for_testing_64_chars_minimum_required_for_hs512_algorithm");
        
        // 2. 프로파일별 설정이 기본 설정보다 우선하는지 확인
        String datasourceUrl = environment.getProperty("spring.datasource.url");
        assertThat(datasourceUrl).contains("testdb"); // test 프로파일의 설정
        
        System.out.println("=== Property Precedence Test ===");
        System.out.println("JWT Secret (from test profile): " + jwtSecret);
        System.out.println("Datasource URL (from test profile): " + datasourceUrl);
    }

    @Test
    @Order(10)
    @DisplayName("환경설정 통계 및 요약")
    void testEnvironmentConfigurationSummary() {
        System.out.println("\n=== Environment Configuration Summary ===");
        
        // 활성 프로파일
        String[] activeProfiles = environment.getActiveProfiles();
        System.out.println("Active Profiles: " + String.join(", ", activeProfiles));
        
        // 주요 설정 값들
        System.out.println("JWT Secret Length: " + jwtSecret.length());
        System.out.println("Google Client ID: " + googleClientId);
        System.out.println("Database URL: " + environment.getProperty("spring.datasource.url"));
        System.out.println("JPA DDL Auto: " + environment.getProperty("spring.jpa.hibernate.ddl-auto"));
        System.out.println("Root Log Level: " + environment.getProperty("logging.level.root"));
        
        // 검증
        assertThat(activeProfiles).isNotEmpty();
        assertThat(jwtSecret.length()).isGreaterThan(50); // 충분한 길이의 시크릿 키
        assertThat(googleClientId).startsWith("test_");
        
        System.out.println("=== All Environment Configuration Tests Passed ===\n");
    }
}

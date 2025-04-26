package com.example.springlm.config;

import com.example.springlm.user.CustomOAuth2UserService;
import com.example.springlm.user.User;
import com.example.springlm.user.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import lombok.RequiredArgsConstructor;
import java.io.IOException;
import java.util.Map;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.example.springlm.config.JwtAuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import java.io.IOException;

@Configuration
@EnableWebSecurity
@Slf4j
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final UserRepository userRepository;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        try {

            log.info("Configuring Security Filter Chain");

            http
                    .csrf((csrf) -> csrf.disable());

            http
                    .formLogin((login) -> login.disable());

            http
                    .httpBasic((basic) -> basic.disable());

            // OAuth2 로그인을 위해 세션 사용 (JWT는 로그인 후에만 사용)
            http
                    .sessionManagement((session) -> session
                            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                            .maximumSessions(1)
                            .maxSessionsPreventsLogin(false));

            http
                    .oauth2Login((oauth2) -> oauth2
                            .loginPage("/login")
                            .userInfoEndpoint((userInfoEndpointConfig) ->
                                    userInfoEndpointConfig.userService(customOAuth2UserService))
                            .successHandler(oauth2AuthenticationSuccessHandler())
                    );

            // 정적 리소스 및 로그인 페이지에 대한 접근 허용 규칙
            http
                    .authorizeHttpRequests((auth) -> auth
                            .requestMatchers("/monitoring/**", "/unified-monitoring").permitAll()  // 모니터링 엔드포인트 명시적 허용
                            .requestMatchers("/check-proto", "/login", "/css/**", "/js/**", "/images/**", "/oauth2/**", "/debug-all", "/project/**", "/api/project/**").permitAll()
                            .anyRequest().authenticated()
                    );  // 그 외 모든 요청은 인증 필요

            // JWT 필터를 OAuth2 로그인 이후에만 적용되도록 조건부 추가
            http
                    .addFilterAfter(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

            // HTTPS 인식을 위한 설정 (dev, prod 환경에서 적용)
            if ("dev".equals(activeProfile) || "prod".equals(activeProfile)) {
                log.info("Enabling HTTPS requirement for {} environment", activeProfile);
                http.requiresChannel(channel -> channel
                        .anyRequest().requiresSecure());
            }

            return http.build();

        } catch (Exception e) {
            log.error("Error configuring Security Filter Chain", e);
            throw e; // 중요한 설정 예외는 그대로 던져서 애플리케이션 시작을 막음
        }
    }

    @Bean
    public AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler() {
        return new SimpleUrlAuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                                                HttpServletResponse response,
                                                Authentication authentication) throws IOException, ServletException {

                log.info("OAuth2 인증 성공, JWT 토큰 생성 시작");

                if (authentication.getPrincipal() instanceof CustomOAuth2UserService.CustomOAuth2User) {
                    CustomOAuth2UserService.CustomOAuth2User oauth2User =
                            (CustomOAuth2UserService.CustomOAuth2User) authentication.getPrincipal();

                    // JWT 토큰 생성
                    String jwt = customOAuth2UserService.generateJwtToken(oauth2User.getUserId());
                    log.info("JWT 토큰 생성 완료");

                    // JWT를 쿠키에 설정
                    Cookie jwtCookie = new Cookie("jwt", jwt);
                    jwtCookie.setHttpOnly(false);
                    jwtCookie.setSecure("dev".equals(activeProfile) || "prod".equals(activeProfile));
                    jwtCookie.setPath("/");
                    jwtCookie.setMaxAge(86400); // 24시간
                    response.addCookie(jwtCookie);
                    
                    log.info("JWT 쿠키 설정 완료");
                }

                // 홈페이지로 리디렉션 (무한 루프 방지)
                response.sendRedirect("/");
            }
        };
    }
}

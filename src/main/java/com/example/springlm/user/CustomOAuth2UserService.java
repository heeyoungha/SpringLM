package com.example.springlm.user;

import com.example.springlm.user.User;
import com.example.springlm.user.UserRepository;
import com.example.springlm.user.dto.GoogleReponse;
import com.example.springlm.user.dto.NaverResponse;
import com.example.springlm.user.dto.OAuth2Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    //DefaultOAuth2UserService OAuth2UserService의 구현체

    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${jwt.expiration.ms:86400000}")
    private long jwtExpirationMs;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("=== CustomOAuth2UserService.loadUser 시작 ===");
        
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("OAuth2User 로드 완료: {}", oAuth2User.getName());

        String provider = userRequest.getClientRegistration().getRegistrationId();
        log.info("Provider: {}", provider);

        OAuth2Response oAuth2Response = null;
        if (provider.equals("naver")) {
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        } else if (provider.equals("google")) {
            oAuth2Response = new GoogleReponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        String username = oAuth2Response.getName();
        log.info("Username: {}", username);
        
        log.info("=== 사용자 조회 시작 ===");
        User existData = userRepository.findByUsername(username).orElse(null);

        log.info("기존 사용자 조회 결과: {}", existData != null ? "존재함" : "존재하지 않음");

        String role = "ROLE_USER";
        User user;
        
        if (existData == null) {
            log.info("=== 새 사용자 생성 시작 ===");
            user = User.builder()
                    .username(username)
                    .email(oAuth2Response.getEmail())
                    .role(role)
                    .build();
            log.info("새 사용자 객체 생성: {}", user);

            log.info("=== 사용자 저장 시작 ===");
            user = userRepository.save(user);
            log.info("사용자 저장 완료 - ID: {}, Username: {}", user.getId(), user.getUsername());
            
            // 트랜잭션 강제 커밋 확인
            log.info("=== 트랜잭션 강제 커밋 시작 ===");
            entityManager.flush();
            entityManager.clear();
            log.info("트랜잭션 flush/clear 완료");
            
            // 저장 후 다시 조회해서 확인
            User savedUser = userRepository.findById(user.getId()).orElse(null);
            log.info("저장 후 조회 결과: {}", savedUser != null ? "성공" : "실패");
            if (savedUser != null) {
                log.info("저장된 사용자 정보 - ID: {}, Username: {}, Email: {}", 
                    savedUser.getId(), savedUser.getUsername(), savedUser.getEmail());
            }
            
        } else {
            log.info("=== 기존 사용자 업데이트 시작 ===");
            existData.updatOauthUser(username, oAuth2Response.getEmail());
            role = existData.getRole();
            log.info("기존 사용자 업데이트 완료");

            user = userRepository.save(existData);
            log.info("기존 사용자 저장 완료 - ID: {}, Username: {}", user.getId(), user.getUsername());
            
            // 트랜잭션 강제 커밋 확인
            log.info("=== 트랜잭션 강제 커밋 시작 ===");
            entityManager.flush();
            entityManager.clear();
            log.info("트랜잭션 flush/clear 완료");
        }

        log.info("=== CustomOAuth2UserService.loadUser 완료 ===");
        return new CustomOAuth2User(oAuth2Response, role, user.getId());
    }

    public String generateJwtToken(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
            .setSubject(user.getId().toString())
            .claim("username", user.getUsername())
            .claim("email", user.getEmail())
            .claim("role", user.getRole())
            .setIssuedAt(new Date())
            .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
            .signWith(key, SignatureAlgorithm.HS512)
            .compact();
    }

    public static class CustomOAuth2User implements OAuth2User {

        private final OAuth2Response oAuth2Response;
        private final String role;
        private final Long userId;

        public CustomOAuth2User(OAuth2Response oAuth2Response, String role, Long id) {

            this.oAuth2Response = oAuth2Response;
            this.role = role;
            this.userId = id;
        }

        public Long getUserId() {
            return userId;
        }

        @Override
        public Map<String, Object> getAttributes() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("name", oAuth2Response.getName());
            attributes.put("email", oAuth2Response.getEmail());
            attributes.put("provider", oAuth2Response.getProvider());
            attributes.put("providerId", oAuth2Response.getProviderId());
            return attributes;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {

            Collection<GrantedAuthority> collection = new ArrayList<>();

            collection.add(new GrantedAuthority() {

                @Override
                public String getAuthority() {

                    return role;
                }
            });

            return collection;
        }

        @Override
        public String getName() {

            return oAuth2Response.getName();
        }

        public String getUsername() {

            return oAuth2Response.getProvider()+" "+oAuth2Response.getProviderId();
        }
    }
}

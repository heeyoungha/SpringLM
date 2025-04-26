package com.example.springlm.login;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(){
        return "login/login";
    }

    @GetMapping("/check-proto")
    @ResponseBody
    public String checkProto(HttpServletRequest request) {
        boolean secure = request.isSecure();
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        String forwardedFor = request.getHeader("X-Forwarded-For");
        String realIp = request.getHeader("X-Real-IP");
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        return String.format("""
            HTTPS Test Results:
            - isSecure(): %s
            - X-Forwarded-Proto: %s
            - X-Forwarded-For: %s
            - X-Real-IP: %s
            - Scheme: %s
            - Server Name: %s
            - Server Port: %d
            """,
            secure, forwardedProto, forwardedFor, realIp, scheme, serverName, serverPort);
    }

    @GetMapping("/debug-all")
    @ResponseBody
    public Map<String, Object> debugAll() {
        Map<String, Object> result = new HashMap<>();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        result.put("authentication", auth != null ? auth.toString() : null);
        if (auth != null) {
            result.put("isAuthenticated", auth.isAuthenticated());
            result.put("principal", auth.getPrincipal());
            result.put("authorities", auth.getAuthorities());
            result.put("auth.name", auth.getName());
        }
        return result;
    }
}

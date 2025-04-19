package com.example.springlm.common;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServiceUtil {
    
    public static <T, ID> T findByIdOrThrow(JpaRepository<T, ID> repository, ID id, RuntimeException exception) {
        return repository.findById(id).orElseThrow(() -> exception);
    }
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(FORMATTER);
    }
    
    public static String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
} 
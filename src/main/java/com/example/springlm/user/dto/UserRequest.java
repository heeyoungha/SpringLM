package com.example.study.user.dto;

import lombok.Data;

public class UserRequest {

    @Data
    public static class CreateUserRequest {
        private String username;
        private String email;
        private String pw;
    }

    @Data
    public static class UpdateUserRequest {
        private String username;
        private String pw;
        private String email;
    }
}

package com.example.springlm.user;

import com.example.springlm.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE users SET is_deleted = true WHERE id = ?")
public class User extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    private String role;
    private String pw;

    public void updateUser(com.example.study.user.dto.UserRequest.UpdateUserRequest request) {
        this.pw = request.getPw();
        this.email = request.getEmail();
        this.username = request.getUsername();
    }
    public void updatOauthUser(String name, String email){
        this.username = name;
        this.email = email;
    }
}

package com.project.hugme.domain.user.entity;

import com.ethlo.time.DateTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;

@Entity
@Table(name="users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(unique=true,nullable = false)
    private String email;

    @Column(length=255)
    private String password;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name="role", nullable=false,length=20)
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(name= "status",nullable=false,length=20)
    private UserStatus status = UserStatus.ACTIVE;

    @CreationTimestamp
    @Column(name="created_at",nullable=false,updatable=false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name="updated_at",nullable=false)
    private Instant updatedAt;

    @Column(name="deleted_at")
    private Instant deletedAt;

    public static User createLocalUser(
            String email,
            String encodedPassword,
            String name
    ){
        User user = new User();
        user.email = email;
        user.password=encodedPassword;
        user.name=name;
        user.role=UserRole.USER;
        user.status=UserStatus.ACTIVE;

        return user;
    }


    public void withdraw() {
        this.status = UserStatus.WITHDRAWN;
        this.deletedAt = Instant.now();
    }



}

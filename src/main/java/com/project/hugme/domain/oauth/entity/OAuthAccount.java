package com.project.hugme.domain.oauth.entity;

import com.project.hugme.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter
@Entity
@Table(name = "oauth_accounts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuthAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "oauth_account_id")
    private Long oauthAccountId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "provider",
            nullable = false,
            length = 20
    )
    private OAuthProvider provider;

    @Column(
            name = "provider_user_id",
            nullable = false,
            length = 255
    )
    private String providerUserId;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @UpdateTimestamp
    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    private OAuthAccount(
            User user,
            OAuthProvider provider,
            String providerUserId
    ) {
        this.user = user;
        this.provider = provider;
        this.providerUserId = providerUserId;
    }

    public static OAuthAccount create(
            User user,
            OAuthProvider provider,
            String providerUserId
    ) {
        return new OAuthAccount(
                user,
                provider,
                providerUserId
        );
    }
}
package com.project.hugme.domain.chatbot.guide.entity;

import com.project.hugme.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Entity
@Table(name = "guide_chat_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GuideChatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    @Column(name = "category", nullable = false, length = 20)
    private String category;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(name = "sources", columnDefinition = "TEXT")
    private String sources;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private GuideChatHistory(User user, String sessionId, String category, String question, String answer, String sources) {
        this.user = user;
        this.sessionId = sessionId;
        this.category = category;
        this.question = question;
        this.answer = answer;
        this.sources = sources;
    }

    public static GuideChatHistory create(User user, String sessionId, String category, String question, String answer, String sources) {
        return new GuideChatHistory(user, sessionId, category, question, answer, sources);
    }
}
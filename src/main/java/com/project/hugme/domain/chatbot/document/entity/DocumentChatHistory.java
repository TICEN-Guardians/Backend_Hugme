package com.project.hugme.domain.chatbot.document.entity;

import com.project.hugme.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Entity
@Table(name = "document_chat_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentChatHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    @Column(name = "application_id")
    private Long applicationId;

    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(name = "sources", columnDefinition = "TEXT")
    private String sources;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private DocumentChatHistory(User user, String sessionId, Long applicationId, Long documentId, String question, String answer, String sources) {
        this.user = user;
        this.sessionId = sessionId;
        this.applicationId = applicationId;
        this.documentId = documentId;
        this.question = question;
        this.answer = answer;
        this.sources = sources;
    }

    public static DocumentChatHistory create(User user, String sessionId, Long applicationId, Long documentId, String question, String answer, String sources) {
        return new DocumentChatHistory(user, sessionId, applicationId, documentId, question, answer, sources);
    }
}

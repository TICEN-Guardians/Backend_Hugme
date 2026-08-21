package com.project.hugme.domain.checklist.entity.application;

import com.project.hugme.domain.chatbot.document.entity.DocumentPreparationCheck;
import com.project.hugme.domain.checklist.entity.product.Product;
import com.project.hugme.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "applications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long applicationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_id",
            nullable = false
    )
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "application_status",
            nullable = false,
            length = 20
    )
    private ApplicationStatus applicationStatus;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @OneToOne(
            mappedBy = "application",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true
    )
    private ApplicationInfo applicationInfo;

    @OneToMany(
            mappedBy = "application",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true
    )
    private List<ChecklistDocumentResult> checklistDocumentResults =
            new ArrayList<>();

    @OneToMany(
            mappedBy = "application",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true
    )
    private List<DocumentPreparationCheck> documentPreparationChecks =
            new ArrayList<>();





    public static Application create(
            User user,
            Product product
    ) {
        Application application = new Application();

        application.user = user;
        application.product = product;
        application.applicationStatus =
                ApplicationStatus.PROGRESS;

        return application;
    }

    public static Application prepare(
            User user,
            Product product
    ) {
        Application application =
                new Application();

        application.user = user;
        application.product = product;
        application.applicationStatus =
                ApplicationStatus.READY;

        return application;
    }



    public void complete() {
        this.applicationStatus = ApplicationStatus.DONE;
    }

}
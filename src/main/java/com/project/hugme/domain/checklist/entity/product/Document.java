package com.project.hugme.domain.checklist.entity.product;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "documents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long documentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "item_id",
            nullable = false
    )
    private ChecklistItem item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_group_id")
    private DocumentGroup documentGroup;

    @Column(
            name = "document_name",
            nullable = false,
            length = 200
    )
    private String documentName;

    @Column(
            name = "description",
            columnDefinition = "TEXT"
    )
    private String description;


    @Column(
            name = "issuer",
            length = 200
    )
    private String issuer;

    @Column(
            name = "validity_rule",
            length = 300
    )
    private String validityRule;

    @Column(
            name = "submission_form",
            length = 100
    )
    private String submissionForm;

    @Column(
            name = "sample_image_url",
            length = 1000
    )
    private String sampleImageUrl;

    @Column(
            name = "sort_order",
            nullable = false
    )
    private Integer sortOrder;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "document_housing_types",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "housing_type_id")
    )
    private Set<HousingType> housingTypes = new HashSet<>();
}
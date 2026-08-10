package com.project.hugme.domain.checklist.entity.product;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "document_groups",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_document_groups_item_order",
                        columnNames = {"item_id", "sort_order"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DocumentGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_group_id")
    private Long documentGroupId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "item_id",
            nullable = false
    )
    private ChecklistItem item;

    @Column(
            name = "group_name",
            nullable = false,
            length = 300
    )
    private String groupName;

    @Column(
            name = "sort_order",
            nullable = false
    )
    private Integer sortOrder;
}
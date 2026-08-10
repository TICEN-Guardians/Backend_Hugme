package com.project.hugme.domain.checklist.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "checklist_groups",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_checklist_groups_section_order",
                        columnNames = {"section_id", "sort_order"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChecklistGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "section_id",
            nullable = false
    )
    private ChecklistSection section;

    @Column(
            name = "group_name",
            nullable = false,
            length = 100
    )
    private String groupName;

    @Column(
            name = "sort_order",
            nullable = false
    )
    private Integer sortOrder;
}

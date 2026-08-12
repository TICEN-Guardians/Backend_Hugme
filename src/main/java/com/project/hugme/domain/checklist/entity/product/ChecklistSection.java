package com.project.hugme.domain.checklist.entity.product;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "checklist_sections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChecklistSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id")
    private Long sectionId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "section_code",
            nullable = false,
            unique = true,
            length = 30
    )
    private SectionCode sectionCode;

    @Column(
            name = "section_name",
            nullable = false,
            length = 100
    )
    private String sectionName;
}

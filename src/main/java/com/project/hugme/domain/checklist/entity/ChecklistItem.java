package com.project.hugme.domain.checklist.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "checklist_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChecklistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "section_id",
            nullable = false
    )
    private ChecklistSection section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private ChecklistGroup group;

    @Column(
            name = "item_name",
            nullable = false,
            length = 200
    )
    private String itemName;

    @Column(
            name = "sort_order",
            nullable = false
    )
    private Integer sortOrder;

    @Column(
            name = "default_included",
            nullable = false
    )
    private boolean defaultIncluded;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "checklist_item_products",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<Product> products = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "checklist_item_housing_types",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "housing_type_id")
    )
    private Set<HousingType> housingTypes = new HashSet<>();
}
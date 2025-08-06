package com.chiragbhisikar.e_commerce.model;

import jakarta.persistence.*;

@Entity
public class SubCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @JoinColumn(name = "category_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    @Column(unique = true, nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    @Column(unique = true, nullable = false, columnDefinition = "TEXT")
    private String description;
}
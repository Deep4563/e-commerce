package com.chiragbhisikar.e_commerce.model;

import jakarta.persistence.*;

@Entity
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    @Column(unique = true, nullable = false, columnDefinition = "TEXT")
    private String description;
}

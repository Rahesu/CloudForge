package com.cloudforge.application.entity;

import jakarta.persistence.*;

@Entity
@Table
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String runtime;
    private String version;
    private String status;

}

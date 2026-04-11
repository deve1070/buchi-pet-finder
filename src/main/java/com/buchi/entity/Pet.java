package com.buchi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique = true)
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "pet_type")
    private PetType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "pet_gender")
    @Builder.Default
    private PetGender gender = PetGender.unknown;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "pet_size")
    private PetSize size;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "pet_age")
    private PetAge age;

    @Column(name = "good_with_children", nullable = false)
    private Boolean goodWithChildren;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    private String status = "available";

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<PetPhoto> photos = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // ── Enums ─────────────────────────────────────────────────────────────────

    public enum PetType   { Cat, Dog, Bird, Rabbit, Fish, Other }
    public enum PetGender { male, female, unknown }
    public enum PetSize   { small, medium, large, xlarge }
    public enum PetAge    { baby, young, adult, senior }
}
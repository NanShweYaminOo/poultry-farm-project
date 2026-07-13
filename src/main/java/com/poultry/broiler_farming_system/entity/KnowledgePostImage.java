package com.poultry.broiler_farming_system.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "knowledge_post_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class KnowledgePostImage {

    // The source schema declares no primary key for this table; a surrogate
    // id is added here since JPA entities require one.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "knowledge_post_id", nullable = false)
    @ToString.Exclude
    private KnowledgePost knowledgePost;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;
}

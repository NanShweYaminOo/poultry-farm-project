package com.poultry.broiler_farming_system.entity;

import com.poultry.broiler_farming_system.entity.enums.KnowledgePostStatus;
import com.poultry.broiler_farming_system.entity.enums.KnowledgePostType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "knowledge_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class KnowledgePost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    @ToString.Exclude
    private User admin;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false, length = 30)
    private KnowledgePostType postType;

    @Column(name = "title_en", nullable = false, length = 255)
    private String titleEn;

    @Column(name = "title_my", nullable = false, length = 255)
    private String titleMy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private KnowledgePostStatus status = KnowledgePostStatus.PUBLISHED;

    @Lob
    @Column(name = "content_en", nullable = false, columnDefinition = "TEXT")
    private String contentEn;

    @Lob
    @Column(name = "content_my", nullable = false, columnDefinition = "TEXT")
    private String contentMy;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @OneToMany(mappedBy = "knowledgePost", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    private List<KnowledgePostImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "knowledgePost", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    private List<KnowledgePostDocument> documents = new ArrayList<>();
}

package com.wealthpro.productcatalog.entity;

import com.wealthpro.productcatalog.enums.ResearchRating;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "research_notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResearchNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "note_id")
    private Long noteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "security_id", nullable = false)
    private Security security;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "rating", nullable = false)
    private ResearchRating rating;

    @Column(name = "published_date", nullable = false)
    private LocalDate publishedDate;

    @Column(name = "content_uri", nullable = false, length = 500)
    private String contentUri;

    @Column(name = "analyst", length = 100)
    private String analyst;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
}
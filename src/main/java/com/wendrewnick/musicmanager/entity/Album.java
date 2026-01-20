package com.wendrewnick.musicmanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "albums")
public class Album extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(name = "release_year")
    private Integer year;

    @Column(name = "cover_url")
    private String coverUrl;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "album_artists", joinColumns = @JoinColumn(name = "album_id"), inverseJoinColumns = @JoinColumn(name = "artist_id"))
    private Set<Artist> artists;
}

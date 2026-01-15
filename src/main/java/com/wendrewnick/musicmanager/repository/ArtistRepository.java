package com.wendrewnick.musicmanager.repository;

import com.wendrewnick.musicmanager.entity.Artist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, UUID> {
    Page<Artist> findByNameContainingIgnoreCase(String name, Pageable pageable);
}

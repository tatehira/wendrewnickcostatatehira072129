package com.wendrewnick.musicmanager.repository;

import com.wendrewnick.musicmanager.entity.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AlbumRepository extends JpaRepository<Album, UUID> {
    Page<Album> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("SELECT a FROM Album a JOIN a.artists art WHERE LOWER(art.name) LIKE LOWER(CONCAT('%', :artistName, '%'))")
    Page<Album> findByArtistsNameContainingIgnoreCase(String artistName, Pageable pageable);
}

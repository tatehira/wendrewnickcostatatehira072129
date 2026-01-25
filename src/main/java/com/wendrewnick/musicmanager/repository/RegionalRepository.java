package com.wendrewnick.musicmanager.repository;

import com.wendrewnick.musicmanager.entity.Regional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegionalRepository extends JpaRepository<Regional, UUID> {
    List<Regional> findByAtivoTrue();

    Optional<Regional> findByRegionalIdAndAtivoTrue(Integer regionalId);

    List<Regional> findByNomeContainingIgnoreCase(String nome);

    List<Regional> findByRegionalId(Integer regionalId);

    List<Regional> findByAtivo(boolean ativo);

    List<Regional> findByNomeContainingIgnoreCaseAndAtivo(String nome, boolean ativo);
}

package com.topfilmesbrasil.repository;

import com.topfilmesbrasil.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SerieRepository extends JpaRepository<Serie, Long> {
    // Similar ao FilmeRepository, consultas para "Top Séries" ou "Novas Séries"
    // seriam adicionadas aqui ou implementadas no service.
    // List<Serie> findTop10ByOrderByDataCriacaoDesc();
    List<Serie> findByEmDestaqueTrue();
}
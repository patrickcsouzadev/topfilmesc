package com.topfilmesbrasil.repository;

import com.topfilmesbrasil.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // Adicione esta importação
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByFilmeIdOrderByDataCriacaoDesc(Long filmeId);

    List<Review> findBySerieIdOrderByDataCriacaoDesc(Long serieId);

    List<Review> findByUsuarioId(Long usuarioId);

    @Transactional
    void deleteByFilmeId(Long filmeId);

    @Transactional
    void deleteBySerieId(Long serieId);
}
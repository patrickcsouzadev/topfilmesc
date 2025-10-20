package com.topfilmesbrasil.repository;

import com.topfilmesbrasil.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // Adicione esta importação
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByFilmeIdOrderByDataCriacaoDesc(Long filmeId);

    List<Review> findBySerieIdOrderByDataCriacaoDesc(Long serieId);

    @Query("SELECT r FROM Review r WHERE r.usuario.email = :emailUsuario ORDER BY r.dataCriacao DESC")
    List<Review> findByUsuarioEmailOrderByDataCriacaoDesc(@Param("emailUsuario") String emailUsuario);

    List<Review> findByUsuarioId(Long usuarioId);

    @Transactional
    void deleteByFilmeId(Long filmeId);

    @Transactional
    void deleteBySerieId(Long serieId);
    
    @Transactional
    void deleteByUsuarioId(Long usuarioId);
}
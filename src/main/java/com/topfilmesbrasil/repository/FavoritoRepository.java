package com.topfilmesbrasil.repository;

import com.topfilmesbrasil.model.Favorito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    // Buscar todos os favoritos de um usuário
    List<Favorito> findByUsuarioIdOrderByDataAdicaoDesc(Long usuarioId);

    // Verificar se um filme específico está nos favoritos do usuário
    Optional<Favorito> findByUsuarioIdAndFilmeId(Long usuarioId, Long filmeId);

    // Verificar se uma série específica está nos favoritos do usuário
    Optional<Favorito> findByUsuarioIdAndSerieId(Long usuarioId, Long serieId);

    // Buscar apenas filmes favoritos de um usuário
    @Query("SELECT f FROM Favorito f WHERE f.usuario.id = :usuarioId AND f.filme IS NOT NULL ORDER BY f.dataAdicao DESC")
    List<Favorito> findFilmesFavoritosByUsuarioId(@Param("usuarioId") Long usuarioId);

    // Buscar apenas séries favoritas de um usuário
    @Query("SELECT f FROM Favorito f WHERE f.usuario.id = :usuarioId AND f.serie IS NOT NULL ORDER BY f.dataAdicao DESC")
    List<Favorito> findSeriesFavoritasByUsuarioId(@Param("usuarioId") Long usuarioId);

    // Em FavoritoRepository.java
    @Transactional
    void deleteByFilmeId(Long filmeId);

    @Transactional
    void deleteBySerieId(Long serieId);

    // Contar total de favoritos de um usuário
    long countByUsuarioId(Long usuarioId);

    // Deletar favorito específico
    void deleteByUsuarioIdAndFilmeId(Long usuarioId, Long filmeId);
    void deleteByUsuarioIdAndSerieId(Long usuarioId, Long serieId);
    
    // Deletar todos os favoritos de um usuário
    @Transactional
    void deleteByUsuarioId(Long usuarioId);
}
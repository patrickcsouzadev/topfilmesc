package com.topfilmesbrasil.service;

import com.topfilmesbrasil.dto.FilmeDTO;
import com.topfilmesbrasil.model.Filme;
import java.util.List;
import java.util.Optional;

public interface FilmeService {
    Filme salvar(Filme filme); // Admin
    Optional<FilmeDTO> findByIdDTO(Long id);
    Optional<Filme> findById(Long id); // Para uso interno, se necessário
    List<FilmeDTO> findAllDTO();
    List<FilmeDTO> buscarTopAvaliados(int limit); // Para "Top Avaliações"
    List<FilmeDTO> buscarNovosLancamentos(int limit); // Para "Novas Releases"
    List<FilmeDTO> buscarDestaques();
    void deletarFilme(Long id); // Admin
}
package com.topfilmesbrasil.service;

import com.topfilmesbrasil.dto.SerieDTO;
import com.topfilmesbrasil.model.Serie;
import java.util.List;
import java.util.Optional;

public interface SerieService {
    Serie salvar(Serie serie);
    Optional<SerieDTO> findByIdDTO(Long id);
    Optional<Serie> findById(Long id);
    List<SerieDTO> findAllDTO();
    List<SerieDTO> buscarTopAvaliadas(int limit);
    List<SerieDTO> buscarNovosLancamentos(int limit);
    List<SerieDTO> buscarDestaques();
    void deletarSerie(Long id);
}
package com.topfilmesbrasil.service;

import com.topfilmesbrasil.dto.FavoritoDTO;
import java.util.List;

public interface FavoritoService {

    FavoritoDTO adicionarFilmeAosFavoritos(Long filmeId, String emailUsuario);
    FavoritoDTO adicionarSerieAosFavoritos(Long serieId, String emailUsuario);

    void removerFilmeDosFavoritos(Long filmeId, String emailUsuario);
    void removerSerieDosFavoritos(Long serieId, String emailUsuario);

    List<FavoritoDTO> listarFavoritosDoUsuario(String emailUsuario);
    List<FavoritoDTO> listarFilmesFavoritosDoUsuario(String emailUsuario);
    List<FavoritoDTO> listarSeriesFavoritasDoUsuario(String emailUsuario);

    boolean isFilmeFavorito(Long filmeId, String emailUsuario);
    boolean isSerieFavorita(Long serieId, String emailUsuario);

    long contarFavoritosDoUsuario(String emailUsuario);
}
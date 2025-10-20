package com.topfilmesbrasil.service.impl;

import com.topfilmesbrasil.dto.FavoritoDTO;
import com.topfilmesbrasil.model.*;
import com.topfilmesbrasil.repository.*;
import com.topfilmesbrasil.service.FavoritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class FavoritoServiceImpl implements FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final FilmeRepository filmeRepository;
    private final SerieRepository serieRepository;
    private final ReviewRepository reviewRepository;

    @Autowired
    public FavoritoServiceImpl(FavoritoRepository favoritoRepository,
                               UsuarioRepository usuarioRepository,
                               FilmeRepository filmeRepository,
                               SerieRepository serieRepository,
                               ReviewRepository reviewRepository) {
        this.favoritoRepository = favoritoRepository;
        this.usuarioRepository = usuarioRepository;
        this.filmeRepository = filmeRepository;
        this.serieRepository = serieRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public FavoritoDTO adicionarFilmeAosFavoritos(Long filmeId, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Filme filme = filmeRepository.findById(filmeId)
                .orElseThrow(() -> new RuntimeException("Filme não encontrado"));

        if (favoritoRepository.findByUsuarioIdAndFilmeId(usuario.getId(), filmeId).isPresent()) {
            throw new RuntimeException("Filme já está nos favoritos");
        }

        Favorito favorito = new Favorito(usuario, filme);
        favorito = favoritoRepository.save(favorito);

        return convertToDTO(favorito);
    }

    @Override
    public FavoritoDTO adicionarSerieAosFavoritos(Long serieId, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Serie serie = serieRepository.findById(serieId)
                .orElseThrow(() -> new RuntimeException("Série não encontrada"));

        if (favoritoRepository.findByUsuarioIdAndSerieId(usuario.getId(), serieId).isPresent()) {
            throw new RuntimeException("Série já está nos favoritos");
        }

        Favorito favorito = new Favorito(usuario, serie);
        favorito = favoritoRepository.save(favorito);

        return convertToDTO(favorito);
    }

    @Override
    public void removerFilmeDosFavoritos(Long filmeId, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        favoritoRepository.deleteByUsuarioIdAndFilmeId(usuario.getId(), filmeId);
    }

    @Override
    public void removerSerieDosFavoritos(Long serieId, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        favoritoRepository.deleteByUsuarioIdAndSerieId(usuario.getId(), serieId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoritoDTO> listarFavoritosDoUsuario(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return favoritoRepository.findByUsuarioIdOrderByDataAdicaoDesc(usuario.getId())
                .stream()
                .map(this::convertToDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoritoDTO> listarFilmesFavoritosDoUsuario(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return favoritoRepository.findFilmesFavoritosByUsuarioId(usuario.getId())
                .stream()
                .map(this::convertToDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoritoDTO> listarSeriesFavoritasDoUsuario(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return favoritoRepository.findSeriesFavoritasByUsuarioId(usuario.getId())
                .stream()
                .map(this::convertToDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFilmeFavorito(Long filmeId, String emailUsuario) {
        return usuarioRepository.findByEmail(emailUsuario)
                .map(usuario -> favoritoRepository.findByUsuarioIdAndFilmeId(usuario.getId(), filmeId).isPresent())
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSerieFavorita(Long serieId, String emailUsuario) {
        return usuarioRepository.findByEmail(emailUsuario)
                .map(usuario -> favoritoRepository.findByUsuarioIdAndSerieId(usuario.getId(), serieId).isPresent())
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarFavoritosDoUsuario(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return favoritoRepository.countByUsuarioId(usuario.getId());
    }

    private FavoritoDTO convertToDTO(Favorito favorito) {
        if (favorito == null || (favorito.getFilme() == null && favorito.getSerie() == null)) {
            return null;
        }

        String titulo;
        String urlPoster;
        Integer anoLancamento;
        String tipoConteudo;
        Long conteudoId;
        Double mediaRating = 0.0;
        Set<String> listaGeneros = new HashSet<>();

        if (favorito.getFilme() != null) {
            Filme filme = favorito.getFilme();
            titulo = filme.getTitulo();
            urlPoster = filme.getUrlPoster();
            anoLancamento = filme.getAnoLancamento();
            tipoConteudo = "filme";
            conteudoId = filme.getId();
            listaGeneros = filme.getListaGeneros();

            List<Review> reviews = reviewRepository.findByFilmeIdOrderByDataCriacaoDesc(filme.getId());
            if (!reviews.isEmpty()) {
                mediaRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
            }
        } else {
            Serie serie = favorito.getSerie();
            titulo = serie.getTitulo();
            urlPoster = serie.getUrlPoster();
            anoLancamento = serie.getAnoLancamento();
            tipoConteudo = "serie";
            conteudoId = serie.getId();
            listaGeneros = serie.getListaGeneros();

            List<Review> reviews = reviewRepository.findBySerieIdOrderByDataCriacaoDesc(serie.getId());
            if (!reviews.isEmpty()) {
                mediaRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
            }
        }

        FavoritoDTO dto = new FavoritoDTO(
                favorito.getId(),
                favorito.getUsuario().getId(),
                conteudoId,
                tipoConteudo,
                titulo,
                urlPoster,
                anoLancamento,
                mediaRating,
                favorito.getDataAdicao()
        );
        dto.setListaGeneros(listaGeneros);
        
        // Definir IDs específicos para filmes e séries
        if (favorito.getFilme() != null) {
            dto.setFilmeId(favorito.getFilme().getId());
            dto.setSerieId(null);
        } else {
            dto.setFilmeId(null);
            dto.setSerieId(favorito.getSerie().getId());
        }

        return dto;
    }
}
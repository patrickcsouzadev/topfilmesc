package com.topfilmesbrasil.service.impl;

import com.topfilmesbrasil.dto.RatingInfoDTO;
import com.topfilmesbrasil.dto.SerieDTO;
import com.topfilmesbrasil.model.Review;
import com.topfilmesbrasil.model.Serie;
import com.topfilmesbrasil.repository.FavoritoRepository;
import com.topfilmesbrasil.repository.ReviewRepository;
import com.topfilmesbrasil.repository.SerieRepository;
import com.topfilmesbrasil.service.SerieService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SerieServiceImpl implements SerieService {

    private final SerieRepository serieRepository;
    private final ReviewRepository reviewRepository;
    private final FavoritoRepository favoritoRepository;

    @Autowired
    public SerieServiceImpl(SerieRepository serieRepository, ReviewRepository reviewRepository, FavoritoRepository favoritoRepository) {
        this.serieRepository = serieRepository;
        this.reviewRepository = reviewRepository;
        this.favoritoRepository = favoritoRepository;
    }

    @Override
    public Serie salvar(Serie serie) {
        return serieRepository.save(serie);
    }

    @Override
    public Optional<SerieDTO> findByIdDTO(Long id) {
        return serieRepository.findById(id).map(this::convertToFullDTO);
    }

    @Override
    public Optional<Serie> findById(Long id) {
        return serieRepository.findById(id);
    }

    @Override
    public List<SerieDTO> findAllDTO() {
        return serieRepository.findAll(Sort.by(Sort.Direction.DESC, "dataCriacao")).stream()
                .map(this::convertToFullDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SerieDTO> buscarTopAvaliadas(int limit) {
        return serieRepository.findAll().stream()
                .map(this::convertToFullDTO)
                .sorted((s1, s2) -> Double.compare(
                        s2.getMediaRating() != null ? s2.getMediaRating() : 0.0,
                        s1.getMediaRating() != null ? s1.getMediaRating() : 0.0
                ))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<SerieDTO> buscarNovosLancamentos(int limit) {
        return serieRepository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "dataCriacao")))
                .getContent().stream()
                .map(this::convertToFullDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SerieDTO> buscarDestaques() {
        return serieRepository.findByEmDestaqueTrue().stream()
                .map(this::convertToFullDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deletarSerie(Long id) {
        if (!serieRepository.existsById(id)) {
            throw new RuntimeException("Série não encontrada com id: " + id);
        }
        favoritoRepository.deleteBySerieId(id);
        reviewRepository.deleteBySerieId(id);
        serieRepository.deleteById(id);
    }

    private SerieDTO convertToFullDTO(Serie serie) {
        SerieDTO dto = new SerieDTO();
        // CORREÇÃO: Ignora o campo 'reviews' que não existe no DTO
        BeanUtils.copyProperties(serie, dto, "mediaRating", "ratingInfo", "reviews");

        // O resto do método preenche os outros campos manualmente...
        dto.setId(serie.getId());
        dto.setTitulo(serie.getTitulo());
        dto.setDescricao(serie.getDescricao());
        dto.setAnoLancamento(serie.getAnoLancamento());
        dto.setGenero(serie.getGenero());
        dto.setListaGeneros(serie.getListaGeneros());
        dto.setUrlPoster(serie.getUrlPoster());
        dto.setUrlBackgroundImage(serie.getUrlBackgroundImage());
        dto.setUrlTrailer(serie.getUrlTrailer());
        dto.setClassificacaoIndicativa(serie.getClassificacaoIndicativa());
        dto.setElenco(serie.getElenco());
        dto.setDataCriacao(serie.getDataCriacao());
        dto.setNumeroTemporadas(serie.getNumeroTemporadas());
        dto.setNumeroEpisodios(serie.getNumeroEpisodios());
        dto.setStatus(serie.getStatus());
        dto.setCriador(serie.getCriador());

        List<Review> reviews = reviewRepository.findBySerieIdOrderByDataCriacaoDesc(serie.getId());
        if (!reviews.isEmpty()) {
            double mediaCalculada1a5 = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
            dto.setRatingInfo(new RatingInfoDTO(mediaCalculada1a5 * 2, reviews.size()));
            dto.setMediaRating(mediaCalculada1a5);
        } else {
            dto.setRatingInfo(new RatingInfoDTO(0.0, 0));
            dto.setMediaRating(0.0);
        }
        return dto;
    }
}
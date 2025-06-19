package com.topfilmesbrasil.service.impl;

import com.topfilmesbrasil.dto.FilmeDTO;
import com.topfilmesbrasil.dto.RatingInfoDTO;
import com.topfilmesbrasil.model.Filme;
import com.topfilmesbrasil.model.Review;
import com.topfilmesbrasil.repository.FavoritoRepository;
import com.topfilmesbrasil.repository.FilmeRepository;
import com.topfilmesbrasil.repository.ReviewRepository;
import com.topfilmesbrasil.service.FilmeService;
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
public class FilmeServiceImpl implements FilmeService {

    private final FilmeRepository filmeRepository;
    private final ReviewRepository reviewRepository;
    private final FavoritoRepository favoritoRepository;

    @Autowired
    public FilmeServiceImpl(FilmeRepository filmeRepository, ReviewRepository reviewRepository, FavoritoRepository favoritoRepository) {
        this.filmeRepository = filmeRepository;
        this.reviewRepository = reviewRepository;
        this.favoritoRepository = favoritoRepository;
    }

    @Override
    public Filme salvar(Filme filme) {
        return filmeRepository.save(filme);
    }

    @Override
    public Optional<FilmeDTO> findByIdDTO(Long id) {
        return filmeRepository.findById(id).map(this::convertToFullDTO);
    }

    @Override
    public Optional<Filme> findById(Long id) {
        return filmeRepository.findById(id);
    }

    @Override
    public List<FilmeDTO> findAllDTO() {
        return filmeRepository.findAll(Sort.by(Sort.Direction.DESC, "dataCriacao")).stream()
                .map(this::convertToFullDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FilmeDTO> buscarTopAvaliados(int limit) {
        return filmeRepository.findAll().stream()
                .map(this::convertToFullDTO)
                .sorted((f1, f2) -> Double.compare(
                        f2.getMediaRating() != null ? f2.getMediaRating() : 0.0,
                        f1.getMediaRating() != null ? f1.getMediaRating() : 0.0
                ))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<FilmeDTO> buscarNovosLancamentos(int limit) {
        return filmeRepository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "dataCriacao")))
                .getContent().stream()
                .map(this::convertToFullDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FilmeDTO> buscarDestaques() {
        return filmeRepository.findByEmDestaqueTrue().stream()
                .map(this::convertToFullDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deletarFilme(Long id) {
        if (!filmeRepository.existsById(id)) {
            throw new RuntimeException("Filme não encontrado com id: " + id);
        }
        favoritoRepository.deleteByFilmeId(id);
        reviewRepository.deleteByFilmeId(id);
        filmeRepository.deleteById(id);
    }

    private FilmeDTO convertToFullDTO(Filme filme) {
        FilmeDTO dto = new FilmeDTO();
        BeanUtils.copyProperties(filme, dto, "mediaRating", "ratingInfo", "duracao", "reviews");

        dto.setId(filme.getId());
        dto.setTitulo(filme.getTitulo());
        dto.setDescricao(filme.getDescricao());
        dto.setAnoLancamento(filme.getAnoLancamento());
        dto.setGenero(filme.getGenero());
        dto.setListaGeneros(filme.getListaGeneros());
        dto.setUrlPoster(filme.getUrlPoster());
        dto.setUrlBackgroundImage(filme.getUrlBackgroundImage());
        dto.setUrlTrailer(filme.getUrlTrailer());
        dto.setClassificacaoIndicativa(filme.getClassificacaoIndicativa());
        dto.setElenco(filme.getElenco());
        dto.setDataCriacao(filme.getDataCriacao());
        dto.setDuracaoMinutos(filme.getDuracaoMinutos());
        dto.setDiretor(filme.getDiretor());

        List<Review> reviews = reviewRepository.findByFilmeIdOrderByDataCriacaoDesc(filme.getId());
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
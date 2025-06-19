package com.topfilmesbrasil.service.impl;

import com.topfilmesbrasil.dto.CriarReviewDTO;
import com.topfilmesbrasil.dto.ReviewDTO;
import com.topfilmesbrasil.dto.UsuarioDTO;
import com.topfilmesbrasil.model.*;
import com.topfilmesbrasil.repository.FilmeRepository;
import com.topfilmesbrasil.repository.ReviewRepository;
import com.topfilmesbrasil.repository.SerieRepository;
import com.topfilmesbrasil.repository.UsuarioRepository;
import com.topfilmesbrasil.service.ReviewService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UsuarioRepository usuarioRepository;
    private final FilmeRepository filmeRepository;
    private final SerieRepository serieRepository;

    @Autowired
    public ReviewServiceImpl(ReviewRepository reviewRepository, UsuarioRepository usuarioRepository,
                             FilmeRepository filmeRepository, SerieRepository serieRepository) {
        this.reviewRepository = reviewRepository;
        this.usuarioRepository = usuarioRepository;
        this.filmeRepository = filmeRepository;
        this.serieRepository = serieRepository;
    }

    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        BeanUtils.copyProperties(review, dto);
        if (review.getUsuario() != null) {
            dto.setUsuario(new UsuarioDTO(
                    review.getUsuario().getId(),
                    review.getUsuario().getNomeCompleto(),
                    review.getUsuario().getEmail(),
                    review.getUsuario().getRole()
            ));
        }
        if (review.getFilme() != null) {
            dto.setConteudoId(review.getFilme().getId());
            dto.setTipoConteudo("filme");
        } else if (review.getSerie() != null) {
            dto.setConteudoId(review.getSerie().getId());
            dto.setTipoConteudo("serie");
        }
        return dto;
    }

    @Override
    public ReviewDTO criarReviewFilme(Long filmeId, CriarReviewDTO criarReviewDTO, String emailUsuarioLogado) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado para adicionar review."));
        Filme filme = filmeRepository.findById(filmeId)
                .orElseThrow(() -> new RuntimeException("Filme não encontrado para adicionar review."));

        Review review = new Review();
        review.setUsuario(usuario);
        review.setFilme(filme);
        review.setRating(criarReviewDTO.getRating());
        review.setComentario(criarReviewDTO.getComentario());
        // dataCriacao é setada via @PrePersist

        return convertToDTO(reviewRepository.save(review));
    }

    @Override
    public ReviewDTO criarReviewSerie(Long serieId, CriarReviewDTO criarReviewDTO, String emailUsuarioLogado) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado para adicionar review."));
        Serie serie = serieRepository.findById(serieId)
                .orElseThrow(() -> new RuntimeException("Série não encontrada para adicionar review."));

        Review review = new Review();
        review.setUsuario(usuario);
        review.setSerie(serie);
        review.setRating(criarReviewDTO.getRating());
        review.setComentario(criarReviewDTO.getComentario());

        return convertToDTO(reviewRepository.save(review));
    }

    @Override
    public List<ReviewDTO> getReviewsByFilmeId(Long filmeId) {
        return reviewRepository.findByFilmeIdOrderByDataCriacaoDesc(filmeId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewDTO> getReviewsBySerieId(Long serieId) {
        return reviewRepository.findBySerieIdOrderByDataCriacaoDesc(serieId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deletarReview(Long reviewId, String emailUsuarioLogado) {
        Usuario usuarioLogado = usuarioRepository.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new AccessDeniedException("Usuário não autenticado tentando deletar review."));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review não encontrado com id: " + reviewId));

        // Admin pode deletar qualquer review. Usuário normal só pode deletar o seu próprio.
        if (usuarioLogado.getRole() == Role.ROLE_ADMIN || review.getUsuario().getId().equals(usuarioLogado.getId())) {
            reviewRepository.delete(review);
        } else {
            throw new AccessDeniedException("Usuário não tem permissão para deletar este review.");
        }
    }
}
package com.topfilmesbrasil.service;

import com.topfilmesbrasil.dto.CriarReviewDTO;
import com.topfilmesbrasil.dto.ReviewDTO;
import java.util.List;

public interface ReviewService {
    ReviewDTO criarReviewFilme(Long filmeId, CriarReviewDTO criarReviewDTO, String emailUsuarioLogado);
    ReviewDTO criarReviewSerie(Long serieId, CriarReviewDTO criarReviewDTO, String emailUsuarioLogado);
    List<ReviewDTO> getReviewsByFilmeId(Long filmeId);
    List<ReviewDTO> getReviewsBySerieId(Long serieId);
    List<ReviewDTO> getReviewsByUsuarioEmail(String emailUsuario);
    void deletarReview(Long reviewId, String emailUsuarioLogado);
}
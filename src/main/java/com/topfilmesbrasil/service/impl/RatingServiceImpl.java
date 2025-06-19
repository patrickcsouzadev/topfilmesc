package com.topfilmesbrasil.service.impl;

import com.topfilmesbrasil.dto.RatingInfoDTO;
import com.topfilmesbrasil.model.Review;
import com.topfilmesbrasil.repository.ReviewRepository;
import com.topfilmesbrasil.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) // A maioria dos métodos aqui são de leitura
public class RatingServiceImpl implements RatingService {

    private final ReviewRepository reviewRepository;

    @Autowired
    public RatingServiceImpl(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public RatingInfoDTO getRatingInfoParaFilme(Long filmeId) {
        List<Review> reviews = reviewRepository.findByFilmeIdOrderByDataCriacaoDesc(filmeId);
        return calcularRatingInfo(reviews);
    }

    @Override
    public RatingInfoDTO getRatingInfoParaSerie(Long serieId) {
        List<Review> reviews = reviewRepository.findBySerieIdOrderByDataCriacaoDesc(serieId);
        return calcularRatingInfo(reviews);
    }

    private RatingInfoDTO calcularRatingInfo(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return new RatingInfoDTO(0.0, 0);
        }
        double media = reviews.stream()
                .mapToInt(Review::getRating) // Assume que Review tem getRating() retornando int (1-5)
                .average()
                .orElse(0.0);
        // O frontend (modal.js no renderStars) parece esperar um rating de 0-5 (para estrelas)
        // e o ratingInfo.media também. Se a média aqui for 1-5, está ok.
        // Se o frontend espera 0-10 para media, precisaria ajustar.
        // Pelo uso em this.renderStars(content.ratingInfo.media) no modal.js, onde renderStars
        // faz (rating / 10) * 5, parece que o ratingInfo.media deveria ser 0-10.
        // Vou ajustar para retornar a média em escala de 0-10 se o rating for 1-5.
        // Assumindo que Review.getRating() retorna 1-5:
        return new RatingInfoDTO(media * 2, reviews.size()); // Média convertida para escala 0-10
    }
}
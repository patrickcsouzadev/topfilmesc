package com.topfilmesbrasil.controller;

import com.topfilmesbrasil.dto.CriarReviewDTO;
import com.topfilmesbrasil.dto.ReviewDTO;
import com.topfilmesbrasil.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reviews/api")
public class ReviewApiController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewApiController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/filme/{filmeId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsParaFilme(@PathVariable Long filmeId) {
        List<ReviewDTO> reviews = reviewService.getReviewsByFilmeId(filmeId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/serie/{serieId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsParaSerie(@PathVariable Long serieId) {
        List<ReviewDTO> reviews = reviewService.getReviewsBySerieId(serieId);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/filme/{filmeId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> criarReviewFilme(@PathVariable Long filmeId,
                                              @Valid @RequestBody CriarReviewDTO criarReviewDTO,
                                              Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Usuário não autenticado."));
        }
        String emailUsuarioLogado = authentication.getName();
        try {
            ReviewDTO reviewSalvo = reviewService.criarReviewFilme(filmeId, criarReviewDTO, emailUsuarioLogado);
            return ResponseEntity.status(HttpStatus.CREATED).body(reviewSalvo);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Usuário não encontrado."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/serie/{serieId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> criarReviewSerie(@PathVariable Long serieId,
                                              @Valid @RequestBody CriarReviewDTO criarReviewDTO,
                                              Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Usuário não autenticado."));
        }
        String emailUsuarioLogado = authentication.getName();
        try {
            ReviewDTO reviewSalvo = reviewService.criarReviewSerie(serieId, criarReviewDTO, emailUsuarioLogado);
            return ResponseEntity.status(HttpStatus.CREATED).body(reviewSalvo);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Usuário não encontrado."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deletarReview(@PathVariable Long reviewId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Usuário não autenticado."));
        }
        String emailUsuarioLogado = authentication.getName();
        try {
            reviewService.deletarReview(reviewId, emailUsuarioLogado);
            return ResponseEntity.ok(Map.of("message", "Review deletado com sucesso."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }
}
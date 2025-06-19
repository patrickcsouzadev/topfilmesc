package com.topfilmesbrasil.controller;

import com.topfilmesbrasil.service.FilmeService;
import com.topfilmesbrasil.service.SerieService;
import com.topfilmesbrasil.service.ReviewService;
import com.topfilmesbrasil.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.Map;

@RestController
@RequestMapping("/admin/api")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDeleteController {

    private final FilmeService filmeService;
    private final SerieService serieService;
    private final ReviewService reviewService;
    private final UsuarioService usuarioService;

    @Autowired
    public AdminDeleteController(FilmeService filmeService, SerieService serieService,
                                 ReviewService reviewService, UsuarioService usuarioService) {
        this.filmeService = filmeService;
        this.serieService = serieService;
        this.reviewService = reviewService;
        this.usuarioService = usuarioService;
    }

    @DeleteMapping("/filmes/{id}")
    public ResponseEntity<?> deletarFilme(@PathVariable Long id) {
        try {
            filmeService.deletarFilme(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Filme deletado com sucesso!"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/series/{id}")
    public ResponseEntity<?> deletarSerie(@PathVariable Long id) {
        try {
            serieService.deletarSerie(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Série deletada com sucesso!"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<?> deletarReviewAdmin(@PathVariable Long reviewId, Authentication authentication) {
        try {
            String emailAdmin = authentication.getName();
            reviewService.deletarReview(reviewId, emailAdmin);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Review deletado com sucesso!"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> deletarUsuario(@PathVariable Long id, Authentication authentication) {
        try {
            // Verifica se o admin não está tentando deletar a si mesmo
            String emailAdmin = authentication.getName();
            var usuarioAdmin = usuarioService.findByEmail(emailAdmin);

            if (usuarioAdmin.isPresent() && usuarioAdmin.get().getId().equals(id)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Você não pode deletar sua própria conta!"
                ));
            }

            usuarioService.deletarUsuario(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Usuário deletado com sucesso!"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }
}
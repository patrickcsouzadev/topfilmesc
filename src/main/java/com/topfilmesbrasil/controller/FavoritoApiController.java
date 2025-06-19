package com.topfilmesbrasil.controller;

import com.topfilmesbrasil.dto.FavoritoDTO;
import com.topfilmesbrasil.service.FavoritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favoritos")
@PreAuthorize("isAuthenticated()") // Apenas usuários logados
public class FavoritoApiController {

    private final FavoritoService favoritoService;

    @Autowired
    public FavoritoApiController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    @PostMapping("/filme/{filmeId}")
    public ResponseEntity<?> adicionarFilmeAosFavoritos(@PathVariable Long filmeId, Authentication authentication) {
        try {
            String emailUsuario = authentication.getName();
            FavoritoDTO favorito = favoritoService.adicionarFilmeAosFavoritos(filmeId, emailUsuario);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Filme adicionado aos favoritos!",
                    "favorito", favorito
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/serie/{serieId}")
    public ResponseEntity<?> adicionarSerieAosFavoritos(@PathVariable Long serieId, Authentication authentication) {
        try {
            String emailUsuario = authentication.getName();
            FavoritoDTO favorito = favoritoService.adicionarSerieAosFavoritos(serieId, emailUsuario);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Série adicionada aos favoritos!",
                    "favorito", favorito
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/filme/{filmeId}")
    public ResponseEntity<?> removerFilmeDosFavoritos(@PathVariable Long filmeId, Authentication authentication) {
        try {
            String emailUsuario = authentication.getName();
            favoritoService.removerFilmeDosFavoritos(filmeId, emailUsuario);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Filme removido dos favoritos!"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/serie/{serieId}")
    public ResponseEntity<?> removerSerieDosFavoritos(@PathVariable Long serieId, Authentication authentication) {
        try {
            String emailUsuario = authentication.getName();
            favoritoService.removerSerieDosFavoritos(serieId, emailUsuario);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Série removida dos favoritos!"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping
    public ResponseEntity<List<FavoritoDTO>> listarFavoritos(Authentication authentication) {
        String emailUsuario = authentication.getName();
        List<FavoritoDTO> favoritos = favoritoService.listarFavoritosDoUsuario(emailUsuario);
        return ResponseEntity.ok(favoritos);
    }

    @GetMapping("/filmes")
    public ResponseEntity<List<FavoritoDTO>> listarFilmesFavoritos(Authentication authentication) {
        String emailUsuario = authentication.getName();
        List<FavoritoDTO> filmes = favoritoService.listarFilmesFavoritosDoUsuario(emailUsuario);
        return ResponseEntity.ok(filmes);
    }

    @GetMapping("/series")
    public ResponseEntity<List<FavoritoDTO>> listarSeriesFavoritas(Authentication authentication) {
        String emailUsuario = authentication.getName();
        List<FavoritoDTO> series = favoritoService.listarSeriesFavoritasDoUsuario(emailUsuario);
        return ResponseEntity.ok(series);
    }

    @GetMapping("/filme/{filmeId}/check")
    public ResponseEntity<?> verificarFilmeFavorito(@PathVariable Long filmeId, Authentication authentication) {
        String emailUsuario = authentication.getName();
        boolean isFavorito = favoritoService.isFilmeFavorito(filmeId, emailUsuario);
        return ResponseEntity.ok(Map.of("isFavorito", isFavorito));
    }

    @GetMapping("/serie/{serieId}/check")
    public ResponseEntity<?> verificarSerieFavorita(@PathVariable Long serieId, Authentication authentication) {
        String emailUsuario = authentication.getName();
        boolean isFavorito = favoritoService.isSerieFavorita(serieId, emailUsuario);
        return ResponseEntity.ok(Map.of("isFavorito", isFavorito));
    }

    @GetMapping("/count")
    public ResponseEntity<?> contarFavoritos(Authentication authentication) {
        String emailUsuario = authentication.getName();
        long total = favoritoService.contarFavoritosDoUsuario(emailUsuario);
        return ResponseEntity.ok(Map.of("total", total));
    }
}
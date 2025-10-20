package com.topfilmesbrasil.controller;

import com.topfilmesbrasil.dto.FavoritoDTO;
import com.topfilmesbrasil.dto.ReviewDTO;
import com.topfilmesbrasil.dto.UsuarioDTO;
import com.topfilmesbrasil.model.Usuario;
import com.topfilmesbrasil.service.FavoritoService;
import com.topfilmesbrasil.service.ReviewService;
import com.topfilmesbrasil.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UsuarioService usuarioService;
    private final FavoritoService favoritoService;
    private final ReviewService reviewService;

    public ProfileController(UsuarioService usuarioService, FavoritoService favoritoService, ReviewService reviewService) {
        this.usuarioService = usuarioService;
        this.favoritoService = favoritoService;
        this.reviewService = reviewService;
    }

    @ModelAttribute("usuarioLogado")
    public UsuarioDTO getUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Usuario) {
                Usuario usuarioAutenticado = (Usuario) principal;
                return new UsuarioDTO(
                        usuarioAutenticado.getId(),
                        usuarioAutenticado.getNomeCompleto(),
                        usuarioAutenticado.getEmail(),
                        usuarioAutenticado.getRole()
                );
            }
        }
        return null;
    }

    @GetMapping
    public String profile(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String emailUsuario = authentication.getName();

        try {
            // Buscar dados do usuário
            Usuario usuario = usuarioService.findByEmail(emailUsuario)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            // Buscar estatísticas do usuário
            List<FavoritoDTO> favoritos = favoritoService.listarFavoritosDoUsuario(emailUsuario);
            List<ReviewDTO> reviews = reviewService.getReviewsByUsuarioEmail(emailUsuario);
            long totalFavoritos = favoritoService.contarFavoritosDoUsuario(emailUsuario);
            long totalReviews = reviews.size();

            // Calcular média de avaliações do usuário
            double mediaAvaliacoes = reviews.stream()
                    .mapToDouble(ReviewDTO::getRating)
                    .average()
                    .orElse(0.0);

            model.addAttribute("usuario", usuario);
            model.addAttribute("favoritos", favoritos);
            model.addAttribute("reviews", reviews);
            model.addAttribute("totalFavoritos", totalFavoritos);
            model.addAttribute("totalReviews", totalReviews);
            model.addAttribute("mediaAvaliacoes", mediaAvaliacoes);
            model.addAttribute("temFavoritos", totalFavoritos > 0);
            model.addAttribute("temReviews", totalReviews > 0);

        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Erro ao carregar perfil: " + e.getMessage());
            model.addAttribute("favoritos", List.of());
            model.addAttribute("reviews", List.of());
            model.addAttribute("temFavoritos", false);
            model.addAttribute("temReviews", false);
        }

        return "profile";
    }
}

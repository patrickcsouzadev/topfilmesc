package com.topfilmesbrasil.controller;

import com.topfilmesbrasil.dto.FavoritoDTO;
import com.topfilmesbrasil.dto.UsuarioDTO;
import com.topfilmesbrasil.model.Usuario;
import com.topfilmesbrasil.service.FavoritoService;
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
@RequestMapping("/favoritos")
public class FavoritosPageController {

    private final FavoritoService favoritoService;

    @Autowired
    public FavoritosPageController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
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
    public String listarFavoritos(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/";
        }

        String emailUsuario = authentication.getName();

        try {
            List<FavoritoDTO> todosFavoritos = favoritoService.listarFavoritosDoUsuario(emailUsuario);
            List<FavoritoDTO> filmesFavoritos = favoritoService.listarFilmesFavoritosDoUsuario(emailUsuario);
            List<FavoritoDTO> seriesFavoritas = favoritoService.listarSeriesFavoritasDoUsuario(emailUsuario);
            long totalFavoritos = favoritoService.contarFavoritosDoUsuario(emailUsuario);

            model.addAttribute("todosFavoritos", todosFavoritos);
            model.addAttribute("filmesFavoritos", filmesFavoritos);
            model.addAttribute("seriesFavoritas", seriesFavoritas);
            model.addAttribute("totalFavoritos", totalFavoritos);
            model.addAttribute("temFavoritos", totalFavoritos > 0);

        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Erro ao carregar favoritos: " + e.getMessage());
            model.addAttribute("todosFavoritos", List.of());
            model.addAttribute("temFavoritos", false);
        }

        return "favoritos";
    }
}
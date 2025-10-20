package com.topfilmesbrasil.controller;

import com.topfilmesbrasil.dto.FilmeDTO;
import com.topfilmesbrasil.dto.SerieDTO;
import com.topfilmesbrasil.dto.UsuarioDTO;
import com.topfilmesbrasil.model.Usuario;
import com.topfilmesbrasil.service.FilmeService;
import com.topfilmesbrasil.service.SerieService;
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
@RequestMapping("/releases")
public class ReleasesController {

    private final FilmeService filmeService;
    private final SerieService serieService;

    public ReleasesController(FilmeService filmeService, SerieService serieService) {
        this.filmeService = filmeService;
        this.serieService = serieService;
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
    public String releases(Model model) {
        // Buscar filmes e séries recentes (últimos lançamentos)
        List<FilmeDTO> filmesRecentes = filmeService.buscarNovosLancamentos(20);
        List<SerieDTO> seriesRecentes = serieService.buscarNovosLancamentos(20);
        
        model.addAttribute("filmesRecentes", filmesRecentes);
        model.addAttribute("seriesRecentes", seriesRecentes);
        model.addAttribute("tituloPagina", "Novos Lançamentos");
        
        return "releases";
    }
}

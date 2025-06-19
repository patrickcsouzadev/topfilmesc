package com.topfilmesbrasil.controller;

import com.topfilmesbrasil.dto.*;
import com.topfilmesbrasil.model.Usuario;
import com.topfilmesbrasil.service.FilmeService;
import com.topfilmesbrasil.service.SerieService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    private final FilmeService filmeService;
    private final SerieService serieService;

    @Autowired
    public HomeController(FilmeService filmeService, SerieService serieService) {
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

    @GetMapping("/")
    public String redirectToMovies() {
        return "redirect:/movies";
    }

    @GetMapping("/movies")
    public String home(Model model, HttpSession session) {
        UsuarioDTO usuarioLogado = getUsuarioLogado();
        if (usuarioLogado != null) {
            session.setAttribute("usuario", usuarioLogado);
        } else {
            session.removeAttribute("usuario");
        }

        List<ConteudoDTO> destaquesCarousel = new ArrayList<>();
        destaquesCarousel.addAll(filmeService.buscarDestaques());
        destaquesCarousel.addAll(serieService.buscarDestaques());
        model.addAttribute("destaquesCarousel", destaquesCarousel);

        List<FilmeDTO> topAvaliadosFilmes = filmeService.buscarTopAvaliados(5);
        List<FilmeDTO> filmesRecentes = filmeService.buscarNovosLancamentos(10);
        List<SerieDTO> seriesDestaque = serieService.buscarTopAvaliadas(10);

        model.addAttribute("topRated", topAvaliadosFilmes);
        model.addAttribute("newReleases", filmesRecentes);
        model.addAttribute("tvShows", seriesDestaque);

        return "index";
    }

    @GetMapping("/filmes")
    public String listarTodosFilmes(Model model) {
        List<FilmeDTO> todosFilmes = filmeService.findAllDTO();
        model.addAttribute("conteudos", todosFilmes);
        model.addAttribute("tituloPagina", "Todos os Filmes");
        model.addAttribute("tipoConteudo", "filme");
        return "listagem-conteudo";
    }

    @GetMapping("/series")
    public String listarTodasSeries(Model model) {
        List<SerieDTO> todasSeries = serieService.findAllDTO();
        model.addAttribute("conteudos", todasSeries);
        model.addAttribute("tituloPagina", "Todas as Séries");
        model.addAttribute("tipoConteudo", "serie");
        return "listagem-conteudo";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("registroUsuarioDTO", new RegistroUsuarioDTO());
        return "signup";
    }
}
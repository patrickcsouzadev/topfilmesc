package com.topfilmesbrasil.controller;

import com.topfilmesbrasil.dto.FilmeDTO;
import com.topfilmesbrasil.dto.ReviewDTO;
import com.topfilmesbrasil.dto.SerieDTO;
import com.topfilmesbrasil.dto.UsuarioDTO;
import com.topfilmesbrasil.model.Usuario;
import com.topfilmesbrasil.service.FilmeService;
import com.topfilmesbrasil.service.ReviewService;
import com.topfilmesbrasil.service.SerieService;
// Não precisamos mais do RatingService aqui, pois o FilmeDTO/SerieDTO já trarão essa info
// import com.topfilmesbrasil.service.RatingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/movies")
public class ContentViewController {

    private final FilmeService filmeService;
    private final SerieService serieService;
    private final ReviewService reviewService;

    @Autowired
    public ContentViewController(FilmeService filmeService, SerieService serieService, ReviewService reviewService) {
        this.filmeService = filmeService;
        this.serieService = serieService;
        this.reviewService = reviewService;
        // this.ratingService = ratingService;
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

    private void popularSessaoUsuario(HttpSession session, UsuarioDTO usuarioLogado) {
        if (usuarioLogado != null) {
            session.setAttribute("usuario", usuarioLogado);
        } else {
            session.removeAttribute("usuario");
        }
    }

    @GetMapping("/filmes")
    public String listarTodosOsFilmes(Model model) {
        List<FilmeDTO> todosOsFilmes = filmeService.findAllDTO();
        model.addAttribute("conteudos", todosOsFilmes);
        model.addAttribute("tituloPagina", "Todos os Filmes");
        model.addAttribute("tipoConteudo", "filme"); // Para o template saber o que está listando
        return "list-content"; // Renderiza uma nova página genérica para listagem
    }

    @GetMapping("/series")
    public String listarTodasAsSeries(Model model) {
        List<SerieDTO> todasAsSeries = serieService.findAllDTO();
        model.addAttribute("conteudos", todasAsSeries);
        model.addAttribute("tituloPagina", "Todas as Séries");
        model.addAttribute("tipoConteudo", "serie");
        return "list-content";
    }

    @GetMapping("/filmes/{id}")
    public String filmeDetail(@PathVariable Long id, Model model, HttpSession session) {
        UsuarioDTO usuarioLogado = getUsuarioLogado();
        popularSessaoUsuario(session, usuarioLogado);

        FilmeDTO filmeDTO = filmeService.findByIdDTO(id)
                .orElseThrow(() -> new RuntimeException("Filme não encontrado: " + id)); // Tratar melhor na vida real

        List<ReviewDTO> reviews = reviewService.getReviewsByFilmeId(id);

        Map<String, Object> movieViewModel = new HashMap<>();
        movieViewModel.put("id", filmeDTO.getId());
        movieViewModel.put("title", filmeDTO.getTitulo());

        if (filmeDTO.getRatingInfo() != null) {
            movieViewModel.put("rating", filmeDTO.getRatingInfo().getMedia() / 2.0); // Para as estrelas (0-5)
            movieViewModel.put("score", String.format("%.1f", filmeDTO.getRatingInfo().getMedia() / 2.0)); // Score numérico (ex: "4.7")
            movieViewModel.put("totalRatings", filmeDTO.getRatingInfo().getTotal()); // Total de avaliações
        } else {
            movieViewModel.put("rating", 0.0);
            movieViewModel.put("score", "0.0");
            movieViewModel.put("totalRatings", 0);
        }

        movieViewModel.put("year", filmeDTO.getAnoLancamento());
        movieViewModel.put("duration", filmeDTO.getDuracao()); // "136 min"
        movieViewModel.put("genres", filmeDTO.getListaGeneros());
        movieViewModel.put("overview", filmeDTO.getDescricao());
        movieViewModel.put("fullDescription", filmeDTO.getDescricao());
        movieViewModel.put("backgroundImage", filmeDTO.getUrlBackgroundImage() != null ? filmeDTO.getUrlBackgroundImage() : "/images/default-bg.jpg");
        movieViewModel.put("posterImage", filmeDTO.getUrlPoster() != null ? filmeDTO.getUrlPoster() : "/images/no-poster.jpg");
        movieViewModel.put("cast", parseElenco(filmeDTO.getElenco()));
        movieViewModel.put("reviews", reviews);

        model.addAttribute("movie", movieViewModel);
        model.addAttribute("user", usuarioLogado);

        return "movie-detail";
    }

    private List<Map<String, String>> parseElenco(String elencoStr) {
        if (elencoStr == null || elencoStr.trim().isEmpty()) {
            return Arrays.asList(createCastMember("Elenco não informado", "", "/images/default-actor.png"));
        }
        return Arrays.stream(elencoStr.split(","))
                .map(s -> {
                    String nome = s.trim();
                    String personagem = "Ator/Atriz"; // Default
                    if (s.contains("(") && s.contains(")")) {
                        nome = s.substring(0, s.indexOf("(")).trim();
                        personagem = s.substring(s.indexOf("(") + 1, s.indexOf(")")).trim();
                    }
                    return createCastMember(nome, personagem, "/images/default-actor.png");
                })
                .collect(Collectors.toList());
    }

    private Map<String, String> createCastMember(String name, String character, String photo) {
        Map<String, String> member = new HashMap<>();
        member.put("name", name);
        member.put("character", character);
        member.put("photo", photo); // O HTML espera 'photo'
        return member;
    }

    @GetMapping("/series/{id}")
    public String serieDetail(@PathVariable Long id, Model model, HttpSession session) {
        UsuarioDTO usuarioLogado = getUsuarioLogado();
        popularSessaoUsuario(session, usuarioLogado);

        SerieDTO serieDTO = serieService.findByIdDTO(id)
                .orElseThrow(() -> new RuntimeException("Série não encontrada: " + id));
        List<ReviewDTO> reviews = reviewService.getReviewsBySerieId(id);

        Map<String, Object> serieViewModel = new HashMap<>();
        serieViewModel.put("id", serieDTO.getId());
        serieViewModel.put("title", serieDTO.getTitulo());

        if (serieDTO.getRatingInfo() != null) {
            serieViewModel.put("rating", serieDTO.getRatingInfo().getMedia() / 2.0); // Para estrelas (0-5)
            serieViewModel.put("score", String.format("%.1f", serieDTO.getRatingInfo().getMedia() / 2.0)); // Score (0-5)
            serieViewModel.put("totalRatings", serieDTO.getRatingInfo().getTotal());
        } else {
            serieViewModel.put("rating", 0.0);
            serieViewModel.put("score", "0.0");
            serieViewModel.put("totalRatings", 0);
        }

        serieViewModel.put("year", serieDTO.getAnoLancamento());
        serieViewModel.put("duration", serieDTO.getNumeroTemporadas() + (serieDTO.getNumeroTemporadas() != null && serieDTO.getNumeroTemporadas() == 1 ? " temporada" : " temporadas"));
        serieViewModel.put("genres", serieDTO.getListaGeneros());
        serieViewModel.put("overview", serieDTO.getDescricao());
        serieViewModel.put("fullDescription", serieDTO.getDescricao());
        serieViewModel.put("backgroundImage", serieDTO.getUrlBackgroundImage() != null ? serieDTO.getUrlBackgroundImage() : "/images/default-bg.jpg");
        serieViewModel.put("posterImage", serieDTO.getUrlPoster() != null ? serieDTO.getUrlPoster() : "/images/no-poster.jpg");
        serieViewModel.put("cast", parseElenco(serieDTO.getElenco()));
        serieViewModel.put("reviews", reviews);
        serieViewModel.put("numeroTemporadas", serieDTO.getNumeroTemporadas());
        serieViewModel.put("statusSerie", serieDTO.getStatus());
        model.addAttribute("movie", serieViewModel); // O template usa a variável "movie"
        model.addAttribute("user", usuarioLogado);

        return "movie-detail";
    }
}
// Este controlador tem a responsabilidade exclusiva de gerenciar as ações de administração relacionadas à criação de filmes e séries.
// Ele é o ponto de entrada seguro para que apenas usuários autorizados possam adicionar novos itens ao catálogo da aplicação.

package com.topfilmesbrasil.controller;

import com.topfilmesbrasil.model.Filme;
import com.topfilmesbrasil.model.Serie;
import com.topfilmesbrasil.service.FilmeService;
import com.topfilmesbrasil.service.SerieService;
import com.topfilmesbrasil.service.StorageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Controller // Esta anotação fundamental do Spring MVC marca a classe como um controlador web. Isso significa que ela é responsável por receber requisições HTTP e direcioná-las para os métodos apropriados.
// Diferente de um @RestController, os métodos aqui retornam o nome de um template (view) para ser renderizado.
@RequestMapping("/admin/content") // Define um prefixo de URL base para todos os métodos dentro desta classe.
// Qualquer mapeamento de méto.do (como /add ou /filmes) será relativo a este caminho.
@PreAuthorize("hasRole('ADMIN')") // garante que nenhum méto.do neste controlador possa ser executado a menos que o usuário atualmente logado tenha a permissão (role) ROLE_ADMIN.
public class AdminContentController {

    private final FilmeService filmeService;  // usado para salvar os novos objetos de filme
    private final SerieService serieService; // usado para salvar os novos objetos de serie
    private final StorageService storageService; //  Um serviço especializado para lidar com o upload de arquivos (pôsteres e banners). Isso mantém a lógica de manipulação de arquivos separada do controlador.

    @Autowired
    public AdminContentController(FilmeService filmeService, SerieService serieService, StorageService storageService) {
        this.filmeService = filmeService;
        this.serieService = serieService;
        this.storageService = storageService;
    }

    @GetMapping("/add")
    public String showAddContentForm(Model model) {
        if (!model.containsAttribute("filme")) {
            model.addAttribute("filme", new Filme());
        }
        if (!model.containsAttribute("serie")) {
            model.addAttribute("serie", new Serie());
        }
        return "admin/add-content";
    }

    @PostMapping("/filmes")
    public String addFilme(@Valid @ModelAttribute("filme") Filme filme,
                           BindingResult result,
                           @RequestParam("posterFile") MultipartFile posterFile,
                           @RequestParam("bannerFile") MultipartFile bannerFile,
                           @RequestParam(name = "generosCsv", required = false) String generosCsv,
                           RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.filme", result);
            redirectAttributes.addFlashAttribute("filme", filme);
            return "redirect:/admin/content/add";
        }

        if (posterFile != null && !posterFile.isEmpty()) {
            String posterPath = storageService.store(posterFile);
            filme.setUrlPoster(posterPath);
        }

        if (bannerFile != null && !bannerFile.isEmpty()) {
            String bannerPath = storageService.store(bannerFile);
            filme.setUrlBackgroundImage(bannerPath);
        }

        if (generosCsv != null && !generosCsv.trim().isEmpty()) {
            Set<String> generosSet = new HashSet<>(Arrays.asList(generosCsv.split("\\s*,\\s*")));
            filme.setListaGeneros(generosSet);
        }

        filmeService.salvar(filme);
        redirectAttributes.addFlashAttribute("successMessage", "Filme adicionado com sucesso!");
        return "redirect:/admin/content/add";
    }

    @PostMapping("/series")
    public String addSerie(@Valid @ModelAttribute("serie") Serie serie,
                           BindingResult result,
                           @RequestParam("posterFileSerie") MultipartFile posterFile, // Nome único para o parâmetro
                           @RequestParam("bannerFileSerie") MultipartFile bannerFile, // Nome único para o parâmetro
                           @RequestParam(name = "generosCsvSerie", required = false) String generosCsv,
                           RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.serie", result);
            redirectAttributes.addFlashAttribute("serie", serie);
            return "redirect:/admin/content/add";
        }

        if (posterFile != null && !posterFile.isEmpty()) {
            String posterPath = storageService.store(posterFile);
            serie.setUrlPoster(posterPath);
        }

        if (bannerFile != null && !bannerFile.isEmpty()) {
            String bannerPath = storageService.store(bannerFile);
            serie.setUrlBackgroundImage(bannerPath);
        }

        if (generosCsv != null && !generosCsv.trim().isEmpty()) {
            Set<String> generosSet = new HashSet<>(Arrays.asList(generosCsv.split("\\s*,\\s*")));
            serie.setListaGeneros(generosSet);
        }

        serieService.salvar(serie);
        redirectAttributes.addFlashAttribute("successMessage", "Série adicionada com sucesso!");
        return "redirect:/admin/content/add";
    }
}
package com.topfilmesbrasil.controller;

import com.topfilmesbrasil.dto.FilmeDTO;
import com.topfilmesbrasil.dto.SerieDTO;
import com.topfilmesbrasil.service.FilmeService;
import com.topfilmesbrasil.service.SerieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/movies/api")
public class ContentApiController {

    private final FilmeService filmeService;
    private final SerieService serieService;

    @Autowired
    public ContentApiController(FilmeService filmeService, SerieService serieService) {
        this.filmeService = filmeService;
        this.serieService = serieService;
    }

    @GetMapping("/filmes/{id}")
    public ResponseEntity<FilmeDTO> getFilmeDetails(@PathVariable Long id) {
        return filmeService.findByIdDTO(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/series/{id}")
    public ResponseEntity<SerieDTO> getSerieDetails(@PathVariable Long id) {
        return serieService.findByIdDTO(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
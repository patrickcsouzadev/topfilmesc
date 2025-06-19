package com.topfilmesbrasil.controller;

import com.topfilmesbrasil.dto.RatingInfoDTO;
import com.topfilmesbrasil.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ratings/api")
public class RatingApiController {

    private final RatingService ratingService;

    @Autowired
    public RatingApiController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @GetMapping("/filme/{filmeId}/media")
    public ResponseEntity<RatingInfoDTO> getRatingInfoFilme(@PathVariable Long filmeId) {
        RatingInfoDTO ratingInfo = ratingService.getRatingInfoParaFilme(filmeId);
        return ResponseEntity.ok(ratingInfo);
    }

    @GetMapping("/serie/{serieId}/media")
    public ResponseEntity<RatingInfoDTO> getRatingInfoSerie(@PathVariable Long serieId) {
        RatingInfoDTO ratingInfo = ratingService.getRatingInfoParaSerie(serieId);
        return ResponseEntity.ok(ratingInfo);
    }
}
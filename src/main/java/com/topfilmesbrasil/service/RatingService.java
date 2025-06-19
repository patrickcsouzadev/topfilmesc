package com.topfilmesbrasil.service;

import com.topfilmesbrasil.dto.RatingInfoDTO;

public interface RatingService {
    RatingInfoDTO getRatingInfoParaFilme(Long filmeId);
    RatingInfoDTO getRatingInfoParaSerie(Long serieId);
}
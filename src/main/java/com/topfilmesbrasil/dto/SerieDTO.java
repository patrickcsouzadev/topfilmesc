package com.topfilmesbrasil.dto;

public class SerieDTO extends ConteudoDTO {
    private Integer numeroTemporadas;
    private Integer numeroEpisodios;
    private String status;
    private String criador;
    private RatingInfoDTO ratingInfo;

    public Integer getNumeroTemporadas() {
        return numeroTemporadas;
    }

    public void setNumeroTemporadas(Integer numeroTemporadas) {
        this.numeroTemporadas = numeroTemporadas;
    }

    public Integer getNumeroEpisodios() {
        return numeroEpisodios;
    }

    public void setNumeroEpisodios(Integer numeroEpisodios) {
        this.numeroEpisodios = numeroEpisodios;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCriador() {
        return criador;
    }

    public void setCriador(String criador) {
        this.criador = criador;
    }

    public RatingInfoDTO getRatingInfo() {
        return ratingInfo;
    }

    public void setRatingInfo(RatingInfoDTO ratingInfo) {
        this.ratingInfo = ratingInfo;
    }
}
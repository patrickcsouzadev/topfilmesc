package com.topfilmesbrasil.dto;

public class FilmeDTO extends ConteudoDTO {
    private Integer duracaoMinutos;
    private String diretor;
    private RatingInfoDTO ratingInfo;

    public Integer getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(Integer duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos;
    }

    public String getDuracao() {
        return duracaoMinutos != null ? duracaoMinutos + " min" : null;
    }

    public String getDiretor() {
        return diretor;
    }

    public void setDiretor(String diretor) {
        this.diretor = diretor;
    }

    public RatingInfoDTO getRatingInfo() {
        return ratingInfo;
    }

    public void setRatingInfo(RatingInfoDTO ratingInfo) {
        this.ratingInfo = ratingInfo;
    }
}
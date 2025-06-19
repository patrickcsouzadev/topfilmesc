package com.topfilmesbrasil.dto;

import java.time.LocalDateTime;
import java.util.Set;

public abstract class ConteudoDTO {
    private Long id;
    private String titulo;
    private String descricao;
    private Integer anoLancamento;
    private String genero;
    private Set<String> listaGeneros;
    private String urlPoster;
    private String urlBackgroundImage;
    private String urlTrailer;
    private String classificacaoIndicativa;
    private String elenco;
    private LocalDateTime dataCriacao;
    private Double mediaRating;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Integer getAnoLancamento() { return anoLancamento; }
    public void setAnoLancamento(Integer anoLancamento) { this.anoLancamento = anoLancamento; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public Set<String> getListaGeneros() { return listaGeneros; }
    public void setListaGeneros(Set<String> listaGeneros) { this.listaGeneros = listaGeneros; }

    public String getUrlPoster() { return urlPoster; }
    public void setUrlPoster(String urlPoster) { this.urlPoster = urlPoster; }

    public String getUrlBackgroundImage() { return urlBackgroundImage; }
    public void setUrlBackgroundImage(String urlBackgroundImage) { this.urlBackgroundImage = urlBackgroundImage; }

    public String getUrlTrailer() { return urlTrailer; }
    public void setUrlTrailer(String urlTrailer) { this.urlTrailer = urlTrailer; }

    public String getClassificacaoIndicativa() { return classificacaoIndicativa; }
    public void setClassificacaoIndicativa(String classificacaoIndicativa) { this.classificacaoIndicativa = classificacaoIndicativa; }

    public String getElenco() { return elenco; }
    public void setElenco(String elenco) { this.elenco = elenco; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public Double getMediaRating() { return mediaRating; }
    public void setMediaRating(Double mediaRating) { this.mediaRating = mediaRating; }
}
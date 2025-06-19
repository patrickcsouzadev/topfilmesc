package com.topfilmesbrasil.dto;

import java.time.LocalDateTime;
import java.util.Set;

public class FavoritoDTO {
    private Long id;
    private Long usuarioId;
    private Long conteudoId;
    private String tipoConteudo;
    private String titulo;
    private String urlPoster;
    private Integer anoLancamento;
    private Double mediaRating;
    private LocalDateTime dataAdicao;
    private Set<String> listaGeneros;

    public FavoritoDTO() {}

    public FavoritoDTO(Long id, Long usuarioId, Long conteudoId, String tipoConteudo,
                       String titulo, String urlPoster, Integer anoLancamento,
                       Double mediaRating, LocalDateTime dataAdicao) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.conteudoId = conteudoId;
        this.tipoConteudo = tipoConteudo;
        this.titulo = titulo;
        this.urlPoster = urlPoster;
        this.anoLancamento = anoLancamento;
        this.mediaRating = mediaRating;
        this.dataAdicao = dataAdicao;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public Long getConteudoId() { return conteudoId; }
    public void setConteudoId(Long conteudoId) { this.conteudoId = conteudoId; }

    public String getTipoConteudo() { return tipoConteudo; }
    public void setTipoConteudo(String tipoConteudo) { this.tipoConteudo = tipoConteudo; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getUrlPoster() { return urlPoster; }
    public void setUrlPoster(String urlPoster) { this.urlPoster = urlPoster; }

    public Integer getAnoLancamento() { return anoLancamento; }
    public void setAnoLancamento(Integer anoLancamento) { this.anoLancamento = anoLancamento; }

    public Double getMediaRating() { return mediaRating; }
    public void setMediaRating(Double mediaRating) { this.mediaRating = mediaRating; }

    public LocalDateTime getDataAdicao() { return dataAdicao; }
    public void setDataAdicao(LocalDateTime dataAdicao) { this.dataAdicao = dataAdicao; }

    public Set<String> getListaGeneros() { return listaGeneros; }
    public void setListaGeneros(Set<String> listaGeneros) { this.listaGeneros = listaGeneros; }
}
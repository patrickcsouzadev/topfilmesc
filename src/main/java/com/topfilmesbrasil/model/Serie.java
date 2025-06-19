package com.topfilmesbrasil.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "series")
public class Serie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O título é obrigatório")
    @Size(max = 255)
    @Column(nullable = false)
    private String titulo;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "ano_lancamento")
    private Integer anoLancamento;

    @Column(length = 100)
    private String genero;

    @Column(name = "em_destaque", nullable = false)
    private boolean emDestaque = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "serie_generos", joinColumns = @JoinColumn(name = "serie_id"))
    @Column(name = "genero_tag")
    private Set<String> listaGeneros = new HashSet<>();

    @Column(name = "url_poster")
    private String urlPoster;

    @Column(name = "url_background_image")
    private String urlBackgroundImage;

    @Column(name = "url_trailer")
    private String urlTrailer;

    @Column(name = "numero_temporadas")
    private Integer numeroTemporadas;

    @Column(name = "numero_episodios")
    private Integer numeroEpisodios;

    @Column(name = "status_serie", length = 50)
    private String status;

    @Column(name = "classificacao_indicativa", length=50)
    private String classificacaoIndicativa;

    @Column(name = "criador", length=200)
    private String criador;

    @Column(name = "elenco", columnDefinition = "TEXT")
    private String elenco;

    @Column(name = "data_criacao", updatable = false)
    private LocalDateTime dataCriacao;

    @Transient
    private Double mediaRating;

    @OneToMany(mappedBy = "serie", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Review> reviews = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
    }

    public Serie() {}

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
    public Integer getNumeroTemporadas() { return numeroTemporadas; }
    public void setNumeroTemporadas(Integer numeroTemporadas) { this.numeroTemporadas = numeroTemporadas; }
    public Integer getNumeroEpisodios() { return numeroEpisodios; }
    public void setNumeroEpisodios(Integer numeroEpisodios) { this.numeroEpisodios = numeroEpisodios; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getClassificacaoIndicativa() { return classificacaoIndicativa; }
    public void setClassificacaoIndicativa(String classificacaoIndicativa) { this.classificacaoIndicativa = classificacaoIndicativa; }
    public String getCriador() { return criador; }
    public void setCriador(String criador) { this.criador = criador; }
    public String getElenco() { return elenco; }
    public void setElenco(String elenco) { this.elenco = elenco; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public Double getMediaRating() { return mediaRating; }
    public void setMediaRating(Double mediaRating) { this.mediaRating = mediaRating; }
    public Set<Review> getReviews() { return reviews; }
    public void setReviews(Set<Review> reviews) { this.reviews = reviews; }

    public boolean isEmDestaque() {
        return emDestaque;
    }

    public void setEmDestaque(boolean emDestaque) {
        this.emDestaque = emDestaque;
    }

    public void setAtivo(boolean b) {

    }
}
package com.topfilmesbrasil.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "favoritos")
public class Favorito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filme_id", nullable = true)
    private Filme filme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serie_id", nullable = true)
    private Serie serie;

    @Column(name = "data_adicao", nullable = false, updatable = false)
    private LocalDateTime dataAdicao;

    @PrePersist
    protected void onCreate() {
        dataAdicao = LocalDateTime.now();
    }

    public Favorito() {}

    public Favorito(Usuario usuario, Filme filme) {
        this.usuario = usuario;
        this.filme = filme;
    }

    public Favorito(Usuario usuario, Serie serie) {
        this.usuario = usuario;
        this.serie = serie;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Filme getFilme() { return filme; }
    public void setFilme(Filme filme) { this.filme = filme; }

    public Serie getSerie() { return serie; }
    public void setSerie(Serie serie) { this.serie = serie; }

    public LocalDateTime getDataAdicao() { return dataAdicao; }
    public void setDataAdicao(LocalDateTime dataAdicao) { this.dataAdicao = dataAdicao; }

    public String getTipoConteudo() {
        return filme != null ? "filme" : "serie";
    }

    public Long getConteudoId() {
        return filme != null ? filme.getId() : serie.getId();
    }

    public String getTituloConteudo() {
        return filme != null ? filme.getTitulo() : serie.getTitulo();
    }
}
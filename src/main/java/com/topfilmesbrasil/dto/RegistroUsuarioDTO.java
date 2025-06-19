package com.topfilmesbrasil.dto;

import com.fasterxml.jackson.annotation.JsonProperty; // <-- Nova importação
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistroUsuarioDTO {

    @NotBlank(message = "Nome completo é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String nomeCompleto;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres")
    private String senha;

    public String getNomeCompleto() { return nomeCompleto; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }

    @JsonProperty("name")
    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @JsonProperty("password")
    public void setSenha(String senha) {
        this.senha = senha;
    }
}
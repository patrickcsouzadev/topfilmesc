package com.topfilmesbrasil.dto;

import com.fasterxml.jackson.annotation.JsonProperty; // <-- IMPORTANTE: Nova importação
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequestDTO {

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    private String senha;

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @JsonProperty("password")
    public void setSenha(String password) {
        this.senha = password;
    }
}
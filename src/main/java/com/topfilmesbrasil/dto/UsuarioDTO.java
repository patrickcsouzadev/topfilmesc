package com.topfilmesbrasil.dto;

import com.topfilmesbrasil.model.Role;

public class UsuarioDTO {
    private Long id;
    private String nomeCompleto;
    private String email;
    private Role role;
    private boolean admin;

    public UsuarioDTO(Long id, String nomeCompleto, String email, Role role) {
        this.id = id;
        this.nomeCompleto = nomeCompleto;
        this.email = email;
        this.role = role;
        this.admin = (role == Role.ROLE_ADMIN);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
        this.admin = (role == Role.ROLE_ADMIN);
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean getAdmin() {  // ← ESTE É NECESSÁRIO PARA O THYMELEAF
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
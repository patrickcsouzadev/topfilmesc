package com.topfilmesbrasil.service;

import com.topfilmesbrasil.model.Usuario;
import com.topfilmesbrasil.model.VerificationToken;

public interface VerificationService {
    VerificationToken gerarTokenVerificacao(Usuario usuario);
    boolean verificarToken(String token);
    void reenviarEmailVerificacao(String email);
}


package com.topfilmesbrasil.service;

import com.topfilmesbrasil.model.Usuario;
import com.topfilmesbrasil.model.PasswordResetToken;

public interface PasswordResetService {
    PasswordResetToken gerarTokenRedefinicaoSenha(Usuario usuario);
    boolean validarTokenRedefinicao(String token);
    void redefinirSenha(String token, String novaSenha);
    void solicitarRedefinicaoSenha(String email);
}

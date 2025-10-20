package com.topfilmesbrasil.service;

public interface EmailService {
    void enviarEmailVerificacao(String emailDestinatario, String nomeUsuario, String tokenVerificacao);
    void enviarEmailRedefinicaoSenha(String emailDestinatario, String nomeUsuario, String tokenRedefinicao);
}


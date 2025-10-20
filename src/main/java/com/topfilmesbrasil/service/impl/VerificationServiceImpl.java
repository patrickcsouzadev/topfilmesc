package com.topfilmesbrasil.service.impl;

import com.topfilmesbrasil.model.Usuario;
import com.topfilmesbrasil.model.VerificationToken;
import com.topfilmesbrasil.repository.UsuarioRepository;
import com.topfilmesbrasil.repository.VerificationTokenRepository;
import com.topfilmesbrasil.service.EmailService;
import com.topfilmesbrasil.service.VerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class VerificationServiceImpl implements VerificationService {

    private final VerificationTokenRepository verificationTokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;

    @Autowired
    public VerificationServiceImpl(VerificationTokenRepository verificationTokenRepository,
                                  UsuarioRepository usuarioRepository,
                                  EmailService emailService) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
    }

    @Override
    public VerificationToken gerarTokenVerificacao(Usuario usuario) {
        // Remove token anterior se existir
        Optional<VerificationToken> tokenExistente = verificationTokenRepository.findByUsuario(usuario);
        if (tokenExistente.isPresent()) {
            verificationTokenRepository.delete(tokenExistente.get());
        }

        // Cria novo token
        VerificationToken token = new VerificationToken(usuario);
        token = verificationTokenRepository.save(token);

        // Envia email de verificação
        emailService.enviarEmailVerificacao(usuario.getEmail(), usuario.getNomeCompleto(), token.getToken());

        return token;
    }

    @Override
    public boolean verificarToken(String token) {
        Optional<VerificationToken> verificationToken = verificationTokenRepository.findByToken(token);
        
        if (verificationToken.isEmpty()) {
            return false;
        }

        VerificationToken tokenObj = verificationToken.get();
        
        // Verifica se o token não expirou
        if (tokenObj.isExpirado()) {
            verificationTokenRepository.delete(tokenObj);
            return false;
        }

        // Ativa a conta do usuário
        Usuario usuario = tokenObj.getUsuario();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        // Remove o token após uso
        verificationTokenRepository.delete(tokenObj);

        return true;
    }

    @Override
    public void reenviarEmailVerificacao(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuário não encontrado com o email: " + email);
        }

        Usuario usuario = usuarioOpt.get();
        
        if (usuario.isEmailVerificado()) {
            throw new RuntimeException("Email já foi verificado");
        }

        gerarTokenVerificacao(usuario);
    }
}


package com.topfilmesbrasil.service.impl;

import com.topfilmesbrasil.model.Usuario;
import com.topfilmesbrasil.model.PasswordResetToken;
import com.topfilmesbrasil.repository.UsuarioRepository;
import com.topfilmesbrasil.repository.PasswordResetTokenRepository;
import com.topfilmesbrasil.service.EmailService;
import com.topfilmesbrasil.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class PasswordResetServiceImpl implements PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordResetServiceImpl(PasswordResetTokenRepository passwordResetTokenRepository,
                                   UsuarioRepository usuarioRepository,
                                   EmailService emailService,
                                   PasswordEncoder passwordEncoder) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PasswordResetToken gerarTokenRedefinicaoSenha(Usuario usuario) {
        // Remove token anterior se existir
        Optional<PasswordResetToken> tokenExistente = passwordResetTokenRepository.findByUsuario(usuario);
        if (tokenExistente.isPresent()) {
            passwordResetTokenRepository.delete(tokenExistente.get());
        }

        // Cria novo token
        PasswordResetToken token = new PasswordResetToken(usuario);
        token = passwordResetTokenRepository.save(token);

        // Envia email de redefinição
        emailService.enviarEmailRedefinicaoSenha(usuario.getEmail(), usuario.getNomeCompleto(), token.getToken());

        return token;
    }

    @Override
    public boolean validarTokenRedefinicao(String token) {
        Optional<PasswordResetToken> passwordResetToken = passwordResetTokenRepository.findByToken(token);
        
        if (passwordResetToken.isEmpty()) {
            return false;
        }

        PasswordResetToken tokenObj = passwordResetToken.get();
        
        // Verifica se o token não expirou
        if (tokenObj.isExpirado()) {
            passwordResetTokenRepository.delete(tokenObj);
            return false;
        }

        return true;
    }

    @Override
    public void redefinirSenha(String token, String novaSenha) {
        Optional<PasswordResetToken> passwordResetToken = passwordResetTokenRepository.findByToken(token);
        
        if (passwordResetToken.isEmpty()) {
            throw new RuntimeException("Token inválido");
        }

        PasswordResetToken tokenObj = passwordResetToken.get();
        
        if (tokenObj.isExpirado()) {
            passwordResetTokenRepository.delete(tokenObj);
            throw new RuntimeException("Token expirado");
        }

        // Atualiza a senha do usuário
        Usuario usuario = tokenObj.getUsuario();
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        // Remove o token após uso
        passwordResetTokenRepository.delete(tokenObj);
    }

    @Override
    public void solicitarRedefinicaoSenha(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuário não encontrado com o email: " + email);
        }

        Usuario usuario = usuarioOpt.get();
        
        if (!usuario.isEmailVerificado()) {
            throw new RuntimeException("Email não verificado. Verifique seu email antes de solicitar redefinição de senha.");
        }

        gerarTokenRedefinicaoSenha(usuario);
    }
}

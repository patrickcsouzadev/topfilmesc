package com.topfilmesbrasil.service.impl;

import com.topfilmesbrasil.dto.RegistroUsuarioDTO;
import com.topfilmesbrasil.dto.UsuarioDTO;
import com.topfilmesbrasil.model.Role;
import com.topfilmesbrasil.model.Usuario;
import com.topfilmesbrasil.repository.UsuarioRepository;
import com.topfilmesbrasil.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional // Adiciona transacionalidade aos métodos do serviço
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder; // Para hashear senhas

    @Autowired
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Usuario registrarNovoUsuario(RegistroUsuarioDTO registroDTO) {
        if (usuarioRepository.existsByEmail(registroDTO.getEmail())) {
            throw new RuntimeException("Erro: Email já está em uso!"); // Idealmente uma exceção customizada
        }
        Usuario usuario = new Usuario();
        usuario.setNomeCompleto(registroDTO.getNomeCompleto());
        usuario.setEmail(registroDTO.getEmail());
        usuario.setSenha(passwordEncoder.encode(registroDTO.getSenha())); // Hashear a senha
        usuario.setRole(Role.ROLE_USER); // Papel padrão para novos registros
        usuario.setAtivo(true);
        return usuarioRepository.save(usuario);
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Override
    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(usuario -> new UsuarioDTO(usuario.getId(), usuario.getNomeCompleto(), usuario.getEmail(), usuario.getRole()))
                .collect(Collectors.toList());
    }

    @Override
    public void deletarUsuario(Long id) {
        // Adicionar lógica de permissão aqui (só admin pode deletar)
        // Poderia ser uma deleção lógica (setar ativo = false) ou física
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com id: " + id));
        // Se for deleção lógica:
        // usuario.setAtivo(false);
        // usuarioRepository.save(usuario);
        // Se for deleção física:
        usuarioRepository.deleteById(id);
    }

    @Override
    public UsuarioDTO getUsuarioDTOById(Long id) {
        return usuarioRepository.findById(id)
                .map(usuario -> new UsuarioDTO(usuario.getId(), usuario.getNomeCompleto(), usuario.getEmail(), usuario.getRole()))
                .orElse(null); // Ou lançar exceção
    }


    // Implementação do método loadUserByUsername do UserDetailsService
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + email));
    }
}
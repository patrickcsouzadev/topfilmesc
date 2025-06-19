package com.topfilmesbrasil.service;

import com.topfilmesbrasil.dto.RegistroUsuarioDTO;
import com.topfilmesbrasil.dto.UsuarioDTO;
import com.topfilmesbrasil.model.Usuario; // Necessário para UserDetailsService
import org.springframework.security.core.userdetails.UserDetailsService; // Para Spring Security

import java.util.List;
import java.util.Optional;


public interface UsuarioService extends UserDetailsService {
    Usuario registrarNovoUsuario(RegistroUsuarioDTO registroDTO);
    Optional<Usuario> findByEmail(String email);
    List<UsuarioDTO> listarTodos();
    void deletarUsuario(Long id);
    UsuarioDTO getUsuarioDTOById(Long id);
}
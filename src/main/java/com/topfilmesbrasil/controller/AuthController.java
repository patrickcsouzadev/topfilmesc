package com.topfilmesbrasil.controller;

import com.topfilmesbrasil.dto.LoginRequestDTO;
import com.topfilmesbrasil.dto.RegistroUsuarioDTO;
import com.topfilmesbrasil.dto.UsuarioDTO;
import com.topfilmesbrasil.model.Usuario;
import com.topfilmesbrasil.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthController(UsuarioService usuarioService, AuthenticationManager authenticationManager) {
        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody RegistroUsuarioDTO registroDTO) {
        try {
            usuarioService.registrarNovoUsuario(registroDTO);
            return ResponseEntity.ok(Map.of("message", "Usuário registrado com sucesso!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<?> autenticarUsuario(@Valid @RequestBody LoginRequestDTO loginRequestDTO, HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getSenha())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

            Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();
            UsuarioDTO usuarioDTO = new UsuarioDTO(
                    usuarioAutenticado.getId(),
                    usuarioAutenticado.getNomeCompleto(),
                    usuarioAutenticado.getEmail(),
                    usuarioAutenticado.getRole()
            );
            session.setAttribute("usuario", usuarioDTO);

            return ResponseEntity.ok(Map.of(
                    "message", "Login bem-sucedido!",
                    "user", usuarioDTO
            ));
        } catch (BadCredentialsException e) {
            // Captura especificamente a falha de autenticação
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Email ou senha inválidos"));
        } catch (Exception e) {
            // Outras exceções inesperadas
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Ocorreu um erro interno."));
        }
    }

    @GetMapping("/auth/status")
    public ResponseEntity<?> getAuthStatus(HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            Usuario usuarioAutenticado = (Usuario) authentication.getPrincipal();
            UsuarioDTO usuarioDTO = new UsuarioDTO(
                    usuarioAutenticado.getId(),
                    usuarioAutenticado.getNomeCompleto(),
                    usuarioAutenticado.getEmail(),
                    usuarioAutenticado.getRole()
            );
            session.setAttribute("usuario", usuarioDTO);
            return ResponseEntity.ok(Map.of("isAuthenticated", true, "user", usuarioDTO));
        }
        session.removeAttribute("usuario");
        return ResponseEntity.ok(Map.of("isAuthenticated", false));
    }
}
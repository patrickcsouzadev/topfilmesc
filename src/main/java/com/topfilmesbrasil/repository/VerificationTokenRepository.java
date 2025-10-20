package com.topfilmesbrasil.repository;

import com.topfilmesbrasil.model.Usuario;
import com.topfilmesbrasil.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    
    Optional<VerificationToken> findByToken(String token);
    
    Optional<VerificationToken> findByUsuario(Usuario usuario);
    
    void deleteByUsuario(Usuario usuario);
    
    void deleteByToken(String token);
}


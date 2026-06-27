package com.example.demo.repositories;

import com.example.demo.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Busca qualquer usuário (Aluno ou Funcionário) pelo e-mail. */
    Optional<Usuario> findByEmail(String email);

    /** Verifica existência de um usuário com e-mail específico. */
    boolean existsByEmail(String email);
}

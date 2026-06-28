package com.example.demo.repositories;

import com.example.demo.models.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    /** Busca um aluno pelo e-mail (herdado de Usuario). */
    Optional<Aluno> findByEmail(String email);

    /** Busca um aluno pelo número de matrícula. */
    Optional<Aluno> findByMatricula(String matricula);

    /** Verifica existência de um aluno com CPF específico. */
    boolean existsByCpf(String cpf);
}

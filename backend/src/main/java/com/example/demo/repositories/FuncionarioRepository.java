package com.example.demo.repositories;

import com.example.demo.models.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    /** Busca um funcionário pelo e-mail (herdado de Usuario). */
    Optional<Funcionario> findByEmail(String email);

    /** Busca funcionários por cargo (ex: "Administrador"). */
    List<Funcionario> findByCargo(String cargo);

    /** Verifica existência de um funcionário com e-mail específico. */
    boolean existsByEmail(String email);

    /** Verifica existência de um funcionário com CPF específico. */
    boolean existsByCpf(String cpf);
}

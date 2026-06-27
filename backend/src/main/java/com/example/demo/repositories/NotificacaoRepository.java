package com.example.demo.repositories;

import com.example.demo.models.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    /** Lista todas as notificações de uma determinada doação. */
    List<Notificacao> findByDoacaoIdOrderByDataEnvioDesc(Long doacaoId);

    /** Lista todas as notificações vinculadas às doações de um aluno específico. */
    @Query("SELECT n FROM Notificacao n WHERE n.doacao.aluno.id = :alunoId ORDER BY n.dataEnvio DESC")
    List<Notificacao> findByAlunoId(@Param("alunoId") Long alunoId);
}

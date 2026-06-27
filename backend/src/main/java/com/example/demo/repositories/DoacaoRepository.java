package com.example.demo.repositories;

import com.example.demo.models.Doacao;
import com.example.demo.models.Aluno;
import com.example.demo.models.Campanha;
import com.example.demo.models.StatusDoacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoacaoRepository extends JpaRepository<Doacao, Long> {

    /** Busca todas as doações de um aluno pelo ID do aluno. */
    List<Doacao> findByAlunoId(Long alunoId);

    /** Busca doações de um aluno em uma campanha específica com determinado status. */
    List<Doacao> findByAlunoAndCampanhaAndStatus(Aluno aluno, Campanha campanha, StatusDoacao status);

    /** Busca todas as doações de uma campanha específica. */
    List<Doacao> findByCampanhaId(Long campanhaId);

    /** Busca doações pelo status. */
    List<Doacao> findByStatus(StatusDoacao status);

    /** Conta doações por status — usado nos relatórios. */
    long countByStatus(StatusDoacao status);

    /**
     * Relatório agregado por campanha:
     * retorna lista de Object[] com [titulo, totalDoacoes, somaHoras].
     */
    @Query("SELECT d.campanha.titulo, COUNT(d), COALESCE(SUM(d.horasConcedidas), 0) " +
           "FROM Doacao d GROUP BY d.campanha.titulo ORDER BY d.campanha.titulo")
    List<Object[]> findRelatorioPorCampanha();
}

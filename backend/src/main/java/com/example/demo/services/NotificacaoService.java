package com.example.demo.services;

import com.example.demo.models.Doacao;
import com.example.demo.models.Notificacao;
import com.example.demo.repositories.NotificacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço de Notificações — UC06 / RF09
 * Registra avisos no banco sempre que o status de uma doação muda.
 */
@Service
public class NotificacaoService {

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    /**
     * Persiste uma notificação vinculada à doação informada.
     *
     * @param doacao   doação que originou o evento
     * @param mensagem texto descritivo do evento
     * @param canal    canal de entrega (ex: "SISTEMA", "EMAIL")
     * @return notificação salva
     */
    @Transactional
    public Notificacao registrarNotificacao(Doacao doacao, String mensagem, String canal) {
        Notificacao notificacao = new Notificacao(
                mensagem,
                LocalDateTime.now(),
                canal != null ? canal : "SISTEMA",
                doacao
        );
        return notificacaoRepository.save(notificacao);
    }

    /**
     * Conveniência — canal padrão "SISTEMA".
     */
    @Transactional
    public Notificacao registrarNotificacao(Doacao doacao, String mensagem) {
        return registrarNotificacao(doacao, mensagem, "SISTEMA");
    }

    /** Lista notificações de uma doação específica (mais recente primeiro). */
    @Transactional(readOnly = true)
    public List<Notificacao> listarPorDoacao(Long doacaoId) {
        return notificacaoRepository.findByDoacaoIdOrderByDataEnvioDesc(doacaoId);
    }

    /** Lista todas as notificações das doações de um aluno (mais recente primeiro). */
    @Transactional(readOnly = true)
    public List<Notificacao> listarPorAluno(Long alunoId) {
        return notificacaoRepository.findByAlunoId(alunoId);
    }
}

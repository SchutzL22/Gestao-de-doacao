package com.example.demo.controllers;

import com.example.demo.models.Notificacao;
import com.example.demo.services.NotificacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para Notificações — UC06 / RF09
 * Expõe endpoints para consultar notificações por doação e por aluno.
 */
@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    @Autowired
    private NotificacaoService notificacaoService;

    /**
     * Lista todas as notificações de uma doação específica.
     * GET /api/notificacoes/doacao/{doacaoId}
     */
    @GetMapping("/doacao/{doacaoId}")
    public ResponseEntity<List<Notificacao>> listarPorDoacao(@PathVariable Long doacaoId) {
        List<Notificacao> notificacoes = notificacaoService.listarPorDoacao(doacaoId);
        return ResponseEntity.ok(notificacoes);
    }

    /**
     * Lista todas as notificações das doações de um aluno.
     * GET /api/notificacoes/aluno/{alunoId}
     */
    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<List<Notificacao>> listarPorAluno(@PathVariable Long alunoId) {
        List<Notificacao> notificacoes = notificacaoService.listarPorAluno(alunoId);
        return ResponseEntity.ok(notificacoes);
    }
}

package com.example.demo.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidade Notificacao — UC06/RF09
 * Registra um aviso sempre que o status de uma doação muda (aprovação ou reprovação).
 */
@Entity
@Table(name = "notificacoes")
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String mensagem;

    @Column(nullable = false)
    private LocalDateTime dataEnvio;

    /** Canal de entrega: SISTEMA, EMAIL, SMS etc. */
    @Column(length = 50)
    private String canal;

    /** Doação que originou esta notificação. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doacao_id", nullable = false)
    private Doacao doacao;

    public Notificacao() {
    }

    public Notificacao(String mensagem, LocalDateTime dataEnvio, String canal, Doacao doacao) {
        this.mensagem = mensagem;
        this.dataEnvio = dataEnvio;
        this.canal = canal;
        this.doacao = doacao;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(LocalDateTime dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public Doacao getDoacao() {
        return doacao;
    }

    public void setDoacao(Doacao doacao) {
        this.doacao = doacao;
    }
}

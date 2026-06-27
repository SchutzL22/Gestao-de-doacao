package com.example.demo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * Representa um item prioritário vinculado a uma Campanha (RF03 / UC13).
 * A FK campanha_id é gerenciada pelo lado @ManyToOne desta entidade.
 */
@Entity
@Table(name = "itens_prioritarios")
public class ItemPrioritario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private Integer meta;
    private String urgencia;

    /**
     * Relação N:1 com Campanha.
     * @JsonIgnore evita loop infinito na serialização JSON
     * (Campanha já serializa a lista; o item não precisa repetir a campanha).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campanha_id")
    @JsonIgnore
    private Campanha campanha;

    public ItemPrioritario() {
    }

    public ItemPrioritario(String nome, Integer meta, String urgencia) {
        this.nome = nome;
        this.meta = meta;
        this.urgencia = urgencia;
    }

    // ── Getters and Setters ────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getMeta() {
        return meta;
    }

    public void setMeta(Integer meta) {
        this.meta = meta;
    }

    public String getUrgencia() {
        return urgencia;
    }

    public void setUrgencia(String urgencia) {
        this.urgencia = urgencia;
    }

    public Campanha getCampanha() {
        return campanha;
    }

    public void setCampanha(Campanha campanha) {
        this.campanha = campanha;
    }
}

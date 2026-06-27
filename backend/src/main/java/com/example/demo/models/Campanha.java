package com.example.demo.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade Campanha — gerencia o ciclo de vida das campanhas de doação (UC13 / RF03).
 * Relacionamento 1:N com ItemPrioritario: uma campanha pode ter múltiplos itens
 * prioritários que são persistidos em cascata.
 */
@Entity
@Table(name = "campanhas")
public class Campanha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String descricao;
    private LocalDate dataLimite;
    private String status;
    private String regrasConversao;
    private Double limiteHoras;

    /**
     * Itens prioritários vinculados a esta campanha.
     * cascade=ALL: criar/atualizar/deletar itens junto com a campanha.
     * orphanRemoval=true: itens removidos da lista são excluídos do banco.
     */
    @OneToMany(mappedBy = "campanha",
               cascade = CascadeType.ALL,
               orphanRemoval = true,
               fetch = FetchType.EAGER)
    private List<ItemPrioritario> itensPrioritarios = new ArrayList<>();

    public Campanha() {
    }

    public Campanha(String titulo, String descricao, LocalDate dataLimite,
                    String status, String regrasConversao, Double limiteHoras) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.dataLimite = dataLimite;
        this.status = status;
        this.regrasConversao = regrasConversao;
        this.limiteHoras = limiteHoras;
    }

    // ── Getters and Setters ────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDate getDataLimite() { return dataLimite; }
    public void setDataLimite(LocalDate dataLimite) { this.dataLimite = dataLimite; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRegrasConversao() { return regrasConversao; }
    public void setRegrasConversao(String regrasConversao) { this.regrasConversao = regrasConversao; }

    public Double getLimiteHoras() { return limiteHoras; }
    public void setLimiteHoras(Double limiteHoras) { this.limiteHoras = limiteHoras; }

    public List<ItemPrioritario> getItensPrioritarios() { return itensPrioritarios; }
    public void setItensPrioritarios(List<ItemPrioritario> itensPrioritarios) {
        this.itensPrioritarios = itensPrioritarios;
    }

    /**
     * Helper para adicionar um item mantendo a referência bidirecional.
     */
    public void addItemPrioritario(ItemPrioritario item) {
        item.setCampanha(this);
        this.itensPrioritarios.add(item);
    }

    /**
     * Remove todos os itens atuais e substitui pela nova lista,
     * mantendo a referência bidirecional em cada item.
     */
    public void sincronizarItens(List<ItemPrioritario> novosItens) {
        this.itensPrioritarios.clear();
        if (novosItens != null) {
            for (ItemPrioritario item : novosItens) {
                item.setCampanha(this);
                this.itensPrioritarios.add(item);
            }
        }
    }
}

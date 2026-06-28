package com.example.demo.models;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "doacoes")
public class Doacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "aluno_id", nullable = true)
    private Aluno aluno;

    @ManyToOne
    @JoinColumn(name = "campanha_id", nullable = false)
    private Campanha campanha;

    private LocalDate data;
    private Integer quantidade;
    private String tipoItem;

    @jakarta.persistence.Convert(converter = StatusDoacaoConverter.class)
    private StatusDoacao status;

    private String fotoEvidencia;
    private String motivoReprovacao;
    private Double horasConcedidas;
    private String localEntrega;

    @jakarta.persistence.OneToMany(mappedBy = "doacao", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true, fetch = jakarta.persistence.FetchType.EAGER)
    private java.util.List<Anexo> anexos = new java.util.ArrayList<>();

    public Doacao() {
    }

    public Doacao(Aluno aluno, Campanha campanha, LocalDate data, Integer quantidade, String tipoItem, StatusDoacao status, String fotoEvidencia, String motivoReprovacao, Double horasConcedidas) {
        this.aluno = aluno;
        this.campanha = campanha;
        this.data = data;
        this.quantidade = quantidade;
        this.tipoItem = tipoItem;
        this.status = status;
        this.fotoEvidencia = fotoEvidencia;
        this.motivoReprovacao = motivoReprovacao;
        this.horasConcedidas = horasConcedidas;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public Campanha getCampanha() {
        return campanha;
    }

    public void setCampanha(Campanha campanha) {
        this.campanha = campanha;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public String getTipoItem() {
        return tipoItem;
    }

    public void setTipoItem(String tipoItem) {
        this.tipoItem = tipoItem;
    }

    public StatusDoacao getStatus() {
        return status;
    }

    public void setStatus(StatusDoacao status) {
        this.status = status;
    }

    public String getFotoEvidencia() {
        return fotoEvidencia;
    }

    public void setFotoEvidencia(String fotoEvidencia) {
        this.fotoEvidencia = fotoEvidencia;
    }

    public String getMotivoReprovacao() {
        return motivoReprovacao;
    }

    public void setMotivoReprovacao(String motivoReprovacao) {
        this.motivoReprovacao = motivoReprovacao;
    }

    public Double getHorasConcedidas() {
        return horasConcedidas;
    }

    public void setHorasConcedidas(Double horasConcedidas) {
        this.horasConcedidas = horasConcedidas;
    }

    public String getLocalEntrega() {
        return localEntrega;
    }

    public void setLocalEntrega(String localEntrega) {
        this.localEntrega = localEntrega;
    }

    public java.util.List<Anexo> getAnexos() {
        return anexos;
    }

    public void setAnexos(java.util.List<Anexo> anexos) {
        this.anexos = anexos;
    }

    public void addAnexo(Anexo anexo) {
        anexo.setDoacao(this);
        this.anexos.add(anexo);
    }
}

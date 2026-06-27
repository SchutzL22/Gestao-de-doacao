package com.example.demo.services;

import com.example.demo.models.Campanha;
import com.example.demo.models.ItemPrioritario;
import com.example.demo.repositories.CampanhaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CampanhaService {

    @Autowired
    private CampanhaRepository campanhaRepository;

    // ── READ ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Campanha> listarTodas() {
        return campanhaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Campanha> listarAtivas() {
        return campanhaRepository.findByStatus("Ativa");
    }

    @Transactional(readOnly = true)
    public Optional<Campanha> buscarPorId(Long id) {
        return campanhaRepository.findById(id);
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * Persiste uma nova campanha com seus itens prioritários (UC13 / RF03).
     * Os itens recebidos no payload JSON são vinculados à campanha antes de salvar.
     */
    @Transactional
    public Campanha criar(Campanha campanha) {
        validarCampanha(campanha);
        if (campanha.getStatus() == null || campanha.getStatus().trim().isEmpty()) {
            campanha.setStatus("Ativa");
        }
        // Garantir que não é uma atualização acidental
        campanha.setId(null);

        // Vincula cada item à campanha (referência bidirecional)
        List<ItemPrioritario> itens = campanha.getItensPrioritarios();
        campanha.setItensPrioritarios(null); // evita conflito de estado transitório
        Campanha salva = campanhaRepository.save(campanha);

        if (itens != null && !itens.isEmpty()) {
            salva.sincronizarItens(itens);
            salva = campanhaRepository.save(salva);
        }

        return salva;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    /**
     * Atualiza uma campanha existente e sincroniza sua lista de itens (UC13).
     * orphanRemoval=true cuida da exclusão de itens removidos.
     */
    @Transactional
    public Campanha atualizar(Long id, Campanha dados) {
        Campanha existente = campanhaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campanha não encontrada com ID: " + id));

        if (dados.getTitulo() != null && !dados.getTitulo().trim().isEmpty()) {
            existente.setTitulo(dados.getTitulo());
        }
        if (dados.getDescricao() != null) {
            existente.setDescricao(dados.getDescricao());
        }
        if (dados.getDataLimite() != null) {
            existente.setDataLimite(dados.getDataLimite());
        }
        if (dados.getStatus() != null && !dados.getStatus().trim().isEmpty()) {
            existente.setStatus(dados.getStatus());
        }
        if (dados.getRegrasConversao() != null) {
            existente.setRegrasConversao(dados.getRegrasConversao());
        }
        if (dados.getLimiteHoras() != null && dados.getLimiteHoras() > 0) {
            existente.setLimiteHoras(dados.getLimiteHoras());
        }

        // Sincroniza a lista de itens se vier no payload
        if (dados.getItensPrioritarios() != null) {
            existente.sincronizarItens(dados.getItensPrioritarios());
        }

        return campanhaRepository.save(existente);
    }

    /**
     * Atalho para encerrar uma campanha sem excluí-la (manter histórico).
     */
    @Transactional
    public Campanha encerrar(Long id) {
        Campanha campanha = campanhaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campanha não encontrada com ID: " + id));
        campanha.setStatus("Encerrada");
        return campanhaRepository.save(campanha);
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Transactional
    public void deletar(Long id) {
        if (!campanhaRepository.existsById(id)) {
            throw new IllegalArgumentException("Campanha não encontrada com ID: " + id);
        }
        campanhaRepository.deleteById(id);
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private void validarCampanha(Campanha campanha) {
        if (campanha.getTitulo() == null || campanha.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("O título da campanha é obrigatório.");
        }
    }
}

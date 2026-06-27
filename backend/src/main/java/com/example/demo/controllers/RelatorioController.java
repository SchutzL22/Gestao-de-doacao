package com.example.demo.controllers;

import com.example.demo.models.StatusDoacao;
import com.example.demo.repositories.DoacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RF12 — Relatórios consolidados.
 * GET /api/relatorios — dados agregados de doações por status e por campanha.
 */
@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    @Autowired
    private DoacaoRepository doacaoRepository;

    @Autowired
    private com.example.demo.services.RelatorioService relatorioService;

    @GetMapping("/download")
    public ResponseEntity<byte[]> baixarRelatorioPDF() {
        byte[] pdfBytes = relatorioService.gerarRelatorioPDF();
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=relatorio_consolidado.pdf")
                .body(pdfBytes);
    }

    /**
     * Retorna um JSON com:
     * - totais gerais (aprovadas, reprovadas, pendentes, horas concedidas)
     * - agrupamento por campanha (total de doações e horas por campanha)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getRelatorio() {
        long totalDoacoes    = doacaoRepository.count();
        long totalAprovadas  = doacaoRepository.countByStatus(StatusDoacao.APROVADA);
        long totalReprovadas = doacaoRepository.countByStatus(StatusDoacao.REPROVADA);
        long totalPendentes  = doacaoRepository.countByStatus(StatusDoacao.PENDENTE);

        // Soma total de horas concedidas (apenas aprovadas)
        double totalHoras = doacaoRepository.findByStatus(StatusDoacao.APROVADA)
                .stream()
                .mapToDouble(d -> d.getHorasConcedidas() != null ? d.getHorasConcedidas() : 0)
                .sum();

        // Agrupamento por campanha
        List<Object[]> rawPorCampanha = doacaoRepository.findRelatorioPorCampanha();
        List<Map<String, Object>> porCampanha = new ArrayList<>();
        for (Object[] row : rawPorCampanha) {
            Map<String, Object> linha = new LinkedHashMap<>();
            linha.put("campanhaTitulo",   row[0] != null ? row[0].toString() : "—");
            linha.put("totalDoacoes",     ((Number) row[1]).longValue());
            linha.put("horasConcedidas",  ((Number) row[2]).doubleValue());
            porCampanha.add(linha);
        }

        Map<String, Object> relatorio = new LinkedHashMap<>();
        relatorio.put("totalDoacoes",         totalDoacoes);
        relatorio.put("totalAprovadas",        totalAprovadas);
        relatorio.put("totalReprovadas",       totalReprovadas);
        relatorio.put("totalPendentes",        totalPendentes);
        relatorio.put("totalHorasConcedidas",  totalHoras);
        relatorio.put("porCampanha",           porCampanha);

        return ResponseEntity.ok(relatorio);
    }
}

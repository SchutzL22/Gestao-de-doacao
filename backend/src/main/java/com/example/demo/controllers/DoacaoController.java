package com.example.demo.controllers;

import com.example.demo.models.Doacao;
import com.example.demo.models.StatusDoacao;
import com.example.demo.services.DoacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doacoes")
public class DoacaoController {

    @Autowired
    private DoacaoService doacaoService;

    @GetMapping
    public ResponseEntity<List<Doacao>> listarTodas() {
        return ResponseEntity.ok(doacaoService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doacao> buscarPorId(@PathVariable Long id) {
        return doacaoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/aluno/{alunoId}")
    public ResponseEntity<List<Doacao>> buscarPorAlunoId(@PathVariable Long alunoId) {
        return ResponseEntity.ok(doacaoService.buscarPorAlunoId(alunoId));
    }

    @PostMapping(consumes = {"application/json"})
    public ResponseEntity<?> registrarJson(@RequestBody Doacao doacao) {
        try {
            Doacao novaDoacao = doacaoService.registrarDoacao(doacao);
            return ResponseEntity.status(HttpStatus.CREATED).body(novaDoacao);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> registrarMultipart(
            @RequestParam(value = "alunoId", required = false) Long alunoId,
            @RequestParam("campanhaId") Long campanhaId,
            @RequestParam("quantidade") Integer quantidade,
            @RequestParam("tipoItem") String tipoItem,
            @RequestParam(value = "localEntrega", required = false) String localEntrega,
            @RequestParam(value = "foto", required = false) org.springframework.web.multipart.MultipartFile foto) {
        try {
            Doacao doacao = new Doacao();
            
            if (alunoId != null) {
                com.example.demo.models.Aluno aluno = new com.example.demo.models.Aluno();
                aluno.setId(alunoId);
                doacao.setAluno(aluno);
            } else {
                doacao.setAluno(null);
            }
            
            com.example.demo.models.Campanha campanha = new com.example.demo.models.Campanha();
            campanha.setId(campanhaId);
            doacao.setCampanha(campanha);
            
            doacao.setQuantidade(quantidade);
            doacao.setTipoItem(tipoItem);
            doacao.setLocalEntrega(localEntrega);
            
            Doacao novaDoacao = doacaoService.registrarDoacao(doacao, foto);
            return ResponseEntity.status(HttpStatus.CREATED).body(novaDoacao);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/validar")
    public ResponseEntity<?> validar(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            String statusStr = payload.get("status");
            String motivoReprovacao = payload.get("motivoReprovacao");
            
            if (statusStr == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Status é obrigatório para validação."));
            }
            
            StatusDoacao novoStatus;
            try {
                novoStatus = StatusDoacao.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("message", "Status de validação inválido. Use APROVADA ou REPROVADA."));
            }
            
            Doacao doacaoValidada = doacaoService.validarDoacao(id, novoStatus, motivoReprovacao);
            return ResponseEntity.ok(doacaoValidada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editarDoacao(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        try {
            Integer quantidade = payload.get("quantidade") != null ? ((Number) payload.get("quantidade")).intValue() : null;
            String tipoItem = (String) payload.get("tipoItem");
            String matricula = (String) payload.get("matricula");

            Doacao doacaoEditada = doacaoService.editarDoacao(id, quantidade, tipoItem, matricula);
            return ResponseEntity.ok(doacaoEditada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        try {
            doacaoService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}

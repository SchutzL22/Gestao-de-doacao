package com.example.demo.controllers;

import com.example.demo.models.Campanha;
import com.example.demo.services.CampanhaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/campanhas")
public class CampanhaController {

    @Autowired
    private CampanhaService campanhaService;

    @GetMapping
    public ResponseEntity<List<Campanha>> listarTodas() {
        return ResponseEntity.ok(campanhaService.listarTodas());
    }

    /** Retorna apenas as campanhas com status "Ativa" (RF03 — Dashboard). */
    @GetMapping("/ativas")
    public ResponseEntity<List<Campanha>> listarAtivas() {
        return ResponseEntity.ok(campanhaService.listarAtivas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Campanha> buscarPorId(@PathVariable Long id) {
        return campanhaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Cria uma nova campanha (UC13). */
    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Campanha campanha) {
        try {
            Campanha novaCampanha = campanhaService.criar(campanha);
            return ResponseEntity.status(HttpStatus.CREATED).body(novaCampanha);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** Atualiza uma campanha existente (UC13). */
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Campanha campanhaDetails) {
        try {
            Campanha atualizada = campanhaService.atualizar(id, campanhaDetails);
            return ResponseEntity.ok(atualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** Encerra uma campanha sem excluí-la (mantém histórico). */
    @PutMapping("/{id}/encerrar")
    public ResponseEntity<?> encerrar(@PathVariable Long id) {
        try {
            Campanha encerrada = campanhaService.encerrar(id);
            return ResponseEntity.ok(encerrada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        try {
            campanhaService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}

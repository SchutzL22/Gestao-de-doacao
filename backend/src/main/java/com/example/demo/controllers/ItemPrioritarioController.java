package com.example.demo.controllers;

import com.example.demo.models.ItemPrioritario;
import com.example.demo.services.ItemPrioritarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/itens-prioritarios")
public class ItemPrioritarioController { // Wait, the filename will be ItemPrioritarioController.java, so let's name the class ItemPrioritarioController

    @Autowired
    private ItemPrioritarioService itemPrioritarioService;

    @GetMapping
    public ResponseEntity<List<ItemPrioritario>> listarTodos() {
        return ResponseEntity.ok(itemPrioritarioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemPrioritario> buscarPorId(@PathVariable Long id) {
        return itemPrioritarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody ItemPrioritario item) {
        try {
            ItemPrioritario novoItem = itemPrioritarioService.salvar(item);
            return ResponseEntity.status(HttpStatus.CREATED).body(novoItem);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody ItemPrioritario itemDetails) {
        try {
            return itemPrioritarioService.buscarPorId(id).map(item -> {
                item.setNome(itemDetails.getNome());
                item.setMeta(itemDetails.getMeta());
                item.setUrgencia(itemDetails.getUrgencia());
                ItemPrioritario atualizado = itemPrioritarioService.salvar(item);
                return ResponseEntity.ok(atualizado);
            }).orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        try {
            itemPrioritarioService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}

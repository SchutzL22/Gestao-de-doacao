package com.example.demo.services;

import com.example.demo.models.ItemPrioritario;
import com.example.demo.repositories.ItemPrioritarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ItemPrioritarioService {

    @Autowired
    private ItemPrioritarioRepository itemPrioritarioRepository;

    @Transactional(readOnly = true)
    public List<ItemPrioritario> listarTodos() {
        return itemPrioritarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ItemPrioritario> buscarPorId(Long id) {
        return itemPrioritarioRepository.findById(id);
    }

    @Transactional
    public ItemPrioritario salvar(ItemPrioritario item) {
        if (item.getNome() == null || item.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do item é obrigatório.");
        }
        return itemPrioritarioRepository.save(item);
    }

    @Transactional
    public void deletar(Long id) {
        if (!itemPrioritarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Item prioritário não encontrado.");
        }
        itemPrioritarioRepository.deleteById(id);
    }
}

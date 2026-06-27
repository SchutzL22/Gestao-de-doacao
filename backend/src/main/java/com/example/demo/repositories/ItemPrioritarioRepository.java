package com.example.demo.repositories;

import com.example.demo.models.ItemPrioritario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemPrioritarioRepository extends JpaRepository<ItemPrioritario, Long> {
}

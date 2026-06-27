package com.example.demo.repositories;

import com.example.demo.models.Campanha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampanhaRepository extends JpaRepository<Campanha, Long> {

    /** Busca campanhas pelo status (ex: "Ativa", "Encerrada"). */
    List<Campanha> findByStatus(String status);

    /** Busca campanhas cujo título contenha a string informada (case-insensitive). */
    List<Campanha> findByTituloContainingIgnoreCase(String titulo);
}

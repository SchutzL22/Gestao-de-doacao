package com.example.demo.repositories;

import com.example.demo.models.Certificado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificadoRepository extends JpaRepository<Certificado, Long> {
    Optional<Certificado> findByToken(String token);
    Optional<Certificado> findByDoacaoId(Long doacaoId);
}

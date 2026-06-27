package com.example.demo.controllers;

import com.example.demo.models.Certificado;
import com.example.demo.services.CertificadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/certificados")
public class CertificadoController {

    @Autowired
    private CertificadoService certificadoService;

    @GetMapping
    public ResponseEntity<List<Certificado>> listarTodos() {
        return ResponseEntity.ok(certificadoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Certificado> buscarPorId(@PathVariable Long id) {
        return certificadoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/doacao/{doacaoId}")
    public ResponseEntity<Certificado> buscarPorDoacaoId(@PathVariable Long doacaoId) {
        return certificadoService.buscarPorDoacaoId(doacaoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/validar/{token}")
    public ResponseEntity<?> validarToken(@PathVariable String token) {
        return certificadoService.buscarPorToken(token)
                .map(certificado -> ResponseEntity.ok(Map.of(
                        "valido", true,
                        "token", certificado.getToken(),
                        "aluno", certificado.getDoacao().getAluno().getNome(),
                        "campanha", certificado.getDoacao().getCampanha().getTitulo(),
                        "horasConcedidas", certificado.getDoacao().getHorasConcedidas(),
                        "dataEmissao", certificado.getDataEmissao()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * RF10 — Download real do PDF do certificado.
     * GET /api/certificados/download/{doacaoId}
     * Retorna o arquivo PDF com Content-Disposition: attachment para download direto.
     */
    @GetMapping("/download/{doacaoId}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long doacaoId) {
        try {
            byte[] pdfBytes = certificadoService.gerarERetornarPDFBytes(doacaoId);
            String nomeArquivo = certificadoService.getNomeArquivoDownload(doacaoId);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + nomeArquivo + "\"")
                    .body(pdfBytes);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

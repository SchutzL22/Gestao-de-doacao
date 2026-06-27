package com.example.demo.configs;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * GlobalExceptionHandler — tratamento centralizado de exceções da API.
 *
 * Garante que todos os erros retornem um JSON padronizado:
 *   { "erro": "Mensagem legível pelo usuário" }
 *
 * Isso permite que o try/catch do frontend extraia error.message
 * diretamente da propriedade "erro" do JSON de resposta.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Regra de negócio violada — argumento inválido ou estado inválido.
     * Ex: matrícula não encontrada, e-mail duplicado, senha vazia.
     * Status: 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", ex.getMessage()));
    }

    /**
     * Qualquer outra exceção não tratada especificamente.
     * Status: 500 Internal Server Error
     * Não expõe detalhes internos ao cliente.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        ex.printStackTrace();
        String mensagem = ex.getMessage() != null ? ex.getMessage() : "Ocorreu um erro interno no servidor.";
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", mensagem));
    }
}

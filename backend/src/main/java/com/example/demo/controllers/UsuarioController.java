package com.example.demo.controllers;

import com.example.demo.models.Aluno;
import com.example.demo.models.Funcionario;
import com.example.demo.models.Usuario;
import com.example.demo.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // ── READ ──────────────────────────────────────────────────────────────────

    /** Lista todos os usuários (Alunos + Funcionários). */
    @GetMapping
    public ResponseEntity<List<Usuario>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    /** Lista apenas os alunos cadastrados. */
    @GetMapping("/alunos")
    public ResponseEntity<List<Aluno>> listarAlunos() {
        return ResponseEntity.ok(usuarioService.listarAlunos());
    }

    /** Lista apenas os funcionários / administradores cadastrados. */
    @GetMapping("/funcionarios")
    public ResponseEntity<List<Funcionario>> listarFuncionarios() {
        return ResponseEntity.ok(usuarioService.listarFuncionarios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    /** Cadastra um novo aluno (RF01 — domínio @aluno.fmpsc.edu.br obrigatório). */
    @PostMapping("/alunos")
    public ResponseEntity<?> cadastrarAluno(@RequestBody Aluno aluno) {
        try {
            Aluno novoAluno = usuarioService.cadastrarAluno(aluno);
            return ResponseEntity.status(HttpStatus.CREATED).body(novoAluno);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** Cadastra um novo funcionário / administrador (UC12). */
    @PostMapping("/funcionarios")
    public ResponseEntity<?> cadastrarFuncionario(@RequestBody Funcionario funcionario) {
        try {
            Funcionario novoFuncionario = usuarioService.cadastrarFuncionario(funcionario);
            return ResponseEntity.status(HttpStatus.CREATED).body(novoFuncionario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── AUTH ──────────────────────────────────────────────────────────────────

    /**
     * Autentica o usuário e retorna o objeto completo (com tipo: Aluno ou Funcionario)
     * para aplicação do controle de acesso RBAC no frontend (RF14).
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String senha = credentials.get("senha");
            if (email == null || senha == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "E-mail e senha são obrigatórios."));
            }
            Usuario usuario = usuarioService.autenticar(email, senha);
            return ResponseEntity.ok(usuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    /** Atualiza dados de um aluno específico. */
    @PutMapping("/alunos/{id}")
    public ResponseEntity<?> atualizarAluno(@PathVariable Long id, @RequestBody Aluno dados) {
        try {
            Aluno atualizado = usuarioService.atualizarAluno(id, dados);
            return ResponseEntity.ok(atualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** Atualiza dados de um funcionário específico. */
    @PutMapping("/funcionarios/{id}")
    public ResponseEntity<?> atualizarFuncionario(@PathVariable Long id, @RequestBody Funcionario dados) {
        try {
            Funcionario atualizado = usuarioService.atualizarFuncionario(id, dados);
            return ResponseEntity.ok(atualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Endpoint unificado de edição de perfil — usado pela view 'Editar Perfil' do frontend.
     * Detecta automaticamente se o usuário é Aluno ou Funcionário e atualiza:
     *   - nome (sempre, se enviado)
     *   - senha (somente se o campo 'novaSenha' vier preenchido no body)
     * Body esperado: { "nome": "...", "novaSenha": "" }
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarPerfil(@PathVariable Long id,
                                               @RequestBody Map<String, String> body) {
        try {
            Usuario atualizado = usuarioService.atualizarPerfil(id, body);
            return ResponseEntity.ok(atualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** Altera a senha de qualquer usuário (RF02). */
    @PutMapping("/{id}/alterar-senha")
    public ResponseEntity<?> alterarSenha(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String novaSenha = body.get("novaSenha");
            if (novaSenha == null || novaSenha.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Nova senha é obrigatória."));
            }
            Usuario usuario = usuarioService.alterarSenha(id, novaSenha);
            return ResponseEntity.ok(usuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        try {
            usuarioService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}

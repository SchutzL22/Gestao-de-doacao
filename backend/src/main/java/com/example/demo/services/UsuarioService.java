package com.example.demo.services;

import com.example.demo.models.Aluno;
import com.example.demo.models.Funcionario;
import com.example.demo.models.Usuario;
import com.example.demo.repositories.AlunoRepository;
import com.example.demo.repositories.FuncionarioRepository;
import com.example.demo.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // ── READ ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Aluno> listarAlunos() {
        return alunoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Funcionario> listarFuncionarios() {
        return funcionarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    /**
     * Cadastra um novo Aluno.
     * Regra RF01: e-mail deve pertencer ao domínio institucional @aluno.fmpsc.edu.br
     */
    @Transactional
    public Aluno cadastrarAluno(Aluno aluno) {
        if (aluno.getEmail() == null || !aluno.getEmail().endsWith("@aluno.fmpsc.edu.br")) {
            throw new IllegalArgumentException("E-mail do Aluno deve pertencer ao domínio @aluno.fmpsc.edu.br");
        }
        if (usuarioRepository.existsByEmail(aluno.getEmail())) {
            throw new IllegalArgumentException("E-mail já cadastrado!");
        }
        
        // Validação da complexidade da senha (LGPD / RNF05): min 8 caracteres, contendo letras e números
        if (aluno.getSenha() == null || !aluno.getSenha().matches("^(?=.*[a-zA-Z])(?=.*\\d).{8,}$")) {
            throw new IllegalArgumentException("A senha deve ter no mínimo 8 caracteres, contendo letras e números.");
        }
        
        // Hashing com BCrypt
        aluno.setSenha(passwordEncoder.encode(aluno.getSenha()));

        if (aluno.getSaldoHoras() == null) {
            aluno.setSaldoHoras(0.0);
        }
        return alunoRepository.save(aluno);
    }

    /**
     * Cadastra um novo Funcionário/Administrador (Coper).
     */
    @Transactional
    public Funcionario cadastrarFuncionario(Funcionario funcionario) {
        if (funcionario.getEmail() == null || funcionario.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("E-mail é obrigatório!");
        }
        if (usuarioRepository.existsByEmail(funcionario.getEmail())) {
            throw new IllegalArgumentException("E-mail já cadastrado!");
        }
        // Hashing com BCrypt se senha fornecida
        if (funcionario.getSenha() != null && !funcionario.getSenha().trim().isEmpty()) {
            funcionario.setSenha(passwordEncoder.encode(funcionario.getSenha()));
        }
        return funcionarioRepository.save(funcionario);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    /**
     * Altera a senha de qualquer usuário (Aluno ou Funcionário).
     */
    @Transactional
    public Usuario alterarSenha(Long id, String novaSenha) {
        if (novaSenha == null || novaSenha.trim().isEmpty()) {
            throw new IllegalArgumentException("Nova senha não pode ser vazia!");
        }
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado!"));
        
        // Validação da complexidade
        if (!novaSenha.matches("^(?=.*[a-zA-Z])(?=.*\\d).{8,}$")) {
            throw new IllegalArgumentException("A nova senha deve ter no mínimo 8 caracteres, contendo letras e números.");
        }
        
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        return usuarioRepository.save(usuario);
    }

    /**
     * Atualiza dados de um Aluno existente.
     */
    @Transactional
    public Aluno atualizarAluno(Long id, Aluno dados) {
        Aluno aluno = alunoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado!"));

        if (dados.getNome() != null && !dados.getNome().trim().isEmpty()) {
            aluno.setNome(dados.getNome());
        }
        if (dados.getCpf() != null) {
            aluno.setCpf(dados.getCpf());
        }
        if (dados.getMatricula() != null) {
            aluno.setMatricula(dados.getMatricula());
        }
        if (dados.getCurso() != null) {
            aluno.setCurso(dados.getCurso());
        }
        // E-mail e saldo de horas não são atualizáveis pelo próprio usuário
        return alunoRepository.save(aluno);
    }

    /**
     * Atualiza dados de um Funcionário existente.
     */
    @Transactional
    public Funcionario atualizarFuncionario(Long id, Funcionario dados) {
        Funcionario func = funcionarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado!"));

        if (dados.getNome() != null && !dados.getNome().trim().isEmpty()) {
            func.setNome(dados.getNome());
        }
        if (dados.getCpf() != null) {
            func.setCpf(dados.getCpf());
        }
        if (dados.getDepartamento() != null) {
            func.setDepartamento(dados.getDepartamento());
        }
        if (dados.getCargo() != null) {
            func.setCargo(dados.getCargo());
        }
        return funcionarioRepository.save(func);
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Transactional
    public void deletar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuário não encontrado!");
        }
        usuarioRepository.deleteById(id);
    }

    // ── PERFIL (edição unificada) ───────────────────────────────────────

    /**
     * Atualiza nome e (opcionalmente) senha de qualquer usuário.
     * Detecta automaticamente o subtipo (Aluno ou Funcionário).
     *
     * @param id   ID do usuário
     * @param body Map com campos: "nome" (obrigatório), "novaSenha" (opcional — ignorado se vazio)
     * @return usuário atualizado (tipo concreto)
     */
    @Transactional
    public Usuario atualizarPerfil(Long id, java.util.Map<String, String> body) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado!"));

        // Atualiza nome se fornecido
        String novoNome = body.get("nome");
        if (novoNome != null && !novoNome.trim().isEmpty()) {
            usuario.setNome(novoNome.trim());
        }

        // Atualiza senha apenas se o campo vier preenchido (com validação e hash)
        String novaSenha = body.get("novaSenha");
        if (novaSenha != null && !novaSenha.trim().isEmpty()) {
            if (!novaSenha.trim().matches("^(?=.*[a-zA-Z])(?=.*\\d).{8,}$")) {
                throw new IllegalArgumentException("A nova senha deve ter no mínimo 8 caracteres, contendo letras e números.");
            }
            usuario.setSenha(passwordEncoder.encode(novaSenha.trim()));
        }

        // Persiste e retorna o tipo concreto para o frontend
        usuarioRepository.save(usuario);

        if (usuario instanceof Aluno) {
            return alunoRepository.findById(id).orElse((Aluno) usuario);
        } else if (usuario instanceof Funcionario) {
            return funcionarioRepository.findById(id).orElse((Funcionario) usuario);
        }
        return usuario;
    }

    // ── AUTH ──────────────────────────────────────────────────────────────────

    /**
     * Autentica o usuário e retorna o objeto concreto (Aluno ou Funcionario)
     * para que o frontend possa aplicar controle de acesso (RBAC / RF14).
     *
     * O objeto retornado é o tipo concreto obtido via polimorfismo JPA JOINED,
     * portanto o JSON incluirá os campos específicos do subtipo.
     */
    @Transactional
    public Usuario autenticar(String email, String senha) {
        if (email == null || senha == null) {
            throw new IllegalArgumentException("E-mail e senha são obrigatórios.");
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com este e-mail!"));

        // Verifica se a conta está no período de bloqueio temporário
        if (usuario.getBloqueadoAte() != null && usuario.getBloqueadoAte().isAfter(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("Conta temporariamente bloqueada devido a excesso de tentativas. Tente novamente mais tarde.");
        }

        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            int falhas = usuario.getTentativasFalhas() != null ? usuario.getTentativasFalhas() + 1 : 1;
            usuario.setTentativasFalhas(falhas);
            if (falhas >= 3) {
                usuario.setBloqueadoAte(java.time.LocalDateTime.now().plusMinutes(15));
                usuarioRepository.save(usuario);
                throw new IllegalArgumentException("Conta temporariamente bloqueada por 15 minutos devido a 3 tentativas falhas.");
            }
            usuarioRepository.save(usuario);
            throw new IllegalArgumentException("Senha incorreta! Tentativas falhas: " + falhas + "/3");
        }

        // Login bem-sucedido — reseta o contador de falhas
        usuario.setTentativasFalhas(0);
        usuario.setBloqueadoAte(null);
        usuarioRepository.save(usuario);

        // Retorna o tipo concreto para suporte a RBAC no frontend
        if (usuario instanceof Aluno) {
            return alunoRepository.findById(usuario.getId()).orElse((Aluno) usuario);
        } else if (usuario instanceof Funcionario) {
            return funcionarioRepository.findById(usuario.getId()).orElse((Funcionario) usuario);
        }

        return usuario;
    }
}

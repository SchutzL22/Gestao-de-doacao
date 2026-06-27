package com.example.demo.configs;

import com.example.demo.models.Aluno;
import com.example.demo.models.Campanha;
import com.example.demo.models.Funcionario;
import com.example.demo.repositories.AlunoRepository;
import com.example.demo.repositories.CampanhaRepository;
import com.example.demo.repositories.FuncionarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;

/**
 * DataSeeder — executado automaticamente ao iniciar a aplicação.
 *
 * Garante a criação obrigatória do Administrador Geral no banco db_gestaodedoacao
 * mesmo que já existam outros usuários cadastrados (RN02).
 * A verificação é feita pelo e-mail do admin, não pela contagem total.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "admin@fmp.edu.br";
    private static final String ALUNO_TESTE_EMAIL = "aluno@aluno.fmpsc.edu.br";

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private CampanhaRepository campanhaRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Correção de DDL para permitir aluno_id como NULL na tabela doacoes
        try {
            entityManager.createNativeQuery("ALTER TABLE doacoes MODIFY COLUMN aluno_id BIGINT NULL").executeUpdate();
            System.out.println("[DataSeeder] Tabela doacoes alterada para aceitar aluno_id NULL.");
        } catch (Exception e) {
            System.out.println("[DataSeeder] Ignorando alteração DDL de doacoes (já nulo ou outro dialeto): " + e.getMessage());
        }

        // Correção de DDL para permitir status como VARCHAR(50) para evitar truncamento de novos ENUMs
        try {
            entityManager.createNativeQuery("ALTER TABLE doacoes MODIFY COLUMN status VARCHAR(50)").executeUpdate();
            System.out.println("[DataSeeder] Tabela doacoes alterada para aceitar status VARCHAR(50).");
        } catch (Exception e) {
            System.out.println("[DataSeeder] Ignorando alteração DDL de status em doacoes: " + e.getMessage());
        }

        // ── ADMINISTRADOR GERAL (obrigatório — RN02) ──────────────────────────
        // Verifica pelo e-mail específico do admin, garantindo que ele sempre
        // exista no banco, independente de outros registros presentes.
        if (!funcionarioRepository.existsByEmail(ADMIN_EMAIL)) {
            Funcionario admin = new Funcionario();
            admin.setNome("Administrador Geral");
            admin.setCpf("000.000.000-00");
            admin.setEmail(ADMIN_EMAIL);
            admin.setSenha(passwordEncoder.encode("admin123"));
            admin.setDepartamento("COPER");
            admin.setCargo("Administrador");
            funcionarioRepository.save(admin);
            System.out.println("[DataSeeder] Administrador Geral criado com sucesso.");
        } else {
            Funcionario admin = funcionarioRepository.findByEmail(ADMIN_EMAIL).orElse(null);
            if (admin != null && (!admin.getSenha().startsWith("$2a$") || !passwordEncoder.matches("admin123", admin.getSenha()))) {
                admin.setSenha(passwordEncoder.encode("admin123"));
                funcionarioRepository.save(admin);
                System.out.println("[DataSeeder] Administrador Geral senha redefinida/criptografada.");
            } else {
                System.out.println("[DataSeeder] Administrador Geral já existe — nenhuma ação necessária.");
            }
        }

        // ── ALUNO DE TESTES ───────────────────────────────────────────────────
        // Criado apenas para facilitar testes do fluxo de alunos em ambiente dev.
        if (!alunoRepository.findByEmail(ALUNO_TESTE_EMAIL).isPresent()) {
            Aluno aluno = new Aluno();
            aluno.setNome("Davi Aravechia");
            aluno.setCpf("222.333.444-55");
            aluno.setEmail(ALUNO_TESTE_EMAIL);
            aluno.setSenha(passwordEncoder.encode("aluno"));
            aluno.setMatricula("19875");
            aluno.setCurso("Análise e Desenvolvimento de Sistemas");
            aluno.setSaldoHoras(0.0);
            alunoRepository.save(aluno);
            System.out.println("[DataSeeder] Aluno de teste criado com sucesso.");
        } else {
            Aluno aluno = alunoRepository.findByEmail(ALUNO_TESTE_EMAIL).orElse(null);
            if (aluno != null && (!aluno.getSenha().startsWith("$2a$") || !passwordEncoder.matches("aluno", aluno.getSenha()))) {
                aluno.setSenha(passwordEncoder.encode("aluno"));
                alunoRepository.save(aluno);
                System.out.println("[DataSeeder] Aluno de teste senha redefinida/criptografada.");
            } else {
                System.out.println("[DataSeeder] Aluno de teste já existe — nenhuma ação necessária.");
            }
        }

        // ── CAMPANHAS INICIAIS ────────────────────────────────────────────────
        if (campanhaRepository.count() == 0) {
            Campanha c1 = new Campanha();
            c1.setTitulo("Inverno Quente");
            c1.setDescricao("Arrecadação de agasalhos e cobertores para famílias em situação de vulnerabilidade.");
            c1.setStatus("Ativa");
            c1.setDataLimite(LocalDate.now().plusMonths(3));
            c1.setRegrasConversao("1 agasalho = 1h | 1 cobertor = 2h");
            c1.setLimiteHoras(30.0);
            campanhaRepository.save(c1);

            Campanha c2 = new Campanha();
            c2.setTitulo("Natal Solidário");
            c2.setDescricao("Arrecadação de alimentos não perecíveis para cestas básicas.");
            c2.setStatus("Ativa");
            c2.setDataLimite(LocalDate.now().plusMonths(6));
            c2.setRegrasConversao("3kg de alimento = 1h");
            c2.setLimiteHoras(15.0);
            campanhaRepository.save(c2);

            System.out.println("[DataSeeder] Campanhas iniciais criadas com sucesso.");
        } else {
            System.out.println("[DataSeeder] Campanhas já existem — nenhuma ação necessária.");
        }
    }
}

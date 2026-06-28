package com.example.demo;

import com.example.demo.models.*;
import com.example.demo.repositories.*;
import com.example.demo.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SystemFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private DoacaoRepository doacaoRepository;

    @Autowired
    private DoacaoService doacaoService;

    @Autowired
    private CampanhaRepository campanhaRepository;

    @Autowired
    private CertificadoRepository certificadoRepository;

    private Aluno alunoDavi;
    private Campanha campanhaInverno;

    @BeforeEach
    public void setup() {
        // Garantir que o aluno Davi exista no banco (usado para login e doações)
        Optional<Usuario> uOpt = usuarioRepository.findByEmail("davi@aluno.fmpsc.edu.br");
        if (uOpt.isPresent()) {
            alunoDavi = (Aluno) uOpt.get();
            // Limpar doações residuais de execuções de testes anteriores
            java.util.List<Doacao> residuais = doacaoRepository.findByAlunoId(alunoDavi.getId());
            for (Doacao d : residuais) {
                try {
                    doacaoService.deletar(d.getId());
                } catch (Exception ignored) {}
            }
            // Resetar saldo de horas para 0.0 para garantir previsibilidade de testes
            alunoDavi.setSaldoHoras(0.0);
            alunoRepository.save(alunoDavi);
        } else {
            Aluno novoAluno = new Aluno();
            novoAluno.setNome("Davi");
            novoAluno.setCpf("111.111.111-11");
            novoAluno.setEmail("davi@aluno.fmpsc.edu.br");
            novoAluno.setSenha("$2a$10$yDSG8/NNll0eOOXXBujQUO/beqHb1tU1UUTbjpVp9XxmoI8aXTnYG");
            novoAluno.setMatricula("19875");
            novoAluno.setCurso("Análise e Desenvolvimento de Sistemas");
            novoAluno.setSaldoHoras(0.0);
            alunoDavi = alunoRepository.save(novoAluno);
        }

        // Garantir que exista uma campanha ativa
        Optional<Campanha> cOpt = campanhaRepository.findAll().stream()
                .filter(c -> "Ativa".equalsIgnoreCase(c.getStatus()) && (c.getDataLimite() == null || c.getDataLimite().isAfter(LocalDate.now())))
                .findFirst();
        if (cOpt.isPresent()) {
            campanhaInverno = cOpt.get();
        } else {
            Campanha novaCampanha = new Campanha();
            novaCampanha.setTitulo("Campanha de Inverno 2026");
            novaCampanha.setDescricao("Arrecadação de agasalhos e cobertores.");
            novaCampanha.setDataLimite(LocalDate.now().plusDays(30));
            novaCampanha.setStatus("Ativa");
            novaCampanha.setLimiteHoras(40.00);
            campanhaInverno = campanhaRepository.save(novaCampanha);
        }
    }

    @Test
    public void testAuthenticationSuccess() throws Exception {
        String jsonRequest = "{\"email\":\"davi@aluno.fmpsc.edu.br\",\"senha\":\"davi123\"}";

        mockMvc.perform(post("/api/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("davi@aluno.fmpsc.edu.br"))
                .andExpect(jsonPath("$.nome").exists());
    }

    @Test
    public void testAuthenticationFail() throws Exception {
        String jsonRequest = "{\"email\":\"davi@aluno.fmpsc.edu.br\",\"senha\":\"senha_errada\"}";

        mockMvc.perform(post("/api/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testRegisterAndApproveAndCascadeDeleteDonationFlow() throws Exception {
        Double saldoHorasInicial = alunoDavi.getSaldoHoras() != null ? alunoDavi.getSaldoHoras() : 0.0;

        // 1. Registrar Doação via API
        String doacaoJson = String.format(
            "{\"aluno\":{\"id\":%d},\"campanha\":{\"id\":%d},\"quantidade\":3,\"tipoItem\":\"Cobertor Grosso\",\"localEntrega\":\"Recepção Principal FMP\"}",
            alunoDavi.getId(),
            campanhaInverno.getId()
        );

        String responseContent = mockMvc.perform(post("/api/doacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(doacaoJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.quantidade").value(3))
                .andReturn().getResponse().getContentAsString();

        // Extrai o ID do JSON retornado
        Long doacaoId = Long.parseLong(
            responseContent.substring(responseContent.indexOf("\"id\":") + 5, responseContent.indexOf(",", responseContent.indexOf("\"id\":")))
        );

        // 2. Validar/Aprovar Doação via API
        String validationJson = "{\"status\":\"APROVADA\"}";

        mockMvc.perform(put("/api/doacoes/" + doacaoId + "/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validationJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.horasConcedidas").value(6.0)); // 3 quantidade * 2 horas = 6.0 horas

        // Verificar se o saldo do aluno aumentou
        Aluno alunoAtualizado = alunoRepository.findById(alunoDavi.getId()).get();
        assertEquals(saldoHorasInicial + 6.0, alunoAtualizado.getSaldoHoras(), 0.01);

        // Verificar se o Certificado correspondente foi criado no banco
        Optional<Certificado> certificadoOpt = certificadoRepository.findByDoacaoId(doacaoId);
        assertTrue(certificadoOpt.isPresent());
        assertNotNull(certificadoOpt.get().getToken());

        // 3. Deletar Doação (deve descontar horas e apagar o certificado associado)
        mockMvc.perform(delete("/api/doacoes/" + doacaoId))
                .andExpect(status().isNoContent());

        // Verificar remoção da doação e certificado
        assertFalse(doacaoRepository.findById(doacaoId).isPresent());
        assertFalse(certificadoRepository.findByDoacaoId(doacaoId).isPresent());

        // Verificar se as horas foram devidamente estornadas do aluno
        Aluno alunoDepoisDeletar = alunoRepository.findById(alunoDavi.getId()).get();
        assertEquals(saldoHorasInicial, alunoDepoisDeletar.getSaldoHoras(), 0.01);
    }
}

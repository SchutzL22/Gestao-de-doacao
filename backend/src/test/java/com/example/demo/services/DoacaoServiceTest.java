package com.example.demo.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.models.Aluno;
import com.example.demo.models.Campanha;
import com.example.demo.models.Certificado;
import com.example.demo.models.Doacao;
import com.example.demo.models.StatusDoacao;
import com.example.demo.repositories.AlunoRepository;
import com.example.demo.repositories.CampanhaRepository;
import com.example.demo.repositories.CertificadoRepository;
import com.example.demo.repositories.DoacaoRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DoacaoServiceTest {

    @Mock
    private DoacaoRepository doacaoRepository;

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private CampanhaRepository campanhaRepository;

    @Mock
    private CertificadoRepository certificadoRepository;

    @Mock
    private CertificadoService certificadoService;

    @Mock
    private NotificacaoService notificacaoService;

    @InjectMocks
    private DoacaoService doacaoService;

    @Test
    public void testRegistrarDoacao_Success() {
        Campanha campanha = new Campanha();
        campanha.setId(1L);
        campanha.setStatus("Ativa");

        Aluno aluno = new Aluno();
        aluno.setId(1L);

        Doacao doacao = new Doacao();
        doacao.setCampanha(campanha);
        doacao.setAluno(aluno);
        doacao.setQuantidade(5);
        doacao.setTipoItem("Casaco");

        when(campanhaRepository.findById(1L)).thenReturn(Optional.of(campanha));
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(doacaoRepository.save(any(Doacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Doacao saved = doacaoService.registrarDoacao(doacao);

        assertNotNull(saved);
        assertEquals(StatusDoacao.PENDENTE, saved.getStatus());
        assertEquals(0.0, saved.getHorasConcedidas());
        assertEquals(5, saved.getQuantidade());
    }

    @Test
    public void testRegistrarDoacao_Fail_CampanhaEncerrada() {
        Campanha campanha = new Campanha();
        campanha.setId(1L);
        campanha.setStatus("Encerrada");

        Doacao doacao = new Doacao();
        doacao.setCampanha(campanha);
        doacao.setQuantidade(5);

        when(campanhaRepository.findById(1L)).thenReturn(Optional.of(campanha));

        assertThrows(IllegalArgumentException.class, () -> {
            doacaoService.registrarDoacao(doacao);
        });
    }

    @Test
    public void testRegistrarDoacao_Fail_QuantidadeZero() {
        Campanha campanha = new Campanha();
        campanha.setId(1L);
        campanha.setStatus("Ativa");

        Doacao doacao = new Doacao();
        doacao.setCampanha(campanha);
        doacao.setQuantidade(0); // quantidade igual a zero

        when(campanhaRepository.findById(1L)).thenReturn(Optional.of(campanha));

        assertThrows(IllegalArgumentException.class, () -> {
            doacaoService.registrarDoacao(doacao);
        });
    }

    @Test
    public void testRegistrarDoacao_Fail_DataExpirada() {
        Campanha campanha = new Campanha();
        campanha.setId(1L);
        campanha.setStatus("Ativa");
        campanha.setDataLimite(LocalDate.now().minusDays(1)); // expired yesterday

        Doacao doacao = new Doacao();
        doacao.setCampanha(campanha);
        doacao.setQuantidade(5);

        when(campanhaRepository.findById(1L)).thenReturn(Optional.of(campanha));

        assertThrows(IllegalArgumentException.class, () -> {
            doacaoService.registrarDoacao(doacao);
        });
    }

    @Test
    public void testValidarDoacao_Aprovar_Success() {
        Aluno aluno = new Aluno();
        aluno.setId(1L);
        aluno.setMatricula("12345");
        aluno.setSaldoHoras(10.0);

        Campanha campanha = new Campanha();
        campanha.setId(1L);
        campanha.setTitulo("Frio Solidário");
        campanha.setLimiteHoras(20.0);

        Doacao doacao = new Doacao();
        doacao.setId(10L);
        doacao.setAluno(aluno);
        doacao.setCampanha(campanha);
        doacao.setStatus(StatusDoacao.PENDENTE);
        doacao.setQuantidade(3); // should award 6 hours
        doacao.setTipoItem("Casaco");

        when(doacaoRepository.findById(10L)).thenReturn(Optional.of(doacao));
        when(doacaoRepository.save(any(Doacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(alunoRepository.save(any(Aluno.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(doacaoRepository.findByAlunoAndCampanhaAndStatus(aluno, campanha, StatusDoacao.APROVADA)).thenReturn(new ArrayList<>());

        Doacao validada = doacaoService.validarDoacao(10L, StatusDoacao.APROVADA, null);

        assertNotNull(validada);
        assertEquals(StatusDoacao.APROVADA, validada.getStatus());
        assertEquals(6.0, validada.getHorasConcedidas());
        assertEquals(16.0, aluno.getSaldoHoras());
        verify(certificadoRepository, times(1)).save(any(Certificado.class));
        verify(notificacaoService, times(1)).registrarNotificacao(eq(doacao), anyString());
    }

    @Test
    public void testValidarDoacao_Reprovar_Success() {
        Campanha campanha = new Campanha();
        campanha.setId(1L);
        campanha.setTitulo("Alimentos");

        Doacao doacao = new Doacao();
        doacao.setId(10L);
        doacao.setCampanha(campanha);
        doacao.setStatus(StatusDoacao.PENDENTE);

        when(doacaoRepository.findById(10L)).thenReturn(Optional.of(doacao));
        when(doacaoRepository.save(any(Doacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Doacao validada = doacaoService.validarDoacao(10L, StatusDoacao.REPROVADA, "Foto sem nitidez");

        assertNotNull(validada);
        assertEquals(StatusDoacao.REPROVADA, validada.getStatus());
        assertEquals("Foto sem nitidez", validada.getMotivoReprovacao());
        verify(notificacaoService, times(1)).registrarNotificacao(eq(doacao), anyString());
    }

    @Test
    public void testDeletar_ApprovedDoacao_ClearsSaldoAndCertificate() {
        Aluno aluno = new Aluno();
        aluno.setId(1L);
        aluno.setSaldoHoras(20.0);

        Doacao doacao = new Doacao();
        doacao.setId(10L);
        doacao.setStatus(StatusDoacao.APROVADA);
        doacao.setHorasConcedidas(6.0);
        doacao.setAluno(aluno);

        Certificado certificado = new Certificado();

        when(doacaoRepository.findById(10L)).thenReturn(Optional.of(doacao));
        when(certificadoRepository.findByDoacaoId(10L)).thenReturn(Optional.of(certificado));

        doacaoService.deletar(10L);

        assertEquals(14.0, aluno.getSaldoHoras());
        verify(alunoRepository, times(1)).save(aluno);
        verify(certificadoRepository, times(1)).delete(certificado);
        verify(doacaoRepository, times(1)).delete(doacao);
    }
}

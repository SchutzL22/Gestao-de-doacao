package com.example.demo.services;

import com.example.demo.models.Aluno;
import com.example.demo.models.Campanha;
import com.example.demo.models.Certificado;
import com.example.demo.models.Doacao;
import com.example.demo.models.StatusDoacao;
import com.example.demo.repositories.AlunoRepository;
import com.example.demo.repositories.CampanhaRepository;
import com.example.demo.repositories.CertificadoRepository;
import com.example.demo.repositories.DoacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DoacaoService {

    @Autowired
    private DoacaoRepository doacaoRepository;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private CampanhaRepository campanhaRepository;

    @Autowired
    private CertificadoRepository certificadoRepository;

    /** UC07/RF10: Geração física do PDF do certificado após aprovação. */
    @Autowired
    private CertificadoService certificadoService;

    /** UC06/RF09: Registro de notificações ao mudar status da doação. */
    @Autowired
    private NotificacaoService notificacaoService;

    @Transactional(readOnly = true)
    public List<Doacao> listarTodas() {
        return doacaoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Doacao> buscarPorId(Long id) {
        return doacaoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Doacao> buscarPorAlunoId(Long alunoId) {
        return doacaoRepository.findByAlunoId(alunoId);
    }

    @Transactional
    public Doacao registrarDoacao(Doacao doacao) {
        if (doacao.getCampanha() == null || doacao.getCampanha().getId() == null) {
            throw new IllegalArgumentException("A campanha é obrigatória para registrar uma doação.");
        }

        Campanha campanha = campanhaRepository.findById(doacao.getCampanha().getId())
                .orElseThrow(() -> new IllegalArgumentException("Campanha não encontrada com o ID especificado."));

        // RN01: Campanha deve estar ativa e dentro do prazo
        if ("Encerrada".equalsIgnoreCase(campanha.getStatus())) {
            throw new IllegalArgumentException("Esta campanha já foi encerrada.");
        }
        if (campanha.getDataLimite() != null && campanha.getDataLimite().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("A data limite desta campanha já expirou.");
        }

        if (doacao.getAluno() != null && doacao.getAluno().getId() != null) {
            Aluno aluno = alunoRepository.findById(doacao.getAluno().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado com o ID especificado."));
            doacao.setAluno(aluno);
            doacao.setStatus(StatusDoacao.PENDENTE);
        } else {
            doacao.setAluno(null);
            doacao.setStatus(StatusDoacao.AGUARDANDO_CORRECAO);
        }

        doacao.setCampanha(campanha);
        
        if (doacao.getData() == null) {
            doacao.setData(LocalDate.now());
        }
        
        doacao.setHorasConcedidas(0.0);
        
        if (doacao.getQuantidade() == null || doacao.getQuantidade() <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }

        return doacaoRepository.save(doacao);
    }

    @Transactional
    public Doacao validarDoacao(Long doacaoId, StatusDoacao novoStatus, String motivoReprovacao) {
        Doacao doacao = doacaoRepository.findById(doacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Doação não encontrada."));

        if (doacao.getStatus() != StatusDoacao.PENDENTE) {
            throw new IllegalArgumentException("Esta doação já foi processada.");
        }

        if (novoStatus == StatusDoacao.APROVADA) {
            Aluno aluno = doacao.getAluno();
            
            // RN05: Aprovação de horas exige matrícula ativa do aluno (verificamos se existe e está cadastrado)
            if (aluno == null || aluno.getMatricula() == null || aluno.getMatricula().trim().isEmpty()) {
                throw new IllegalArgumentException("Aluno não possui matrícula ativa.");
            }

            Campanha campanha = doacao.getCampanha();
            
            // Cálculo de horas: default de 2 horas por item doado
            Double horasCalculadas = doacao.getQuantidade() * 2.0;

            // Aplicar limites por campanha se houver
            Double limiteCampanha = campanha.getLimiteHoras();
            if (limiteCampanha != null && limiteCampanha > 0) {
                List<Doacao> aprovadas = doacaoRepository.findByAlunoAndCampanhaAndStatus(aluno, campanha, StatusDoacao.APROVADA);
                Double horasObtidas = aprovadas.stream().mapToDouble(Doacao::getHorasConcedidas).sum();
                
                if (horasObtidas >= limiteCampanha) {
                    horasCalculadas = 0.0;
                } else if (horasObtidas + horasCalculadas > limiteCampanha) {
                    horasCalculadas = limiteCampanha - horasObtidas;
                }
            }

            doacao.setHorasConcedidas(horasCalculadas);
            doacao.setStatus(StatusDoacao.APROVADA);

            // Atualizar saldo de horas do aluno
            Double saldoAtual = aluno.getSaldoHoras() != null ? aluno.getSaldoHoras() : 0.0;
            aluno.setSaldoHoras(saldoAtual + horasCalculadas);
            alunoRepository.save(aluno);

            // UC07 / RN06: Gerar Certificado com Token de autenticidade
            Certificado certificado = new Certificado();
            certificado.setToken(UUID.randomUUID().toString().toUpperCase());
            certificado.setCaminhoArquivo("");  // será preenchido após gerarPDF()
            certificado.setDataEmissao(LocalDateTime.now());
            certificado.setDoacao(doacao);
            certificadoRepository.save(certificado);

            // RF10: Gera o arquivo PDF físico em disco (layout oficial FMP)
            try {
                certificadoService.gerarPDF(doacao, certificado);
            } catch (Exception ex) {
                // Falha na geração do PDF não deve reverter a aprovação;
                // o certificado já está salvo e pode ser regerado.
                System.err.println("[AVISO] Falha ao gerar PDF para doacao " + doacao.getId() + ": " + ex.getMessage());
            }

            // UC06/RF09: Notificação de aprovação
            String msgAprovacao = String.format(
                "Parabéns! Sua doação de %d %s para a campanha '%s' foi aprovada. Horas concedidas: %.1fh. Seu certificado está disponível para download.",
                doacao.getQuantidade(),
                doacao.getTipoItem() != null ? doacao.getTipoItem() : "item(s)",
                doacao.getCampanha().getTitulo(),
                horasCalculadas
            );
            notificacaoService.registrarNotificacao(doacao, msgAprovacao);

        } else if (novoStatus == StatusDoacao.REPROVADA) {
            if (motivoReprovacao == null || motivoReprovacao.trim().isEmpty()) {
                throw new IllegalArgumentException("O motivo da reprovação é obrigatório.");
            }
            doacao.setStatus(StatusDoacao.REPROVADA);
            doacao.setMotivoReprovacao(motivoReprovacao);
            doacao.setHorasConcedidas(0.0);

            // UC06/RF09: Notificação de reprovação
            String msgReprovacao = String.format(
                "Sua doação para a campanha '%s' foi reprovada. Motivo: %s",
                doacao.getCampanha().getTitulo(),
                motivoReprovacao
            );
            notificacaoService.registrarNotificacao(doacao, msgReprovacao);
        } else {
            throw new IllegalArgumentException("Status de validação inválido.");
        }

        return doacaoRepository.save(doacao);
    }

    @Transactional
    public void deletar(Long id) {
        Doacao doacao = doacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doação não encontrada."));

        // Se a doação foi aprovada, precisamos descontar as horas do saldo do aluno
        if (doacao.getStatus() == StatusDoacao.APROVADA) {
            Aluno aluno = doacao.getAluno();
            if (aluno != null && doacao.getHorasConcedidas() != null) {
                Double saldoAtual = aluno.getSaldoHoras() != null ? aluno.getSaldoHoras() : 0.0;
                aluno.setSaldoHoras(Math.max(0.0, saldoAtual - doacao.getHorasConcedidas()));
                alunoRepository.save(aluno);
            }
            // Remove o certificado associado para evitar violação de integridade estrangeira
            Optional<Certificado> certOpt = certificadoRepository.findByDoacaoId(id);
            certOpt.ifPresent(certificado -> certificadoRepository.delete(certificado));
        }

        // 1. Deletar registros de edição e notificações vinculados (evita constraint violations no banco)
        if (entityManager != null) {
            entityManager.createNativeQuery("DELETE FROM registros_edicao WHERE doacao_id = :id")
                    .setParameter("id", id)
                    .executeUpdate();

            entityManager.createNativeQuery("DELETE FROM notificacoes WHERE doacao_id = :id")
                    .setParameter("id", id)
                    .executeUpdate();
        }

        doacaoRepository.delete(doacao);
    }

    private static final java.nio.file.Path rootDir = java.nio.file.Paths.get("uploads");

    private String salvarArquivo(org.springframework.web.multipart.MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            return null;
        }
        try {
            if (!java.nio.file.Files.exists(rootDir)) {
                java.nio.file.Files.createDirectories(rootDir);
            }
            String nomeOriginal = arquivo.getOriginalFilename();
            String extensao = "";
            if (nomeOriginal != null && nomeOriginal.contains(".")) {
                extensao = nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
            }
            String nomeSalvar = UUID.randomUUID().toString() + extensao;
            java.nio.file.Path destino = rootDir.resolve(nomeSalvar);
            java.nio.file.Files.copy(arquivo.getInputStream(), destino, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return "uploads/" + nomeSalvar;
        } catch (Exception e) {
            throw new RuntimeException("Falha ao salvar arquivo", e);
        }
    }

    @Transactional
    public Doacao registrarDoacao(Doacao doacao, org.springframework.web.multipart.MultipartFile foto) {
        Doacao registrada = registrarDoacao(doacao);
        if (foto != null && !foto.isEmpty()) {
            String caminho = salvarArquivo(foto);
            if (caminho != null) {
                registrada.setFotoEvidencia(caminho);
                com.example.demo.models.Anexo anexo = new com.example.demo.models.Anexo(caminho, registrada);
                registrada.addAnexo(anexo);
            }
        }
        return doacaoRepository.save(registrada);
    }

    @Transactional
    public Doacao editarDoacao(Long id, Integer quantidade, String tipoItem, String matricula) {
        Doacao doacao = doacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doação não encontrada com o ID especificado."));

        if (doacao.getStatus() == StatusDoacao.APROVADA || doacao.getStatus() == StatusDoacao.REPROVADA) {
            throw new IllegalArgumentException("Não é possível editar uma doação que já foi processada.");
        }

        if (quantidade != null) {
            if (quantidade <= 0) {
                throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
            }
            doacao.setQuantidade(quantidade);
        }

        if (tipoItem != null) {
            if (tipoItem.trim().isEmpty()) {
                throw new IllegalArgumentException("O tipo de item não pode ser vazio.");
            }
            doacao.setTipoItem(tipoItem);
        }

        if (matricula != null) {
            if (matricula.trim().isEmpty()) {
                doacao.setAluno(null);
                doacao.setStatus(StatusDoacao.AGUARDANDO_CORRECAO);
            } else {
                Aluno aluno = alunoRepository.findByMatricula(matricula.trim())
                        .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado com a matrícula: " + matricula));
                doacao.setAluno(aluno);
                if (doacao.getStatus() == StatusDoacao.AGUARDANDO_CORRECAO) {
                    doacao.setStatus(StatusDoacao.PENDENTE);
                }
            }
        }

        return doacaoRepository.save(doacao);
    }
}

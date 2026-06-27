package com.example.demo.services;

import com.example.demo.models.Certificado;
import com.example.demo.models.Doacao;
import com.example.demo.repositories.CertificadoRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Serviço de Certificados — UC07 / RN06 / RF10
 * Responsável por gerar fisicamente o PDF do certificado com layout oficial FMP
 * e por fornecer os bytes para download via API REST.
 */
@Service
public class CertificadoService {

    /** Diretório base onde os PDFs serão salvos. Configurável via application.properties. */
    @Value("${certificados.diretorio:certificados}")
    private String diretorioCertificados;

    @Autowired
    private CertificadoRepository certificadoRepository;

    // ── Consultas ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Certificado> listarTodos() {
        return certificadoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Certificado> buscarPorId(Long id) {
        return certificadoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Certificado> buscarPorToken(String token) {
        return certificadoRepository.findByToken(token);
    }

    @Transactional(readOnly = true)
    public Optional<Certificado> buscarPorDoacaoId(Long doacaoId) {
        return certificadoRepository.findByDoacaoId(doacaoId);
    }

    // ── Geração de PDF ────────────────────────────────────────────────────────

    /**
     * Gera fisicamente o arquivo PDF do certificado em disco.
     * Deve ser chamado após o Certificado já ter sido salvo no banco.
     *
     * @param doacao      doação aprovada
     * @param certificado certificado já persistido com token e caminhoArquivo
     */
    public void gerarPDF(Doacao doacao, Certificado certificado) {
        try {
            // Garante que o diretório existe
            Path dir = Paths.get(diretorioCertificados);
            Files.createDirectories(dir);

            String nomeArquivo = "certificado_doacao_" + doacao.getId() + ".pdf";
            Path caminhoCompleto = dir.resolve(nomeArquivo);

            try (PDDocument doc = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.A4);
                doc.addPage(page);

                PDType1Font fonteTitulo  = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font fonteNormal  = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                PDType1Font fonteItalico = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
                PDType1Font fonteMono    = new PDType1Font(Standard14Fonts.FontName.COURIER);

                float largura  = page.getMediaBox().getWidth();   // ~595 pt
                float altura   = page.getMediaBox().getHeight();  // ~842 pt
                float margem   = 60f;

                // ─── Dados do aluno e doação ───────────────────────────────
                String nomeAluno    = doacao.getAluno() != null ? doacao.getAluno().getNome() : "—";
                String matricula    = doacao.getAluno() != null ? doacao.getAluno().getMatricula() : "—";
                String curso        = doacao.getAluno() != null && doacao.getAluno().getCurso() != null
                                        ? doacao.getAluno().getCurso() : "—";
                String campanha     = doacao.getCampanha() != null ? doacao.getCampanha().getTitulo() : "—";
                String tipoItem     = doacao.getTipoItem() != null ? doacao.getTipoItem() : "—";
                int quantidade      = doacao.getQuantidade() != null ? doacao.getQuantidade() : 0;
                double horas        = doacao.getHorasConcedidas() != null ? doacao.getHorasConcedidas() : 0;
                String token        = certificado.getToken();
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
                String dataEmissao  = certificado.getDataEmissao() != null
                                        ? certificado.getDataEmissao().format(fmt) : "—";
                String dataDoacao   = doacao.getData() != null
                                        ? doacao.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—";

                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                    // ── Barra superior (cabeçalho colorido) ─────────────────
                    cs.setNonStrokingColor(0.08f, 0.25f, 0.50f);  // azul FMP
                    cs.addRect(0, altura - 90, largura, 90);
                    cs.fill();

                    // ── Título na barra ──────────────────────────────────────
                    cs.beginText();
                    cs.setFont(fonteTitulo, 20);
                    cs.setNonStrokingColor(1f, 1f, 1f);  // branco
                    cs.newLineAtOffset(margem, altura - 42);
                    cs.showText("Faculdade Municipal de Palhoça — FMP");
                    cs.endText();

                    cs.beginText();
                    cs.setFont(fonteNormal, 13);
                    cs.setNonStrokingColor(0.75f, 0.88f, 1f);  // azul claro
                    cs.newLineAtOffset(margem, altura - 68);
                    cs.showText("Sistema de Gestão de Doações — Certificado Oficial");
                    cs.endText();

                    // ── Título do certificado ────────────────────────────────
                    cs.setNonStrokingColor(0.08f, 0.25f, 0.50f);
                    cs.beginText();
                    cs.setFont(fonteTitulo, 18);
                    cs.newLineAtOffset(margem, altura - 145);
                    cs.showText("CERTIFICADO DE PARTICIPAÇÃO EM DOAÇÃO");
                    cs.endText();

                    // ── Linha separadora ─────────────────────────────────────
                    cs.setStrokingColor(0.08f, 0.25f, 0.50f);
                    cs.setLineWidth(1.5f);
                    cs.moveTo(margem, altura - 158);
                    cs.lineTo(largura - margem, altura - 158);
                    cs.stroke();

                    // ── Texto de abertura ────────────────────────────────────
                    cs.setNonStrokingColor(0.15f, 0.15f, 0.15f);
                    cs.beginText();
                    cs.setFont(fonteNormal, 11);
                    cs.newLineAtOffset(margem, altura - 185);
                    cs.showText("A Faculdade Municipal de Palhoça certifica que o(a) aluno(a):");
                    cs.endText();

                    // ── Nome do aluno em destaque ────────────────────────────
                    cs.beginText();
                    cs.setFont(fonteTitulo, 16);
                    cs.setNonStrokingColor(0.05f, 0.18f, 0.40f);
                    cs.newLineAtOffset(margem, altura - 215);
                    cs.showText(nomeAluno);
                    cs.endText();

                    // ── Dados do aluno ───────────────────────────────────────
                    float yDados = altura - 245;
                    float espacoLinha = 20f;

                    cs.setNonStrokingColor(0.15f, 0.15f, 0.15f);
                    String[][] linhasDados = {
                        {"Matrícula:", matricula},
                        {"Curso:", curso},
                        {"Campanha:", campanha},
                        {"Item Doado:", tipoItem},
                        {"Quantidade:", String.valueOf(quantidade) + " unidade(s)"},
                        {"Data da Doação:", dataDoacao},
                        {"Horas Concedidas:", String.format("%.1f hora(s) complementar(es)", horas)}
                    };

                    for (String[] linha : linhasDados) {
                        cs.beginText();
                        cs.setFont(fonteTitulo, 11);
                        cs.newLineAtOffset(margem, yDados);
                        cs.showText(linha[0]);
                        cs.endText();

                        cs.beginText();
                        cs.setFont(fonteNormal, 11);
                        cs.newLineAtOffset(margem + 160, yDados);
                        cs.showText(linha[1]);
                        cs.endText();

                        yDados -= espacoLinha;
                    }

                    // ── Linha separadora inferior ────────────────────────────
                    float yToken = yDados - 20f;
                    cs.setStrokingColor(0.75f, 0.75f, 0.75f);
                    cs.setLineWidth(0.8f);
                    cs.moveTo(margem, yToken + 10);
                    cs.lineTo(largura - margem, yToken + 10);
                    cs.stroke();

                    // ── Token de autenticidade ───────────────────────────────
                    cs.setNonStrokingColor(0.3f, 0.3f, 0.3f);
                    cs.beginText();
                    cs.setFont(fonteItalico, 9);
                    cs.newLineAtOffset(margem, yToken - 10);
                    cs.showText("Token de Autenticidade (para verificação em: fmp.edu.br/verificar):");
                    cs.endText();

                    cs.beginText();
                    cs.setFont(fonteMono, 9);
                    cs.setNonStrokingColor(0.10f, 0.10f, 0.40f);
                    cs.newLineAtOffset(margem, yToken - 28);
                    cs.showText(token);
                    cs.endText();

                    // ── Data de emissão ──────────────────────────────────────
                    cs.setNonStrokingColor(0.3f, 0.3f, 0.3f);
                    cs.beginText();
                    cs.setFont(fonteNormal, 9);
                    cs.newLineAtOffset(margem, yToken - 50);
                    cs.showText("Emitido em: " + dataEmissao);
                    cs.endText();

                    // ── Rodapé ───────────────────────────────────────────────
                    cs.setNonStrokingColor(0.08f, 0.25f, 0.50f);
                    cs.addRect(0, 0, largura, 35);
                    cs.fill();

                    cs.beginText();
                    cs.setFont(fonteNormal, 8);
                    cs.setNonStrokingColor(1f, 1f, 1f);
                    cs.newLineAtOffset(margem, 12);
                    cs.showText("Documento gerado automaticamente pelo Sistema de Gestão de Doações FMP | Projeto Integrador II - 2026.1");
                    cs.endText();
                }

                doc.save(caminhoCompleto.toFile());
            }

            // Atualiza o caminho salvo no banco com o caminho absoluto
            certificado.setCaminhoArquivo(caminhoCompleto.toAbsolutePath().toString());
            certificadoRepository.save(certificado);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar o PDF do certificado da doação " + doacao.getId() + ": " + e.getMessage(), e);
        }
    }

    // ── Download ──────────────────────────────────────────────────────────────

    /**
     * Lê o arquivo PDF do disco e retorna os bytes para download via HTTP.
     *
     * @param doacaoId ID da doação cujo certificado será baixado
     * @return bytes do PDF
     * @throws RuntimeException se o certificado não existir ou o arquivo não for encontrado
     */
    public byte[] gerarERetornarPDFBytes(Long doacaoId) {
        Certificado certificado = certificadoRepository.findByDoacaoId(doacaoId)
                .orElseThrow(() -> new RuntimeException(
                        "Certificado não encontrado para a doação " + doacaoId + ". " +
                        "Verifique se a doação foi aprovada."));

        String caminho = certificado.getCaminhoArquivo();
        if (caminho == null || caminho.isBlank()) {
            throw new RuntimeException("Caminho do arquivo de certificado não registrado no banco.");
        }

        Path arquivo = Paths.get(caminho);
        if (!Files.exists(arquivo)) {
            throw new RuntimeException("Arquivo PDF não encontrado em disco: " + caminho);
        }

        try {
            return Files.readAllBytes(arquivo);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler o arquivo PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Retorna o nome do arquivo para o header Content-Disposition.
     */
    public String getNomeArquivoDownload(Long doacaoId) {
        return certificadoRepository.findByDoacaoId(doacaoId)
                .map(c -> {
                    String caminho = c.getCaminhoArquivo();
                    if (caminho == null) return "certificado_" + doacaoId + ".pdf";
                    return new File(caminho).getName();
                })
                .orElse("certificado_" + doacaoId + ".pdf");
    }
}

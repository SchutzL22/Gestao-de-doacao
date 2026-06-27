package com.example.demo.services;

import com.example.demo.models.StatusDoacao;
import com.example.demo.repositories.DoacaoRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Serviço de Relatórios — UC06 / RF12
 * Responsável por gerar o PDF consolidado com o total de doações,
 * horas concedidas e agregação por campanha.
 */
@Service
public class RelatorioService {

    @Autowired
    private DoacaoRepository doacaoRepository;

    public byte[] gerarRelatorioPDF() {
        long totalDoacoes    = doacaoRepository.count();
        long totalAprovadas  = doacaoRepository.countByStatus(StatusDoacao.APROVADA);
        long totalReprovadas = doacaoRepository.countByStatus(StatusDoacao.REPROVADA);
        long totalPendentes  = doacaoRepository.countByStatus(StatusDoacao.PENDENTE);

        // Soma total de horas concedidas (apenas aprovadas)
        double totalHoras = doacaoRepository.findByStatus(StatusDoacao.APROVADA)
                .stream()
                .mapToDouble(d -> d.getHorasConcedidas() != null ? d.getHorasConcedidas() : 0)
                .sum();

        // Agrupamento por campanha
        List<Object[]> porCampanha = doacaoRepository.findRelatorioPorCampanha();

        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDType1Font fonteTitulo  = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fonteNormal  = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            float largura  = page.getMediaBox().getWidth();
            float altura   = page.getMediaBox().getHeight();
            float margem   = 50f;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                // Cabeçalho azul
                cs.setNonStrokingColor(0.08f, 0.25f, 0.50f);
                cs.addRect(0, altura - 80, largura, 80);
                cs.fill();

                // Título principal
                cs.beginText();
                cs.setFont(fonteTitulo, 18);
                cs.setNonStrokingColor(1f, 1f, 1f);
                cs.newLineAtOffset(margem, altura - 45);
                cs.showText("FMP — Relatório Consolidado de Campanhas");
                cs.endText();

                cs.beginText();
                cs.setFont(fonteNormal, 11);
                cs.setNonStrokingColor(0.85f, 0.85f, 0.85f);
                cs.newLineAtOffset(margem, altura - 62);
                cs.showText("Dados agregados de doações e horas complementares");
                cs.endText();

                // Título de Métricas
                cs.setNonStrokingColor(0.15f, 0.15f, 0.15f);
                cs.beginText();
                cs.setFont(fonteTitulo, 14);
                cs.newLineAtOffset(margem, altura - 120);
                cs.showText("Métricas Gerais");
                cs.endText();

                // Linha separadora
                cs.setStrokingColor(0.8f, 0.8f, 0.8f);
                cs.setLineWidth(1f);
                cs.moveTo(margem, altura - 130);
                cs.lineTo(largura - margem, altura - 130);
                cs.stroke();

                // Tabela de Métricas Gerais
                float y = altura - 155;
                
                String[][] metricas = {
                    {"Total de Doações Registradas:", String.valueOf(totalDoacoes)},
                    {"Doações Aprovadas:", String.valueOf(totalAprovadas)},
                    {"Doações Reprovadas:", String.valueOf(totalReprovadas)},
                    {"Doações Pendentes:", String.valueOf(totalPendentes)},
                    {"Horas Concedidas:", String.format("%.1f horas", totalHoras)}
                };

                for (String[] metrica : metricas) {
                    cs.beginText();
                    cs.setFont(fonteTitulo, 11);
                    cs.newLineAtOffset(margem, y);
                    cs.showText(metrica[0]);
                    cs.endText();

                    cs.beginText();
                    cs.setFont(fonteNormal, 11);
                    cs.newLineAtOffset(margem + 200, y);
                    cs.showText(metrica[1]);
                    cs.endText();

                    y -= 20;
                }

                // Título Campanhas
                y -= 20;
                cs.beginText();
                cs.setFont(fonteTitulo, 14);
                cs.newLineAtOffset(margem, y);
                cs.showText("Doações por Campanha");
                cs.endText();

                y -= 10;
                cs.moveTo(margem, y);
                cs.lineTo(largura - margem, y);
                cs.stroke();

                // Cabeçalhos da tabela por campanha
                y -= 25;
                cs.beginText();
                cs.setFont(fonteTitulo, 10);
                cs.newLineAtOffset(margem, y);
                cs.showText("Campanha");
                cs.endText();

                cs.beginText();
                cs.setFont(fonteTitulo, 10);
                cs.newLineAtOffset(largura - margem - 220, y);
                cs.showText("Total Doações");
                cs.endText();

                cs.beginText();
                cs.setFont(fonteTitulo, 10);
                cs.newLineAtOffset(largura - margem - 80, y);
                cs.showText("Horas Concedidas");
                cs.endText();

                y -= 5;
                cs.moveTo(margem, y);
                cs.lineTo(largura - margem, y);
                cs.stroke();

                // Listar campanhas
                cs.setFont(fonteNormal, 10);
                for (Object[] row : porCampanha) {
                    y -= 20;
                    if (y < 60) {
                        cs.beginText();
                        cs.newLineAtOffset(margem, y);
                        cs.showText("[...] Mais campanhas omitidas");
                        cs.endText();
                        break;
                    }

                    String titulo = row[0] != null ? row[0].toString() : "—";
                    if (titulo.length() > 40) {
                        titulo = titulo.substring(0, 37) + "...";
                    }
                    String doacoes = row[1] != null ? row[1].toString() : "0";
                    String horasCampanha = String.format("%.1f h", ((Number) row[2]).doubleValue());

                    cs.beginText();
                    cs.setFont(fonteNormal, 10);
                    cs.newLineAtOffset(margem, y);
                    cs.showText(titulo);
                    cs.endText();

                    cs.beginText();
                    cs.newLineAtOffset(largura - margem - 220, y);
                    cs.showText(doacoes);
                    cs.endText();

                    cs.beginText();
                    cs.newLineAtOffset(largura - margem - 80, y);
                    cs.showText(horasCampanha);
                    cs.endText();
                }

                // Rodapé com data de emissão
                cs.setNonStrokingColor(0.5f, 0.5f, 0.5f);
                cs.beginText();
                cs.setFont(fonteNormal, 8);
                cs.newLineAtOffset(margem, 45);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                cs.showText("Emitido em: " + LocalDateTime.now().format(formatter));
                cs.endText();

                cs.setNonStrokingColor(0.08f, 0.25f, 0.50f);
                cs.addRect(0, 0, largura, 25);
                cs.fill();

                cs.beginText();
                cs.setFont(fonteNormal, 8);
                cs.setNonStrokingColor(1f, 1f, 1f);
                cs.newLineAtOffset(margem, 8);
                cs.showText("Relatório Oficial de Controle Acadêmico FMP — Palhoça/SC");
                cs.endText();
            }

            doc.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar relatório em PDF: " + e.getMessage(), e);
        }
    }
}

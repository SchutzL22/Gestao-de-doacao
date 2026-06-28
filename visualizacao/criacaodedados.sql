USE db_gestaodedoacao;

-- ==========================================
-- 1. CAMPANHAS (Ativas, Encerradas e Futuras)
-- ==========================================
INSERT INTO campanhas (titulo, descricao, data_limite, status, regras_conversao, limite_horas) VALUES
('Campanha de Inverno 2026', 'Arrecadação de agasalhos e cobertores.', '2026-08-30', 'Ativa', '1 agasalho/cobertor = 5 horas', 40.00),
('SOS Enchentes RS', 'Alimentos não perecíveis e água potável.', '2026-07-15', 'Ativa', '1 kg/item = 2 horas', 30.00),
('Material Escolar Solidário', 'Kits para o início do ano letivo.', '2026-03-10', 'Encerrada', '1 kit completo = 10 horas', 20.00),
('Doação de Sangue FMP', 'Semana de doação de sangue no hemocentro parceiro.', '2026-11-20', 'Ativa', '1 doação = 20 horas', 40.00),
('Natal Solidário 2025', 'Arrecadação de brinquedos novos ou em bom estado.', '2025-12-20', 'Encerrada', '1 brinquedo = 3 horas', 15.00),
('Tech para Todos', 'Doação de lixo eletrônico e peças de computador.', '2026-09-30', 'Ativa', '1 equipamento = 5 horas', 25.00);


-- ==========================================
-- 2. ITENS PRIORITÁRIOS
-- ==========================================
INSERT INTO itens_prioritarios (campanha_id, nome, meta, urgencia) VALUES
(1, 'Cobertor Grosso', 200, 'Alta'),
(1, 'Casaco de Frio', 300, 'Média'),
(2, 'Arroz (Pacote 5kg)', 500, 'Alta'),
(2, 'Água Mineral (Fardo)', 400, 'Alta'),
(3, 'Caderno Universitário', 150, 'Média'),
(4, 'Bolsa de Sangue', 100, 'Alta'),
(5, 'Bola de Futebol', 50, 'Baixa'),
(6, 'Teclado/Mouse', 80, 'Baixa');


-- ==========================================
-- 3. DOAÇÕES
-- IDs: 2=Davi, 3=Eduardo, 4=Lucas, 5=Vitor | 1=Administrador
-- ==========================================
INSERT INTO doacoes (aluno_id, campanha_id, func_registro_id, func_validacao_id, data, quantidade, tipo_item, status, motivo_reprovacao, horas_concedidas, horario_doacao, retencao_anos, local_entrega) VALUES

-- Aluno DAVI (ID 2)
(2, 1, 1, 1, '2026-06-01', 2, 'Cobertor Grosso', 'Aprovada', NULL, 10.00, '08:30:00', 10, 'Recepção Principal FMP'),
(2, 2, NULL, NULL, '2026-06-25', 5, 'Arroz (Pacote 5kg)', 'Pendente', NULL, NULL, '10:15:00', 10, 'Recepção Principal FMP'),
(2, 3, 1, 1, '2026-02-15', 1, 'Caderno Universitário', 'Aprovada', NULL, 10.00, '14:20:00', 10, 'Sala da COPER'),
(2, 5, 1, 1, '2025-12-05', 1, 'Carrinho de Plástico', 'Reprovada', 'Brinquedo quebrado, não atende aos critérios.', 0.00, '09:45:00', 10, 'Sala da COPER'),

-- Aluno EDUARDO (ID 3)
(3, 4, 1, 1, '2026-06-20', 1, 'Bolsa de Sangue', 'Aprovada', NULL, 20.00, '11:00:00', 10, 'Hemocentro SC'),
(3, 2, 1, 1, '2026-06-22', 10, 'Água Mineral (Fardo)', 'Aprovada', NULL, 20.00, '16:40:00', 10, 'Recepção Principal FMP'),
(3, 6, NULL, NULL, '2026-06-26', 2, 'Teclado/Mouse', 'Pendente', NULL, NULL, '18:10:00', 10, 'Laboratório de TI'),
(3, 1, 1, 1, '2026-06-10', 3, 'Casaco de Frio', 'Aprovada', NULL, 15.00, '13:30:00', 10, 'Sala da COPER'),
(3, 1, NULL, NULL, '2026-06-27', 1, 'Meias', 'Pendente', NULL, NULL, '08:00:00', 10, 'Recepção Principal FMP'),

-- Aluno LUCAS SCHUTZ (ID 4)
(4, 2, 1, 1, '2026-06-12', 3, 'Feijão (Pacote 1kg)', 'Aprovada', NULL, 6.00, '09:15:00', 10, 'Recepção Principal FMP'),
(4, 1, 1, 1, '2026-06-18', 1, 'Camiseta Regata', 'Reprovada', 'A campanha de inverno aceita apenas agasalhos e cobertores.', 0.00, '10:05:00', 10, 'Sala da COPER'),
(4, 3, 1, 1, '2026-02-28', 2, 'Kit Lápis de Cor', 'Reprovada', 'Fora do prazo da campanha.', 0.00, '15:50:00', 10, 'Recepção Principal FMP'),
(4, 6, NULL, NULL, '2026-06-26', 1, 'Monitor Antigo', 'Pendente', NULL, NULL, '19:20:00', 10, 'Laboratório de TI'),
(4, 5, 1, 1, '2025-12-10', 5, 'Bola de Futebol', 'Aprovada', NULL, 15.00, '14:00:00', 10, 'Sala da COPER'),

-- Aluno VITOR (ID 5)
(5, 4, 1, 1, '2026-06-05', 1, 'Bolsa de Sangue', 'Aprovada', NULL, 20.00, '08:00:00', 10, 'Hemocentro SC'),
(5, 1, 1, 1, '2026-06-15', 5, 'Cobertor Grosso', 'Aprovada', NULL, 25.00, '17:30:00', 10, 'Recepção Principal FMP'),
(5, 2, NULL, NULL, '2026-06-26', 20, 'Macarrão', 'Pendente', NULL, NULL, '12:15:00', 10, 'Sala da COPER'),
(5, 6, NULL, NULL, '2026-06-27', 3, 'Placa Mãe Queimada', 'Pendente', NULL, NULL, '20:10:00', 10, 'Laboratório de TI'),
(5, 5, 1, 1, '2025-11-25', 1, 'Urso de Pelúcia', 'Reprovada', 'Item com rasgos, impróprio para doação.', 0.00, '16:20:00', 10, 'Recepção Principal FMP');


-- ==========================================
-- 4. ANEXOS (Simulando comprovantes)
-- ==========================================
INSERT INTO anexos (doacao_id, nome_arquivo, caminho) VALUES
(1, 'foto_cobertores.jpg', '/uploads/foto_cobertores_1.jpg'),
(2, 'recibo_arroz.pdf', '/uploads/recibo_arroz_2.pdf'),
(3, 'foto_caderno.jpg', '/uploads/foto_caderno_3.jpg'),
(4, 'foto_carrinho.jpg', '/uploads/foto_carrinho_4.jpg'),
(5, 'comprovante_sangue_eduardo.pdf', '/uploads/comprovante_sangue_5.pdf'),
(6, 'nota_fiscal_agua.pdf', '/uploads/nota_fiscal_agua_6.pdf'),
(7, 'foto_mouse.jpg', '/uploads/foto_mouse_7.jpg'),
(8, 'foto_casaco.jpg', '/uploads/foto_casaco_8.jpg'),
(9, 'foto_meias.jpg', '/uploads/foto_meias_9.jpg'),
(10, 'cupom_fiscal_feijao.pdf', '/uploads/cupom_fiscal_10.pdf'),
(11, 'foto_camiseta.jpg', '/uploads/foto_camiseta_11.jpg'),
(12, 'foto_lapis.jpg', '/uploads/foto_lapis_12.jpg'),
(13, 'foto_monitor.jpg', '/uploads/foto_monitor_13.jpg'),
(14, 'foto_bolas_natal.jpg', '/uploads/foto_bolas_14.jpg'),
(15, 'comprovante_sangue_vitor.pdf', '/uploads/comprovante_sangue_15.pdf'),
(16, 'foto_cobertores_vitor.jpg', '/uploads/foto_cobertores_16.jpg'),
(17, 'nfe_macarrao.pdf', '/uploads/nfe_macarrao_17.pdf'),
(18, 'foto_placa_mae.jpg', '/uploads/foto_placa_mae_18.jpg'),
(19, 'foto_urso.jpg', '/uploads/foto_urso_19.jpg');


-- ==========================================
-- 5. CERTIFICADOS (Gerados apenas para as doações APROVADAS)
-- ==========================================
INSERT INTO certificados (doacao_id, token, caminho_arquivo, data_emissao) VALUES
(1, 'tok-davi-001', '/certificados/cert_001.pdf', '2026-06-02 10:00:00'),
(3, 'tok-davi-002', '/certificados/cert_003.pdf', '2026-02-16 11:00:00'),
(5, 'tok-edu-001', '/certificados/cert_005.pdf', '2026-06-21 09:30:00'),
(6, 'tok-edu-002', '/certificados/cert_006.pdf', '2026-06-23 14:00:00'),
(8, 'tok-edu-003', '/certificados/cert_008.pdf', '2026-06-11 16:20:00'),
(10, 'tok-luc-001', '/certificados/cert_010.pdf', '2026-06-13 08:45:00'),
(14, 'tok-luc-002', '/certificados/cert_014.pdf', '2025-12-11 10:15:00'),
(15, 'tok-vit-001', '/certificados/cert_015.pdf', '2026-06-06 13:10:00'),
(16, 'tok-vit-002', '/certificados/cert_016.pdf', '2026-06-16 09:00:00');


-- ==========================================
-- 6. NOTIFICAÇÕES (Histórico de alertas para o aluno)
-- ==========================================
INSERT INTO notificacoes (aluno_id, doacao_id, mensagem, data_envio, canal) VALUES
(2, 1, 'Sua doação foi aprovada! 10 horas complementares creditadas.', '2026-06-02 10:05:00', 'Sistema'),
(2, 2, 'Sua doação de Arroz está sob análise da COPER.', '2026-06-25 10:20:00', 'Sistema'),
(2, 4, 'Sua doação foi reprovada. Motivo: Brinquedo quebrado.', '2025-12-06 09:00:00', 'Email'),

(3, 5, 'Doação de Sangue validada com sucesso! 20 horas adicionadas.', '2026-06-21 09:35:00', 'Sistema'),
(3, 6, 'Sua doação de Água Mineral foi aprovada.', '2026-06-23 14:05:00', 'Sistema'),
(3, 7, 'Sua doação de Teclado/Mouse foi registrada e aguarda análise.', '2026-06-26 18:15:00', 'Sistema'),

(4, 10, 'Sua doação de Feijão foi aprovada. Verifique seu certificado.', '2026-06-13 08:50:00', 'Sistema'),
(4, 11, 'Doação reprovada. Motivo: A campanha de inverno aceita apenas agasalhos.', '2026-06-19 10:00:00', 'Sistema'),
(4, 13, 'Sua doação de Lixo Eletrônico está pendente de validação.', '2026-06-26 19:25:00', 'Sistema'),

(5, 15, 'Sua doação de Sangue foi aprovada! Excelente iniciativa.', '2026-06-06 13:15:00', 'Sistema'),
(5, 17, 'Recebemos seu registro de doação de Macarrão. Aguarde análise.', '2026-06-26 12:20:00', 'Sistema'),
(5, 19, 'Sua doação foi reprovada. O item encontra-se impróprio para uso.', '2025-11-26 16:30:00', 'Email');


-- ==========================================
-- 7. REGISTROS DE EDIÇÃO (Auditoria do Administrador)
-- ==========================================
INSERT INTO registros_edicao (doacao_id, funcionario_id, data, campo_alterado, valor_antigo, valor_novo) VALUES
(1, 1, '2026-06-02 09:55:00', 'status', 'Pendente', 'Aprovada'),
(3, 1, '2026-02-16 10:50:00', 'status', 'Pendente', 'Aprovada'),
(4, 1, '2025-12-06 08:50:00', 'status', 'Pendente', 'Reprovada'),
(5, 1, '2026-06-21 09:20:00', 'status', 'Pendente', 'Aprovada'),
(6, 1, '2026-06-23 13:50:00', 'status', 'Pendente', 'Aprovada'),
(8, 1, '2026-06-11 16:10:00', 'status', 'Pendente', 'Aprovada'),
(10, 1, '2026-06-13 08:40:00', 'status', 'Pendente', 'Aprovada'),
(11, 1, '2026-06-19 09:50:00', 'status', 'Pendente', 'Reprovada'),
(12, 1, '2026-03-01 10:00:00', 'status', 'Pendente', 'Reprovada'),
(14, 1, '2025-12-11 10:10:00', 'status', 'Pendente', 'Aprovada'),
(15, 1, '2026-06-06 13:00:00', 'status', 'Pendente', 'Aprovada'),
(16, 1, '2026-06-16 08:50:00', 'status', 'Pendente', 'Aprovada'),
(19, 1, '2025-11-26 16:20:00', 'status', 'Pendente', 'Reprovada');

-- ==========================================
-- 8. ATUALIZAR SALDO DOS ALUNOS
-- ==========================================
UPDATE alunos SET saldo_horas = 20.00 WHERE id = 2; -- Davi (10 + 10)
UPDATE alunos SET saldo_horas = 55.00 WHERE id = 3; -- Eduardo (20 + 20 + 15)
UPDATE alunos SET saldo_horas = 21.00 WHERE id = 4; -- Lucas (6 + 15)
UPDATE alunos SET saldo_horas = 45.00 WHERE id = 5; -- Vitor (20 + 25)
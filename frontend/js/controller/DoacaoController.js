/* =====================================================
   COPER FMP | js/controller/DoacaoController.js
   Orquestra todas as Views e o Model com chamadas assíncronas.
   Versão 2 — toast, localStorage, inputs por ID, notificações visuais.
   ===================================================== */

const DoacaoController = (() => {

    let sessaoAtual = null;

    // ─────────────────────────────────────────────────────────────────────────
    // TOAST — notificação visual no DOM (substitui alert())
    // ─────────────────────────────────────────────────────────────────────────
    function _toast(mensagem, tipo = 'success') {
        const container = document.getElementById('toastContainer');
        if (!container) { console.warn(mensagem); return; }

        const toast = document.createElement('div');
        toast.className = `toast toast-${tipo}`;

        const icone = tipo === 'success' ? 'fa-circle-check'
                    : tipo === 'error'   ? 'fa-circle-xmark'
                    : 'fa-circle-info';

        toast.innerHTML = `
            <i class="fa-solid ${icone}"></i>
            <span>${mensagem}</span>
            <button class="toast-close" aria-label="Fechar"><i class="fa-solid fa-xmark"></i></button>
        `;

        toast.querySelector('.toast-close').addEventListener('click', () => toast.remove());
        container.appendChild(toast);

        // Remove automaticamente após 4 segundos
        setTimeout(() => { if (toast.parentNode) toast.remove(); }, 4000);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INIT — inicializa bindings e restaura sessão do localStorage
    // ─────────────────────────────────────────────────────────────────────────
    function init() {
        // ── ROUTE GUARD ───────────────────────────────────────────────────────
        // Verifica sessão apenas se o appView já estiver visível (reload direto
        // na área autenticada). Se não houver sessão válida, o guard força o
        // loginView antes de qualquer binding de views protegidas.
        const appView = document.getElementById('appView');
        if (appView && !appView.classList.contains('hidden')) {
            if (!RouteGuard.verificar()) return;
        }

        LoginView.bindSubmit(_handleLogin);
        SidebarView.bindLogout(_handleLogout);
        if (TopbarView.bindLogout) {
            TopbarView.bindLogout(_handleLogout);
        }
        RegistrarDoacaoView.bindSubmit(_handleRegistroDoacao);
        CampanhasAdminView.bindNovaCampanha(_handleNovaCampanha);
        CampanhasAdminView.bindFormEdicao(_handleSalvarEdicaoCampanha);

        // Modal Editar Campanha: fechar ao clicar no X ou no overlay
        document.getElementById('btnFecharModalEditar')
                ?.addEventListener('click', _fecharModalEditar);
        document.getElementById('modalEditarCampanha')
                ?.addEventListener('click', (e) => { if (e.target.id === 'modalEditarCampanha') _fecharModalEditar(); });

        // Modal Editar Doação: fechar e salvar
        document.getElementById('btnFecharModalEditarDoacao')
                ?.addEventListener('click', _fecharModalEditarDoacao);
        document.getElementById('modalEditarDoacao')
                ?.addEventListener('click', (e) => { if (e.target.id === 'modalEditarDoacao') _fecharModalEditarDoacao(); });
        document.getElementById('formEditarDoacao')
                ?.addEventListener('submit', _handleSalvarEdicaoDoacao);

        // Tarefa 1: bind do formulário de editar perfil
        _bindEditarPerfil();

        // Relatório PDF download (RF12)
        document.getElementById('btnExportarRelatorioPDF')
                ?.addEventListener('click', _handleDownloadRelatorioPDF);

        // Itens Prioritários: bind do formulário CRUD (uma única vez)
        ItensPrioritariosAdminView.bindFormSubmit(_handleSalvarItem);
        ItensPrioritariosAdminView.bindCancelar(null);
        // Modal: fechar ao clicar no X
        document.getElementById('btnFecharModalItens')
                ?.addEventListener('click', _fecharModalItens);
        // Modal: fechar ao clicar fora (no overlay)
        document.getElementById('modalItensPrioritarios')
                ?.addEventListener('click', (e) => { if (e.target.id === 'modalItensPrioritarios') _fecharModalItens(); });
        // Cadastro de novo aluno (Criar Conta)
        LoginView.bindCadastro(_handleCadastroAluno);
        // Esqueci a Senha
        LoginView.bindEsqueciSenha(
            async (email) => {
                const conta = await UsuarioModel.buscarPorEmail(email);
                if (!conta) {
                    LoginView._exibirResultadoEsqueci &&
                        LoginView._exibirResultadoEsqueci('E-mail não encontrado. Verifique e tente novamente.', 'erro');
                    // a View já trata a ausência internamente
                }
                return conta;
            },
            async (id, novaSenha) => {
                try {
                    await UsuarioModel.alterarSenha(id, novaSenha);
                    _toast('✅ Senha redefinida com sucesso! Faça login com a nova senha.', 'success');
                    LoginView.mostrarLogin();
                } catch (err) {
                    _toast(err.message || 'Erro ao redefinir senha.', 'error');
                }
            }
        );

        // Restaura sessão salva no localStorage ao recarregar a página
        const sessaoSalva = UsuarioModel.getSessaoSalva();
        if (sessaoSalva) {
            sessaoAtual = sessaoSalva;
            _iniciarSessao(sessaoSalva).catch(err => {
                console.error('Falha ao restaurar sessão:', err);
                UsuarioModel.limparSessao();
                sessaoAtual = null;
            });
        }
    }


    // Tarefa 1: registra o listener do formulário de perfil uma única vez no init
    function _bindEditarPerfil() {
        const form = document.getElementById('formPerfil');
        if (!form) return;
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            await _handleEditarPerfil(e);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TAREFA 2: LOGIN — sem alert em caso de 200/OK
    // Separação clara entre erro de autenticação e erro de inicialização.
    // ─────────────────────────────────────────────────────────────────────────
    async function _handleLogin({ email, senha }) {
        // Desabilita o botão para evitar double-submit
        const btnSubmit = document.querySelector('#loginForm button[type="submit"]');
        if (btnSubmit) { btnSubmit.disabled = true; btnSubmit.textContent = 'Entrando...'; }

        let usuario;
        try {
            // Só lança erro se a resposta HTTP NÃO for 200/OK (ex: 401 credenciais inválidas)
            usuario = await UsuarioModel.autenticar(email, senha);
        } catch (error) {
            // Exibe erro APENAS se o login falhou — nunca em caso de sucesso
            LoginView.exibirErro(error.message || 'Credenciais inválidas. Verifique e tente novamente.');
            if (btnSubmit) { btnSubmit.disabled = false; btnSubmit.textContent = 'Entrar'; }
            return;
        }

        // Login HTTP 200/OK — salvo no localStorage (feito dentro de UsuarioModel.autenticar)
        sessaoAtual = usuario;
        if (btnSubmit) { btnSubmit.disabled = false; btnSubmit.textContent = 'Entrar'; }

        try {
            await _iniciarSessao(usuario);
        } catch (error) {
            // Erro ao carregar dados pós-login: NÃO exibe alerta na tela de login
            console.error('Erro ao inicializar dados após login:', error);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOGOUT — limpa localStorage e retorna ao login
    // ─────────────────────────────────────────────────────────────────────────
    function _handleLogout() {
        UsuarioModel.limparSessao();
        sessaoAtual = null;
        RouteGuard.redirecionarParaLogin('Logout realizado pelo utilizador.');
    }


    // ─────────────────────────────────────────────────────────────────────────
    // INICIALIZAÇÃO DA SESSÃO
    // ─────────────────────────────────────────────────────────────────────────
    async function _iniciarSessao(usuario) {
        // ── ROUTE GUARD: valida tipo do utilizador ────────────────────────────
        if (!RouteGuard.exigirTipo(usuario.tipo)) return;

        LoginView.mostrarApp();
        TopbarView.exibirUsuario(usuario.nome, usuario.role, usuario.tipo);
        SidebarView.configurarPorTipo(usuario.tipo);
        SidebarView.bindNavegacao(_handleNavegacao);

        try {
            if (usuario.tipo === 'admin') {
                // TAREFA 3: fetch paralelo de todas as APIs necessárias ao abrir o dashboard
                const [pendentes, totalHoras, doacoes, campanhas] = await Promise.all([
                    DoacaoModel.getTotalPendentes(),
                    DoacaoModel.getTotalHoras(),
                    DoacaoModel.getDoacoes(),           // GET /doacoes
                    CampanhaModel.listarTodas()         // GET /campanhas
                ]);

                DashboardAdminView.render(pendentes, totalHoras);
                _renderValidacao(doacoes);              // injeta tabela no DOM
                CampanhasAdminView.render(campanhas);   // injeta cards no DOM
                CampanhasAdminView.bindBotoesCampanha(campanhas, _handleEditarCampanhaClick, _handleEncerrarCampanhaClick);

                // Preenche o <select> de campanhas com dados reais do backend
                _preencherSelectCampanhas(campanhas);

            } else {
                // TAREFA 3: fetch paralelo para o dashboard do aluno com notificações
                const [doacoes, campanhas, notificacoes] = await Promise.all([
                    DoacaoModel.getDoacoes(usuario.id), // GET /doacoes/aluno/{id}
                    CampanhaModel.listarTodas(),        // GET /campanhas
                    NotificacaoModel.listarPorAluno(usuario.id).catch(() => [])
                ]);

                DashboardAlunoView.render(usuario.saldoHoras || 0, doacoes.length);
                HistoricoAlunoView.render(doacoes);
                _bindBotoesPDF();   // RF10: habilita download real do PDF
                CampanhasAlunoView.render(campanhas);   // injeta cards no DOM
                _bindBotoesVerItens(); // UC10: abre modal de itens prioritários
                TopbarView.configurarNotificacoes(notificacoes);
            }
        } catch (error) {
            console.error('Erro ao inicializar dados da sessão:', error);
            _toast(error.message || 'Erro ao carregar dados do servidor. Verifique se o backend está em execução.', 'error');
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // TAREFA 3: NAVEGAÇÃO — fetch e renderização ao trocar de seção
    // ─────────────────────────────────────────────────────────────────────────
    async function _handleNavegacao(targetId) {
        // Limpa a barra de pesquisa ao trocar de seção (RF13)
        const searchInput = document.getElementById('topbarSearchInput');
        if (searchInput) {
            searchInput.value = '';
            TopbarView.filtrarTelaAtual('');
        }

        try {
            if (targetId === 'validarDoacoes') {
                // TAREFA 3: GET /doacoes → injeta tabela no DOM
                const doacoes = await DoacaoModel.getDoacoes();
                _renderValidacao(doacoes);

            } else if (targetId === 'campanhasAdmin') {
                // TAREFA 3: GET /campanhas → injeta cards no DOM (admin)
                const campanhas = await CampanhaModel.listarTodas();
                CampanhasAdminView.render(campanhas);
                CampanhasAdminView.bindBotoesCampanha(campanhas, _handleEditarCampanhaClick, _handleEncerrarCampanhaClick);

            } else if (targetId === 'campanhasAluno') {
                // TAREFA 3: GET /campanhas → injeta cards no DOM (aluno)
                const campanhas = await CampanhaModel.listarTodas();
                CampanhasAlunoView.render(campanhas);
                _bindBotoesVerItens(); // abre modal de itens ao clicar

            } else if (targetId === 'historicoAluno') {
                const doacoes = await DoacaoModel.getDoacoes(sessaoAtual.id);
                HistoricoAlunoView.render(doacoes);
                _bindBotoesPDF();   // RF10: habilita download real do PDF

            } else if (targetId === 'itensPrioritariosAdmin') {
                await _carregarItensPrioritarios();

            } else if (targetId === 'relatoriosAdmin') {
                const dados = await RelatorioModel.buscar();
                RelatoriosAdminView.render(dados);

            } else if (targetId === 'dashboardAdmin') {
                const [pendentes, totalHoras] = await Promise.all([
                    DoacaoModel.getTotalPendentes(),
                    DoacaoModel.getTotalHoras()
                ]);
                DashboardAdminView.render(pendentes, totalHoras);

            } else if (targetId === 'dashboardAluno') {
                const doacoes = await DoacaoModel.getDoacoes(sessaoAtual.id);
                DashboardAlunoView.render(sessaoAtual.saldoHoras || 0, doacoes.length);

            } else if (targetId === 'editarPerfil') {
                // Tarefa 1: popula o formulário com os dados do usuário logado
                _popularFormPerfil(sessaoAtual);
            }

            // Atualiza notificações do aluno de forma assíncrona se for aluno
            if (sessaoAtual && sessaoAtual.tipo === 'aluno') {
                NotificacaoModel.listarPorAluno(sessaoAtual.id)
                    .then(notifs => TopbarView.configurarNotificacoes(notifs))
                    .catch(err => console.error("Erro ao recarregar notificações:", err));
            }
        } catch (error) {
            console.error('Erro ao carregar dados na navegação:', error);
            _toast(error.message || 'Falha ao carregar dados. Tente novamente.', 'error');
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS — validação e renderização
    // ─────────────────────────────────────────────────────────────────────────

    /** Preenche o <select> de campanhas com dados reais do backend. */
    function _preencherSelectCampanhas(campanhas) {
        const select = document.getElementById('campanhaSelect');
        if (!select) return;
        if (!campanhas || campanhas.length === 0) {
            select.innerHTML = '<option value="">Nenhuma campanha ativa</option>';
            return;
        }
        select.innerHTML = campanhas
            .map(c => `<option value="${c.id}">${c.titulo}</option>`)
            .join('');
    }

    /** Renderiza a tabela de validação e ativa os botões de aprovar/reprovar. */
    function _renderValidacao(doacoes) {
        ValidarDoacoesView.render(doacoes);
        _bindValidacaoBotoes();
    }

    /** Registra os event listeners nos botões da tabela de validação. */
    function _bindValidacaoBotoes() {
        const tbody = document.getElementById('tableValidacaoBody');
        if (!tbody) return;

        tbody.querySelectorAll('button.btn-icon.check').forEach(btn => {
            btn.addEventListener('click', async () => {
                const id = btn.getAttribute('data-id');
                if (!id) return;
                btn.disabled = true;
                try {
                    await DoacaoModel.validarDoacao(id, 'APROVADA', '');
                    _toast('Doação aprovada com sucesso!', 'success');
                    const [doacoes, p, h] = await Promise.all([
                        DoacaoModel.getDoacoes(),
                        DoacaoModel.getTotalPendentes(),
                        DoacaoModel.getTotalHoras()
                    ]);
                    _renderValidacao(doacoes);
                    DashboardAdminView.render(p, h);
                } catch (err) {
                    _toast(err.message || 'Erro ao aprovar doação.', 'error');
                    btn.disabled = false;
                }
            });
        });

        tbody.querySelectorAll('button.btn-icon.times').forEach(btn => {
            btn.addEventListener('click', async () => {
                const id = btn.getAttribute('data-id');
                if (!id) return;
                const motivo = prompt('Motivo da reprovação (opcional):') || '';
                btn.disabled = true;
                try {
                    await DoacaoModel.validarDoacao(id, 'REPROVADA', motivo);
                    _toast('Doação reprovada.', 'info');
                    const [doacoes, p, h] = await Promise.all([
                        DoacaoModel.getDoacoes(),
                        DoacaoModel.getTotalPendentes(),
                        DoacaoModel.getTotalHoras()
                    ]);
                    _renderValidacao(doacoes);
                    DashboardAdminView.render(p, h);
                } catch (err) {
                    _toast(err.message || 'Erro ao reprovar doação.', 'error');
                    btn.disabled = false;
                }
            });
        });

        ValidarDoacoesView.bindEditar(_handleEditarDoacaoClick);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TAREFA 4: SUBMISSÃO DE FORMULÁRIOS — preventDefault, POST, toast de sucesso
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Registrar Doação — captura inputs por ID, envia POST /doacoes,
     * exibe toast de sucesso e atualiza as views sem alert().
     */
    async function _handleRegistroDoacao(e) {
        // preventDefault já é chamado pelo RegistrarDoacaoView.bindSubmit
        const btnSubmit = e.target.querySelector('button[type="submit"]');
        if (btnSubmit) { btnSubmit.disabled = true; btnSubmit.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Salvando...'; }

        try {
            // Captura inputs por ID (robusto, sem depender de placeholder)
            const matricula  = document.getElementById('matriculaInput').value.trim();
            const campanhaId = document.getElementById('campanhaSelect').value;
            const tipoItem   = document.getElementById('tipoItemInput').value.trim();
            const quantidade = document.getElementById('quantidadeInput').value;
            const localEntrega = document.getElementById('localEntregaInput').value.trim();
            const evidenciaInput = document.getElementById('evidenciaInput');
            const foto = evidenciaInput && evidenciaInput.files ? evidenciaInput.files[0] : null;

            // Matrícula é opcional
            if (!campanhaId || !tipoItem || !quantidade || !localEntrega) {
                _toast('Preencha todos os campos obrigatórios (incluindo Local de Entrega).', 'error');
                if (btnSubmit) { btnSubmit.disabled = false; btnSubmit.innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Salvar Registro'; }
                return;
            }

            // Envia POST /doacoes via DoacaoModel
            await DoacaoModel.registrarDoacao(matricula, campanhaId, tipoItem, quantidade, localEntrega, foto);

            // Tarefa 4: notificação visual de sucesso (sem alert bloqueante)
            _toast('✅ Doação registrada com sucesso e enviada para validação!', 'success');
            e.target.reset();

            // Atualiza dashboard e tabela de validação em paralelo
            const [pendentes, totalHoras, doacoes] = await Promise.all([
                DoacaoModel.getTotalPendentes(),
                DoacaoModel.getTotalHoras(),
                DoacaoModel.getDoacoes()
            ]);
            DashboardAdminView.render(pendentes, totalHoras);
            _renderValidacao(doacoes);

            // Redireciona para a aba de validação
            const itemMenuValidar = document.querySelector('#menuAdmin li[data-target="validarDoacoes"]');
            if (itemMenuValidar) itemMenuValidar.click();

        } catch (error) {
            console.error('Erro ao registrar doação:', error);
            _toast(error.message || 'Erro ao registrar doação. Verifique a matrícula.', 'error');
        } finally {
            if (btnSubmit) { btnSubmit.disabled = false; btnSubmit.innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Salvar Registro'; }
        }
    }

    /**
     * Nova Campanha — captura inputs por ID, envia POST /campanhas,
     * exibe toast de sucesso e atualiza a lista sem alert().
     */
    async function _handleNovaCampanha(dados) {
        const btnSubmit = document.querySelector('#formNovaCampanha button[type="submit"]');
        if (btnSubmit) { btnSubmit.disabled = true; btnSubmit.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Criando...'; }

        try {
            // Envia POST /campanhas com os dados capturados pela CampanhasAdminView
            await CampanhaModel.salvar(dados);

            // Tarefa 4: notificação visual de sucesso
            _toast('🎯 Campanha criada com sucesso!', 'success');
            document.getElementById('formNovaCampanha').reset();

            // Atualiza a lista de campanhas e o select do formulário de registro
            const campanhas = await CampanhaModel.listarTodas();
            CampanhasAdminView.render(campanhas);
            CampanhasAdminView.bindBotoesCampanha(campanhas, _handleEditarCampanhaClick, _handleEncerrarCampanhaClick);
            _preencherSelectCampanhas(campanhas);

        } catch (error) {
            console.error('Erro ao criar campanha:', error);
            _toast(error.message || 'Erro ao criar campanha.', 'error');
        } finally {
            if (btnSubmit) { btnSubmit.disabled = false; btnSubmit.innerHTML = '<i class="fa-solid fa-plus"></i> Criar Campanha'; }
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // TAREFA 1: EDITAR PERFIL
    // ──────────────────────────────────────────────────────────────────

    /** Popula o formulário de perfil com os dados do usuário logado. */
    function _popularFormPerfil(usuario) {
        const nomeFld   = document.getElementById('perfilNome');
        const emailFld  = document.getElementById('perfilEmail');
        const senhaFld  = document.getElementById('perfilSenha');
        const idFld     = document.getElementById('perfilIdentificador');
        const lblId     = document.getElementById('labelIdentificador');

        if (nomeFld)  nomeFld.value  = usuario.nome  || '';
        if (emailFld) emailFld.value = usuario.email || '';
        if (senhaFld) senhaFld.value = '';   // nunca pré-preenche a senha

        // Exibe matrícula (aluno) ou CPF (admin) como identificador somente leitura
        if (usuario.tipo === 'aluno') {
            if (lblId)  lblId.textContent  = 'Matrícula';
            if (idFld)  idFld.value        = usuario.matricula || '';
        } else {
            if (lblId)  lblId.textContent  = 'CPF';
            if (idFld)  idFld.value        = usuario.cpf       || '';
        }
    }

    /**
     * Submete o formulário de editar perfil.
     * Envia PUT /usuarios/{id} via UsuarioModel.atualizarPerfil().
     * Atualiza o localStorage e a topbar em caso de sucesso.
     */
    async function _handleEditarPerfil(e) {
        const btnSubmit = e.target.querySelector('button[type="submit"]');
        if (btnSubmit) { btnSubmit.disabled = true; btnSubmit.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Salvando...'; }

        try {
            if (!sessaoAtual || !sessaoAtual.id) {
                throw new Error('Sessão inválida. Faça login novamente.');
            }

            const nome      = document.getElementById('perfilNome').value.trim();
            const novaSenha = document.getElementById('perfilSenha').value;

            if (!nome) {
                throw new Error('O nome não pode ficar em branco.');
            }

            // Chama PUT /usuarios/{id} — senha atualizada somente se preenchida
            const usuarioAtualizado = await UsuarioModel.atualizarPerfil(
                sessaoAtual.id, nome, novaSenha
            );

            // Atualiza a sessão na memória e no localStorage
            sessaoAtual = usuarioAtualizado;
            localStorage.setItem('coper_sessao', JSON.stringify(sessaoAtual));

            // Atualiza a topbar com o novo nome
            TopbarView.exibirUsuario(sessaoAtual.nome, sessaoAtual.role);

            // Atualiza o formulário com a nova sessão limpa
            _popularFormPerfil(sessaoAtual);

            _toast('Perfil atualizado com sucesso!', 'success');

        } catch (error) {
            console.error('Erro ao atualizar perfil:', error);
            _toast(error.message || 'Erro ao salvar alterações.', 'error');
        } finally {
            if (btnSubmit) { btnSubmit.disabled = false; btnSubmit.innerHTML = 'Salvar Alterações'; }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RF10: DOWNLOAD DE PDF — botão "PDF" no histórico do aluno
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Registra o evento de clique em cada botão .btn-icon.pdf da tabela de histórico.
     * Faz fetch GET /api/certificados/download/{doacaoId}, converte em Blob
     * e cria um <a> temporário para forçar o download real do arquivo PDF.
     */
    function _bindBotoesPDF() {
        const tbody = document.getElementById('tableHistoricoBody');
        if (!tbody) return;

        tbody.querySelectorAll('button.btn-icon.pdf').forEach(btn => {
            // Remove listener anterior para evitar duplicação ao re-renderizar
            btn.replaceWith(btn.cloneNode(true));
        });

        // Re-seleciona após a substituição
        tbody.querySelectorAll('button.btn-icon.pdf').forEach(btn => {
            btn.addEventListener('click', async () => {
                const doacaoId = btn.getAttribute('data-doacao-id');
                const campanha = btn.getAttribute('data-campanha') || 'certificado';

                if (!doacaoId) {
                    _toast('ID da doação não encontrado no botão.', 'error');
                    return;
                }

                // Feedback visual enquanto baixa
                const iconOriginal = btn.innerHTML;
                btn.disabled = true;
                btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i>';

                try {
                    const { API_BASE } = window.Config || {};
                    const url = `${API_BASE || ''}/api/certificados/download/${doacaoId}`;

                    const response = await fetch(url, {
                        method: 'GET',
                        headers: { 'Accept': 'application/pdf' }
                    });

                    if (!response.ok) {
                        if (response.status === 404) {
                            throw new Error('Certificado PDF não encontrado. Tente novamente mais tarde.');
                        }
                        throw new Error(`Erro ao baixar PDF (HTTP ${response.status}).`);
                    }

                    // Converte a resposta em Blob e cria URL temporária
                    const blob = await response.blob();
                    const blobUrl = URL.createObjectURL(blob);

                    // Link temporário para forçar download no navegador
                    const link = document.createElement('a');
                    link.href = blobUrl;
                    link.download = `certificado_doacao_${doacaoId}.pdf`;
                    document.body.appendChild(link);
                    link.click();
                    document.body.removeChild(link);

                    // Libera memória
                    setTimeout(() => URL.revokeObjectURL(blobUrl), 5000);

                    _toast(`✅ PDF do certificado "${campanha}" baixado com sucesso!`, 'success');

                } catch (err) {
                    console.error('Erro ao baixar PDF:', err);
                    _toast(err.message || 'Falha ao baixar o certificado PDF.', 'error');
                } finally {
                    btn.disabled = false;
                    btn.innerHTML = iconOriginal;
                }
            });
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RF14 / UC14: CRUD DE ITENS PRIORITÁRIOS (Admin)
    // ─────────────────────────────────────────────────────────────────────────

    /** Busca todos os itens e re-renderiza a tabela. */
    async function _carregarItensPrioritarios() {
        const itens = await ItemPrioritarioModel.listarTodos();
        ItensPrioritariosAdminView.render(itens);
        _bindItensAdminBotoes(itens);
    }

    /** Registra editar/excluir na tabela (precisa dos dados para editar). */
    function _bindItensAdminBotoes(itens) {
        ItensPrioritariosAdminView.bindTabelaBotoes(
            // Editar: preenche o form com os dados do item clicado
            (id) => {
                const item = itens.find(i => i.id === id);
                if (item) ItensPrioritariosAdminView.preencherFormEdicao(item);
            },
            // Excluir
            async (id) => {
                if (!confirm('Excluir este item prioritário?')) return;
                try {
                    await ItemPrioritarioModel.deletar(id);
                    _toast('Item excluído com sucesso.', 'info');
                    await _carregarItensPrioritarios();
                } catch (err) {
                    _toast(err.message || 'Erro ao excluir item.', 'error');
                }
            }
        );
    }

    /**
     * Salva (cria ou atualiza) um item prioritário.
     * Chamado pelo ItensPrioritariosAdminView.bindFormSubmit.
     */
    async function _handleSalvarItem({ id, nome, meta, urgencia }) {
        const btnSubmit = document.querySelector('#formItemPrioritario button[type="submit"]');
        if (btnSubmit) { btnSubmit.disabled = true; btnSubmit.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Salvando...'; }

        try {
            if (!nome) throw new Error('O nome do item é obrigatório.');

            if (id) {
                await ItemPrioritarioModel.atualizar(id, { nome, meta, urgencia });
                _toast('Item atualizado com sucesso!', 'success');
            } else {
                await ItemPrioritarioModel.criar({ nome, meta, urgencia });
                _toast('Item criado com sucesso!', 'success');
            }

            ItensPrioritariosAdminView.limparForm();
            await _carregarItensPrioritarios();

        } catch (err) {
            _toast(err.message || 'Erro ao salvar item.', 'error');
        } finally {
            if (btnSubmit) { btnSubmit.disabled = false; btnSubmit.innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Salvar Item'; }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UC10: MODAL "VER ITENS PRIORITÁRIOS" (Aluno)
    // ─────────────────────────────────────────────────────────────────────────

    /** Registra o clique em todos os botões "Ver Itens Prioritários" dos cards de campanha. */
    function _bindBotoesVerItens() {
        document.querySelectorAll('.btn-ver-itens').forEach(btn => {
            btn.addEventListener('click', async () => {
                const campanhaId   = btn.getAttribute('data-campanha-id');
                const campanhaNome = btn.getAttribute('data-campanha-nome') || 'Campanha';
                await _abrirModalItens(campanhaId, campanhaNome);
            });
        });
    }

    /** Abre o modal e lista os itens prioritários. */
    async function _abrirModalItens(campanhaId, campanhaNome) {
        const modal      = document.getElementById('modalItensPrioritarios');
        const nomeEl     = document.getElementById('modalItensCampanhaNome');
        const listaEl    = document.getElementById('modalItensLista');
        if (!modal || !listaEl) return;

        if (nomeEl) nomeEl.textContent = `Campanha: ${campanhaNome}`;
        listaEl.innerHTML = '<p style="text-align:center;color:var(--text-muted);"><i class="fa-solid fa-spinner fa-spin"></i> Carregando...</p>';
        modal.classList.remove('hidden');
        document.body.style.overflow = 'hidden';

        try {
            const itens = await ItemPrioritarioModel.listarTodos();

            if (!itens.length) {
                listaEl.innerHTML = '<p style="text-align:center;color:var(--text-muted);padding:1rem;">Nenhum item prioritário cadastrado ainda.</p>';
                return;
            }

            listaEl.innerHTML = `
                <ul class="modal-itens-lista">
                    ${itens.map(item => {
                        const urgClass = item.urgencia === 'ALTA'  ? 'badge-urgente'
                                       : item.urgencia === 'MEDIA' ? 'badge-pendente'
                                       : 'badge-aprovado';
                        const urgLabel = item.urgencia === 'ALTA'  ? 'Alta'
                                       : item.urgencia === 'MEDIA' ? 'Média' : 'Normal';
                        return `
                            <li class="modal-item-row">
                                <span class="modal-item-nome"><i class="fa-solid fa-box"></i> ${item.nome}</span>
                                <span class="badge ${urgClass}">${urgLabel}</span>
                                ${item.meta != null ? `<span class="text-muted">Meta: ${item.meta} un.</span>` : ''}
                            </li>`;
                    }).join('')}
                </ul>
            `;
        } catch (err) {
            listaEl.innerHTML = `<p style="color:var(--danger);text-align:center;">⚠️ ${err.message}</p>`;
        }
    }

    /** Fecha o modal de itens prioritários. */
    function _fecharModalItens() {
        const modal = document.getElementById('modalItensPrioritarios');
        if (modal) modal.classList.add('hidden');
        document.body.style.overflow = '';
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RF01 — CRIAR CONTA (cadastro de novo aluno)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Cadastra um novo aluno via POST /api/usuarios/alunos.
     * Valida e-mail institucional, senhas coincidentes e campos obrigatórios.
     * Após sucesso, loga automaticamente o usuário cadastrado.
     */
    async function _handleCadastroAluno({ nome, cpf, email, matricula, curso, senha, confirmar }) {
        const btn = document.getElementById('btnCadastrar');
        if (btn) { btn.disabled = true; btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Cadastrando...'; }

        try {
            // Validações no frontend
            if (!nome || !cpf || !email || !matricula || !curso || !senha) {
                throw new Error('Preencha todos os campos obrigatórios.');
            }
            if (!email.toLowerCase().endsWith('@aluno.fmpsc.edu.br')) {
                throw new Error('Use somente o e-mail institucional (@aluno.fmpsc.edu.br).');
            }
            if (senha.length < 6) {
                throw new Error('A senha deve ter no mínimo 6 caracteres.');
            }
            if (senha !== confirmar) {
                throw new Error('As senhas não coincidem.');
            }
            if (cpf.replace(/\D/g, '').length !== 11) {
                throw new Error('CPF inválido — informe 11 dígitos.');
            }

            // POST /api/usuarios/alunos
            const novoAluno = await UsuarioModel.cadastrarAluno({
                nome, cpf: cpf.replace(/\D/g, ''), email, matricula, curso, senha,
                saldoHoras: 0
            });

            LoginView.feedbackCadastro('✅ Conta criada! Entrando automaticamente...', 'ok');

            // Login automático
            await new Promise(r => setTimeout(r, 1200));
            sessaoAtual = await UsuarioModel.autenticar(email, senha);
            await _iniciarSessao(sessaoAtual);

        } catch (err) {
            console.error('Erro ao cadastrar aluno:', err);
            LoginView.feedbackCadastro('⚠️ ' + (err.message || 'Erro ao cadastrar. Tente novamente.'), 'erro');
        } finally {
            if (btn) { btn.disabled = false; btn.innerHTML = '<i class="fa-solid fa-user-check"></i> Cadastrar'; }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UC13: CONTROLE DE MODAL E ALTERAÇÃO DE CAMPANHAS
    // ─────────────────────────────────────────────────────────────────────────

    function _fecharModalEditar() {
        const modal = document.getElementById('modalEditarCampanha');
        if (modal) modal.classList.add('hidden');
        document.body.style.overflow = '';
    }

    function _handleEditarCampanhaClick(campanha) {
        const modal = document.getElementById('modalEditarCampanha');
        if (!modal) return;
        CampanhasAdminView.preencherModalEdicao(campanha);
        modal.classList.remove('hidden');
        document.body.style.overflow = 'hidden';
    }

    async function _handleEncerrarCampanhaClick(id) {
        if (!confirm('Deseja realmente encerrar esta campanha? (Novas doações não poderão ser registradas para ela)')) return;
        try {
            await CampanhaModel.encerrar(id);
            _toast('Campanha encerrada com sucesso!', 'success');

            const campanhas = await CampanhaModel.listarTodas();
            CampanhasAdminView.render(campanhas);
            CampanhasAdminView.bindBotoesCampanha(campanhas, _handleEditarCampanhaClick, _handleEncerrarCampanhaClick);
            _preencherSelectCampanhas(campanhas);
        } catch (error) {
            console.error('Erro ao encerrar campanha:', error);
            _toast(error.message || 'Erro ao encerrar campanha.', 'error');
        }
    }

    async function _handleSalvarEdicaoCampanha(dados) {
        const btnSubmit = document.querySelector('#formEditarCampanha button[type="submit"]');
        if (btnSubmit) {
            btnSubmit.disabled = true;
            btnSubmit.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Salvando...';
        }

        try {
            if (!dados.titulo) {
                throw new Error('O título da campanha é obrigatório.');
            }

            await CampanhaModel.atualizar(dados.id, dados);
            _toast('Campanha atualizada com sucesso!', 'success');
            _fecharModalEditar();

            const campanhas = await CampanhaModel.listarTodas();
            CampanhasAdminView.render(campanhas);
            CampanhasAdminView.bindBotoesCampanha(campanhas, _handleEditarCampanhaClick, _handleEncerrarCampanhaClick);
            _preencherSelectCampanhas(campanhas);
        } catch (error) {
            console.error('Erro ao editar campanha:', error);
            _toast(error.message || 'Erro ao editar campanha.', 'error');
        } finally {
            if (btnSubmit) {
                btnSubmit.disabled = false;
                btnSubmit.innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Salvar Alterações';
            }
        }
    }

    // Modal Editar Doação Helpers
    function _fecharModalEditarDoacao() {
        const modal = document.getElementById('modalEditarDoacao');
        if (modal) modal.classList.add('hidden');
        document.body.style.overflow = '';
    }

    async function _handleEditarDoacaoClick(id) {
        try {
            const doacao = await DoacaoModel.buscarPorId(id);
            if (!doacao) return;

            document.getElementById('editarDoacaoId').value = doacao.id;
            document.getElementById('editarDoacaoMatricula').value = doacao.matricula || '';
            document.getElementById('editarDoacaoTipoItem').value = doacao.tipoItem || '';
            document.getElementById('editarDoacaoQuantidade').value = doacao.quantidade || '';

            const modal = document.getElementById('modalEditarDoacao');
            if (modal) {
                modal.classList.remove('hidden');
                document.body.style.overflow = 'hidden';
            }
        } catch (err) {
            _toast(err.message || 'Erro ao carregar doação para edição.', 'error');
        }
    }

    async function _handleSalvarEdicaoDoacao(e) {
        e.preventDefault();
        const btnSubmit = e.target.querySelector('button[type="submit"]');
        if (btnSubmit) { btnSubmit.disabled = true; btnSubmit.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Salvando...'; }

        try {
            const id = document.getElementById('editarDoacaoId').value;
            const matricula = document.getElementById('editarDoacaoMatricula').value.trim();
            const tipoItem = document.getElementById('editarDoacaoTipoItem').value.trim();
            const quantidade = parseInt(document.getElementById('editarDoacaoQuantidade').value, 10);

            if (!tipoItem || isNaN(quantidade) || quantidade <= 0) {
                throw new Error('Tipo de Item e Quantidade válidos são obrigatórios.');
            }

            await DoacaoModel.editarDoacao(id, { quantidade, tipoItem, matricula });
            _toast('✅ Doação alterada com sucesso!', 'success');
            _fecharModalEditarDoacao();

            const [doacoes, p, h] = await Promise.all([
                DoacaoModel.getDoacoes(),
                DoacaoModel.getTotalPendentes(),
                DoacaoModel.getTotalHoras()
            ]);
            _renderValidacao(doacoes);
            DashboardAdminView.render(p, h);

        } catch (err) {
            console.error('Erro ao salvar edição da doação:', err);
            _toast(err.message || 'Erro ao salvar alterações da doação.', 'error');
        } finally {
            if (btnSubmit) { btnSubmit.disabled = false; btnSubmit.innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Salvar Alterações'; }
        }
    }

    async function _handleDownloadRelatorioPDF() {
        const btn = document.getElementById('btnExportarRelatorioPDF');
        if (!btn) return;

        const originalHtml = btn.innerHTML;
        btn.disabled = true;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Exportando...';

        try {
            const response = await fetch(`${API_BASE_URL}/relatorios/download`, {
                method: 'GET',
                headers: { 'Accept': 'application/pdf' }
            });

            if (!response.ok) {
                throw new Error(`Erro ao gerar PDF do relatório (HTTP ${response.status}).`);
            }

            const blob = await response.blob();
            const blobUrl = URL.createObjectURL(blob);

            const link = document.createElement('a');
            link.href = blobUrl;
            link.download = `relatorio_consolidado_${new Date().toISOString().slice(0, 10)}.pdf`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);

            setTimeout(() => URL.revokeObjectURL(blobUrl), 5000);
            _toast('✅ Relatório em PDF exportado com sucesso!', 'success');
        } catch (err) {
            console.error('Erro ao baixar relatório PDF:', err);
            _toast(err.message || 'Falha ao baixar o relatório em PDF.', 'error');
        } finally {
            btn.disabled = false;
            btn.innerHTML = originalHtml;
        }
    }

    return { init };
})();

document.addEventListener('DOMContentLoaded', () => DoacaoController.init());

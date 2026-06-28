/* js/view/shared/LoginView.js
   Gerencia a tela de login, cadastro de aluno e esqueci-a-senha. */

const LoginView = (() => {

    const loginView  = document.getElementById('loginView');
    const appView    = document.getElementById('appView');
    const loginForm  = document.getElementById('loginForm');

    // ── Painéis ────────────────────────────────────────────────────────────
    const painelLogin        = document.getElementById('painelLogin');
    const painelCadastro     = document.getElementById('painelCadastro');
    const painelEsqueciSenha = document.getElementById('painelEsqueciSenha');

    // ── Mensagem de erro inline no formulário de login ──────────────────────
    const errMsg = document.createElement('p');
    errMsg.id = 'loginErro';
    errMsg.style.cssText = 'color:#DC2626;font-size:0.83rem;margin-top:0.5rem;display:none;';
    loginForm.querySelector('button[type="submit"]').insertAdjacentElement('afterend', errMsg);
    loginForm.addEventListener('input', () => { errMsg.style.display = 'none'; });

    // ── Toggle mostrar/ocultar senha ───────────────────────────────────────
    const toggleSenhaBtn  = document.getElementById('toggleSenha');
    const toggleSenhaIcon = document.getElementById('toggleSenhaIcon');
    const senhaInput      = document.getElementById('senha');

    if (toggleSenhaBtn && senhaInput && toggleSenhaIcon) {
        toggleSenhaBtn.addEventListener('click', () => {
            const visivel = senhaInput.type === 'text';
            senhaInput.type = visivel ? 'password' : 'text';
            toggleSenhaIcon.classList.toggle('fa-eye',       visivel);
            toggleSenhaIcon.classList.toggle('fa-eye-slash', !visivel);
            toggleSenhaBtn.setAttribute(
                'aria-label',
                visivel ? 'Mostrar senha' : 'Ocultar senha'
            );
        });
    }

    // ── Alternância entre painéis ──────────────────────────────────────────
    document.getElementById('linkCriarConta')
        ?.addEventListener('click', () => _mostrarPainel('cadastro'));
    document.getElementById('linkEsqueciSenha')
        ?.addEventListener('click', () => _mostrarPainel('esqueci'));
    document.getElementById('linkVoltarLogin1')
        ?.addEventListener('click', () => _mostrarPainel('login'));
    document.getElementById('linkVoltarLogin2')
        ?.addEventListener('click', () => _mostrarPainel('login'));

    function _mostrarPainel(painel) {
        painelLogin.classList.add('hidden');
        painelCadastro.classList.add('hidden');
        painelEsqueciSenha.classList.add('hidden');

        if (painel === 'login')    painelLogin.classList.remove('hidden');
        if (painel === 'cadastro') painelCadastro.classList.remove('hidden');
        if (painel === 'esqueci')  {
            painelEsqueciSenha.classList.remove('hidden');
            // Reseta o painel de recuperação ao abrir
            _resetarPainelEsqueci();
        }
    }

    // ── Mostrar / ocultar app ──────────────────────────────────────────────
    function mostrarApp() {
        loginView.classList.add('hidden');
        loginView.classList.remove('active');
        appView.classList.remove('hidden');
        appView.classList.add('active');
    }

    function mostrarLogin() {
        appView.classList.add('hidden');
        appView.classList.remove('active');
        loginView.classList.remove('hidden');
        loginView.classList.add('active');
        loginForm.reset();
        errMsg.style.display = 'none';
        _mostrarPainel('login');
    }

    // ── Login ──────────────────────────────────────────────────────────────
    function bindSubmit(callback) {
        loginForm.addEventListener('submit', (e) => {
            e.preventDefault();
            callback({
                email: document.getElementById('email').value.trim(),
                senha: document.getElementById('senha').value
            });
        });
    }

    /** Exibe mensagem de erro inline no formulário de login. */
    function exibirErro(msg) {
        errMsg.textContent = '\u26A0\uFE0F ' + msg;
        errMsg.style.display = 'block';
    }

    // ── Cadastro ───────────────────────────────────────────────────────────
    /**
     * Registra o submit do formulário de cadastro.
     * @param {Function} callback — recebe { nome, cpf, email, matricula, curso, senha }
     */
    function bindCadastro(callback) {
        const form = document.getElementById('formCadastro');
        if (!form) return;
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            callback({
                nome:     document.getElementById('cadastroNome').value.trim(),
                cpf:      document.getElementById('cadastroCpf').value.trim(),
                email:    document.getElementById('cadastroEmail').value.trim(),
                matricula:document.getElementById('cadastroMatricula').value.trim(),
                curso:    document.getElementById('cadastroCurso').value.trim(),
                senha:    document.getElementById('cadastroSenha').value,
                confirmar:document.getElementById('cadastroConfirmarSenha').value
            });
        });
    }

    /** Exibe feedback no painel de cadastro (sucesso ou erro). */
    function feedbackCadastro(msg, tipo) {
        const btn = document.getElementById('btnCadastrar');
        _inserirFeedback('formCadastro', msg, tipo, btn);
    }

    // ── Esqueci a senha ────────────────────────────────────────────────────
    /**
     * Registra o submit do formulário de esqueci-senha.
     * O fluxo tem 2 etapas:
     *   1ª) buscar conta pelo e-mail → exibe campo nova senha
     *   2ª) submit com nova senha → envia alteração
     * @param {Function} onBuscarConta — recebe (email) → deve retornar { id, nome }
     * @param {Function} onAlterarSenha — recebe (id, novaSenha)
     */
    function bindEsqueciSenha(onBuscarConta, onAlterarSenha) {
        const form         = document.getElementById('formEsqueciSenha');
        const grupaNova    = document.getElementById('grupaNovaSenha');
        const inputNova    = document.getElementById('recuperarNovaSenha');
        const btnRecuperar = document.getElementById('btnRecuperar');
        if (!form) return;

        let contaEncontrada = null; // guarda { id, nome } após busca

        form.addEventListener('submit', async (e) => {
            e.preventDefault();

            const email = document.getElementById('recuperarEmail').value.trim();

            // ETAPA 2: conta já encontrada → altera senha
            if (contaEncontrada) {
                const novaSenha = inputNova.value;
                if (!novaSenha || novaSenha.length < 6) {
                    _exibirResultado('A senha deve ter no mínimo 6 caracteres.', 'erro');
                    return;
                }
                await onAlterarSenha(contaEncontrada.id, novaSenha);
                return;
            }

            // ETAPA 1: busca conta pelo e-mail
            if (!email) {
                _exibirResultado('Informe o e-mail.', 'erro');
                return;
            }

            btnRecuperar.disabled = true;
            btnRecuperar.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Buscando...';

            const conta = await onBuscarConta(email);

            btnRecuperar.disabled = false;

            if (conta) {
                contaEncontrada = conta;
                // Mostra campo de nova senha
                grupaNova.classList.remove('hidden');
                inputNova.required = true;
                btnRecuperar.innerHTML = '<i class="fa-solid fa-key"></i> Redefinir Senha';
                _exibirResultado(
                    `<i class="fa-solid fa-circle-check" style="color:var(--success)"></i> Conta encontrada: <strong>${conta.nome}</strong>. Defina a nova senha abaixo.`,
                    'ok'
                );
            } else {
                btnRecuperar.innerHTML = '<i class="fa-solid fa-magnifying-glass"></i> Buscar conta';
            }
        });
    }

    /** Exibe resultado no painel "Esqueci a Senha". */
    function _exibirResultado(html, tipo) {
        const el = document.getElementById('recuperarResultado');
        if (!el) return;
        el.style.cssText = `margin-top:0.75rem;font-size:0.83rem;color:${tipo === 'erro' ? '#DC2626' : 'var(--text-dark)'};`;
        el.innerHTML = html;
    }

    /** Limpa o painel de recuperação ao abrir novamente. */
    function _resetarPainelEsqueci() {
        const form      = document.getElementById('formEsqueciSenha');
        const grupaNova = document.getElementById('grupaNovaSenha');
        const inputNova = document.getElementById('recuperarNovaSenha');
        const btnRec    = document.getElementById('btnRecuperar');
        const resultado = document.getElementById('recuperarResultado');

        if (form)      form.reset();
        if (grupaNova) grupaNova.classList.add('hidden');
        if (inputNova) inputNova.required = false;
        if (btnRec)    btnRec.innerHTML = '<i class="fa-solid fa-magnifying-glass"></i> Buscar conta';
        if (resultado) resultado.innerHTML = '';
    }

    // ── Helper: feedback abaixo de botão ──────────────────────────────────
    function _inserirFeedback(formId, msg, tipo, refBtn) {
        const form = document.getElementById(formId);
        if (!form) return;
        let fb = form.querySelector('.form-feedback');
        if (!fb) {
            fb = document.createElement('p');
            fb.className = 'form-feedback';
            refBtn ? refBtn.insertAdjacentElement('afterend', fb) : form.appendChild(fb);
        }
        fb.style.cssText = `font-size:0.83rem;margin-top:0.5rem;color:${tipo === 'erro' ? '#DC2626' : 'var(--success)'};`;
        fb.textContent = msg;
    }

    return { mostrarApp, mostrarLogin, bindSubmit, exibirErro, bindCadastro, feedbackCadastro, bindEsqueciSenha };
})();

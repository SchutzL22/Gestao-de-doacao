/* =====================================================
   COPER FMP | js/routeGuard.js
   Mecanismo de proteção de rotas (Route Guard).

   Verifica se existe uma sessão válida salva no localStorage
   antes de permitir acesso às views protegidas (admin / aluno).

   Uso:
     RouteGuard.verificar()          → redireciona ao login se não autenticado
     RouteGuard.exigirTipo('admin')  → redireciona se o tipo não corresponder
     RouteGuard.getSessao()          → retorna o objeto de sessão ou null
   ===================================================== */

const RouteGuard = (() => {

    /** Chave usada pelo UsuarioModel para persistir a sessão. */
    const SESSAO_KEY = 'coper_sessao';

    /**
     * Lê e faz parse da sessão salva no localStorage.
     * Retorna null se não houver sessão ou se o JSON estiver corrompido.
     */
    function getSessao() {
        try {
            const raw = localStorage.getItem(SESSAO_KEY);
            return raw ? JSON.parse(raw) : null;
        } catch {
            return null;
        }
    }

    /**
     * Verifica se há um utilizador autenticado.
     * Se não houver sessão válida, oculta o appView e força o loginView
     * imediatamente — antes de qualquer conteúdo protegido ser exibido.
     *
     * @returns {boolean} true se autenticado, false se redirecionou para login.
     */
    function verificar() {
        const sessao = getSessao();

        if (!sessao || !sessao.id) {
            _redirecionarParaLogin('Acesso restrito. Faça login para continuar.');
            return false;
        }

        return true;
    }

    /**
     * Verifica autenticação E se o tipo do utilizador corresponde ao esperado.
     * Útil para proteger views exclusivas de 'admin' ou 'aluno'.
     *
     * @param {'admin'|'aluno'} tipoEsperado
     * @returns {boolean} true se autenticado com o tipo correto.
     */
    function exigirTipo(tipoEsperado) {
        const sessao = getSessao();

        if (!sessao || !sessao.id) {
            _redirecionarParaLogin('Acesso restrito. Faça login para continuar.');
            return false;
        }

        if (sessao.tipo !== tipoEsperado) {
            _redirecionarParaLogin(`Acesso negado. Esta área é restrita a utilizadores do tipo "${tipoEsperado}".`);
            return false;
        }

        return true;
    }

    /**
     * Força a exibição do loginView e oculta o appView.
     * Funciona mesmo antes do DoacaoController ou LoginView estarem inicializados,
     * operando diretamente sobre o DOM.
     *
     * @param {string} [motivo] - Mensagem opcional a exibir no console.
     */
    function _redirecionarParaLogin(motivo) {
        if (motivo) {
            console.warn(`[RouteGuard] ${motivo}`);
        }

        // Limpa a sessão corrompida ou inválida do storage
        try {
            localStorage.removeItem(SESSAO_KEY);
            sessionStorage.removeItem(SESSAO_KEY);
        } catch { /* ignorar erros de storage em contextos restritos */ }

        // Exibe o loginView e oculta o appView diretamente no DOM
        // (evita loop de redirecionamento login.html ↔ index.html)
        const loginView = document.getElementById('loginView');
        const appView   = document.getElementById('appView');
        if (loginView) { loginView.classList.remove('hidden'); loginView.classList.add('active'); }
        if (appView)   { appView.classList.add('hidden');      appView.classList.remove('active'); }
    }

    // API pública
    return { verificar, exigirTipo, getSessao, redirecionarParaLogin: _redirecionarParaLogin };

})();

// Interceptador global de requisições fetch para tratar tokens expirados (401/403)
(() => {
    const originalFetch = window.fetch;
    window.fetch = async function(...args) {
        try {
            const response = await originalFetch(...args);
            const url = typeof args[0] === 'string' ? args[0] : (args[0] && args[0].url);
            const isLoginRequest = url && url.includes('/usuarios/login');

            if ((response.status === 401 || response.status === 403) && !isLoginRequest) {
                RouteGuard.redirecionarParaLogin(`Sessão expirada ou não autorizada (${response.status}).`);
            }
            return response;
        } catch (error) {
            throw error;
        }
    };
})();


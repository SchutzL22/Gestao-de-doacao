/* =====================================================
   COPER FMP | js/model/NotificacaoModel.js
   Dados de notificações consumindo o backend.
   ===================================================== */

const NotificacaoModel = (() => {

    async function listarPorAluno(alunoId) {
        try {
            const response = await fetch(`${API_BASE_URL}/notificacoes/aluno/${alunoId}`);
            if (!response.ok) {
                const errData = await response.json().catch(() => ({}));
                throw new Error(errData.erro || errData.message || 'Erro ao obter notificações.');
            }
            return await response.json();
        } catch (error) {
            console.error("Erro em NotificacaoModel.listarPorAluno:", error);
            throw error;
        }
    }

    return { listarPorAluno };
})();

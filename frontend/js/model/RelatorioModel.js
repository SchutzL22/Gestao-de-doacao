/* =====================================================
   COPER FMP | js/model/RelatorioModel.js
   Busca dados agregados para a aba de Relatórios.
   Endpoint: GET /api/relatorios
   ===================================================== */

const RelatorioModel = (() => {

    async function buscar() {
        const res = await fetch(`${API_BASE_URL}/relatorios`);
        if (!res.ok) throw new Error('Erro ao carregar dados de relatório.');
        return await res.json();
    }

    return { buscar };
})();

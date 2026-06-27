/* =====================================================
   COPER FMP | js/view/admin/RelatoriosAdminView.js
   Exibe dados agregados do endpoint GET /api/relatorios.
   ===================================================== */

const RelatoriosAdminView = (() => {

    /**
     * Renderiza o painel de relatórios.
     * @param {Object} dados — { totalDoacoes, totalAprovadas, totalReprovadas,
     *                           totalPendentes, totalHorasConcedidas, porCampanha[] }
     */
    function render(dados) {
        _renderKPIs(dados);
        _renderTabelaCampanhas(dados.porCampanha || []);
    }

    function _renderKPIs(dados) {
        _set('relTotalDoacoes',    dados.totalDoacoes    ?? 0);
        _set('relTotalAprovadas',  dados.totalAprovadas  ?? 0);
        _set('relTotalReprovadas', dados.totalReprovadas ?? 0);
        _set('relTotalPendentes',  dados.totalPendentes  ?? 0);
        _set('relTotalHoras',      (dados.totalHorasConcedidas ?? 0).toFixed(1) + 'h');
    }

    function _set(id, valor) {
        const el = document.getElementById(id);
        if (el) el.textContent = valor;
    }

    function _renderTabelaCampanhas(porCampanha) {
        const tbody = document.getElementById('tableRelatorioBody');
        if (!tbody) return;
        tbody.innerHTML = '';

        if (!porCampanha.length) {
            tbody.innerHTML = `<tr><td colspan="3" style="text-align:center;
                color:var(--text-muted);padding:1.5rem;">Nenhum dado disponível.</td></tr>`;
            return;
        }

        porCampanha.forEach(row => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${row.campanhaTitulo}</td>
                <td>${row.totalDoacoes}</td>
                <td>${(row.horasConcedidas ?? 0).toFixed(1)}h</td>
            `;
            tbody.appendChild(tr);
        });
    }

    return { render };
})();

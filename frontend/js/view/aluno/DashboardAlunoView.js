/* js/view/aluno/DashboardAlunoView.js
   Renderiza o painel do aluno com dados dinâmicos. */

const DashboardAlunoView = (() => {

    function render(saldoHoras, totalDoacoes) {
        const saldoEl = document.getElementById('saldoHorasAluno');
        const totalEl = document.getElementById('doacoesRealizadasAluno');
        if (saldoEl) saldoEl.textContent = `${saldoHoras}h`;
        if (totalEl) totalEl.textContent = totalDoacoes;
    }

    return { render };
})();

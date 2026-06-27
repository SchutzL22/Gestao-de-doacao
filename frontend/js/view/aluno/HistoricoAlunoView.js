/* js/view/aluno/HistoricoAlunoView.js
   Renderiza a tabela de histórico de doações do aluno. */

const HistoricoAlunoView = (() => {

    function render(doacoes) {
        const tbody = document.getElementById('tableHistoricoBody');
        tbody.innerHTML = '';

        doacoes.forEach(d => {
            const isAprovada = d.status === 'Aprovada';
            const comprovante = isAprovada
                ? `<button class="btn-icon pdf" data-doacao-id="${d.id}" data-campanha="${d.campanha}" title="Baixar PDF Autenticado"><i class="fa-solid fa-file-pdf"></i></button>`
                : '-';

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${d.data}</td>
                <td>${d.campanha}</td>
                <td>${d.horas}h</td>
                <td><span class="badge ${isAprovada ? 'badge-aprovado' : 'badge-pendente'}">${d.status}</span></td>
                <td>${comprovante}</td>
            `;
            tbody.appendChild(tr);
        });
    }

    return { render };
})();

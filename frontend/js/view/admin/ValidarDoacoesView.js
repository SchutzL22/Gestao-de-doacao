/* js/view/admin/ValidarDoacoesView.js
   Renderiza a tabela de validação de doações. */

const ValidarDoacoesView = (() => {

    function render(doacoes) {
        const tbody = document.getElementById('tableValidacaoBody');
        tbody.innerHTML = '';

        doacoes.forEach(d => {
            const isPendente = d.status === 'Pendente';
            const isAguardando = d.status === 'Aguardando Correção';
            const isProcessavel = isPendente || isAguardando;

            let acoes = '';
            if (isProcessavel) {
                const btnApprove = isPendente 
                    ? `<button class="btn-icon check" data-id="${d.id}" title="Aprovar"><i class="fa-solid fa-check"></i></button>`
                    : `<button class="btn-icon check" data-id="${d.id}" title="Aprovar (Aguardando Matrícula)" disabled style="opacity: 0.5; cursor: not-allowed;"><i class="fa-solid fa-check"></i></button>`;
                
                acoes = `
                    ${btnApprove}
                    <button class="btn-icon times" data-id="${d.id}" title="Reprovar"><i class="fa-solid fa-xmark"></i></button>
                    <button class="btn-icon edit" data-id="${d.id}" title="Editar"><i class="fa-solid fa-pen-to-square"></i></button>
                `;
            } else {
                acoes = '<span class="text-muted">Processado</span>';
            }

            let badgeClass = 'badge-pendente';
            if (d.status === 'Aprovada') badgeClass = 'badge-aprovado';
            else if (d.status === 'Reprovada') badgeClass = 'badge-urgente';
            else if (d.status === 'Aguardando Correção') badgeClass = 'badge-aguardando';

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${d.data}</td>
                <td>${d.aluno ? d.aluno : '<span class="text-muted"><em>Não informado</em></span>'}<br><small class="text-muted">${d.matricula || ''}</small></td>
                <td>${d.tipoItem}</td>
                <td>${d.quantidade}</td>
                <td><span class="badge ${badgeClass}">${d.status}</span></td>
                <td>${acoes}</td>
            `;
            tbody.appendChild(tr);
        });
    }

    function bindEditar(callback) {
        const tbody = document.getElementById('tableValidacaoBody');
        if (!tbody) return;
        tbody.querySelectorAll('button.btn-icon.edit').forEach(btn => {
            btn.addEventListener('click', () => {
                const id = btn.getAttribute('data-id');
                callback(id);
            });
        });
    }

    return { render, bindEditar };
})();


/* =====================================================
   COPER FMP | js/view/admin/ItensPrioritariosAdminView.js
   View de CRUD de Itens Prioritários para o administrador.
   ===================================================== */

const ItensPrioritariosAdminView = (() => {

    // IDs dos campos do formulário
    const FORM_ID        = 'formItemPrioritario';
    const INPUT_NOME_ID  = 'itemNome';
    const INPUT_META_ID  = 'itemMeta';
    const SELECT_URG_ID  = 'itemUrgencia';
    const INPUT_ID_ID    = 'itemIdEdicao';   // hidden — guarda ID ao editar
    const TBODY_ID       = 'tableItensBody';
    const BTN_CANCEL_ID  = 'btnCancelarEdicaoItem';

    /** Urgência → badge CSS */
    function _badgeUrgencia(urgencia) {
        const u = (urgencia || 'NORMAL').toUpperCase();
        if (u === 'ALTA')   return `<span class="badge badge-urgente">Alta</span>`;
        if (u === 'MEDIA')  return `<span class="badge badge-pendente">Média</span>`;
        return                     `<span class="badge badge-aprovado">Normal</span>`;
    }

    /**
     * Renderiza a tabela de itens.
     * @param {Array} itens — lista de { id, nome, meta, urgencia }
     */
    function render(itens) {
        const tbody = document.getElementById(TBODY_ID);
        if (!tbody) return;
        tbody.innerHTML = '';

        if (!itens || itens.length === 0) {
            tbody.innerHTML = `<tr><td colspan="4" style="text-align:center;color:var(--text-muted);padding:1.5rem;">
                Nenhum item cadastrado ainda.</td></tr>`;
            return;
        }

        itens.forEach(item => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${item.nome}</td>
                <td>${item.meta != null ? item.meta : '—'}</td>
                <td>${_badgeUrgencia(item.urgencia)}</td>
                <td>
                    <button class="btn-icon edit"  data-id="${item.id}" title="Editar">
                        <i class="fa-solid fa-pen-to-square"></i>
                    </button>
                    <button class="btn-icon times" data-id="${item.id}" title="Excluir">
                        <i class="fa-solid fa-trash"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    }

    /**
     * Preenche o formulário com os dados de um item para edição.
     * @param {Object} item
     */
    function preencherFormEdicao(item) {
        const idFld   = document.getElementById(INPUT_ID_ID);
        const nomeFld = document.getElementById(INPUT_NOME_ID);
        const metaFld = document.getElementById(INPUT_META_ID);
        const urgFld  = document.getElementById(SELECT_URG_ID);
        const cancel  = document.getElementById(BTN_CANCEL_ID);
        const titulo  = document.getElementById('tituloFormItem');

        if (idFld)   idFld.value   = item.id;
        if (nomeFld) nomeFld.value = item.nome;
        if (metaFld) metaFld.value = item.meta != null ? item.meta : '';
        if (urgFld)  urgFld.value  = (item.urgencia || 'NORMAL').toUpperCase();
        if (cancel)  cancel.classList.remove('hidden');
        if (titulo)  titulo.textContent = 'Editar Item Prioritário';

        if (nomeFld) nomeFld.focus();
    }

    /** Limpa o formulário (modo criação). */
    function limparForm() {
        const form   = document.getElementById(FORM_ID);
        const idFld  = document.getElementById(INPUT_ID_ID);
        const cancel = document.getElementById(BTN_CANCEL_ID);
        const titulo = document.getElementById('tituloFormItem');

        if (form)   form.reset();
        if (idFld)  idFld.value = '';
        if (cancel) cancel.classList.add('hidden');
        if (titulo) titulo.textContent = 'Novo Item Prioritário';
    }

    /**
     * Registra o submit do formulário.
     * @param {Function} callback — recebe ({ id, nome, meta, urgencia })
     */
    function bindFormSubmit(callback) {
        const form = document.getElementById(FORM_ID);
        if (!form) return;
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            const id      = document.getElementById(INPUT_ID_ID)?.value  || null;
            const nome    = document.getElementById(INPUT_NOME_ID)?.value.trim() || '';
            const meta    = document.getElementById(INPUT_META_ID)?.value;
            const urgencia= document.getElementById(SELECT_URG_ID)?.value || 'NORMAL';
            callback({
                id:       id ? Number(id) : null,
                nome,
                meta:     meta !== '' ? parseInt(meta, 10) : null,
                urgencia
            });
        });
    }

    /**
     * Registra os botões Editar e Excluir da tabela.
     * @param {Function} onEditar   — recebe (id)
     * @param {Function} onExcluir  — recebe (id)
     */
    function bindTabelaBotoes(onEditar, onExcluir) {
        const tbody = document.getElementById(TBODY_ID);
        if (!tbody) return;

        tbody.querySelectorAll('button.btn-icon.edit').forEach(btn => {
            btn.addEventListener('click', () => onEditar(Number(btn.getAttribute('data-id'))));
        });
        tbody.querySelectorAll('button.btn-icon.times').forEach(btn => {
            btn.addEventListener('click', () => onExcluir(Number(btn.getAttribute('data-id'))));
        });
    }

    /** Registra o botão Cancelar edição. */
    function bindCancelar(callback) {
        const btn = document.getElementById(BTN_CANCEL_ID);
        if (btn) btn.addEventListener('click', () => { limparForm(); if (callback) callback(); });
    }

    return { render, preencherFormEdicao, limparForm, bindFormSubmit, bindTabelaBotoes, bindCancelar };
})();

/* js/view/admin/CampanhasAdminView.js
   Renderiza a lista de campanhas, formulário de criação e modal de edição
   para o admin. Inclui suporte a itens prioritários dinâmicos (UC13). */

const CampanhasAdminView = (() => {

    // ── RENDER CARDS ──────────────────────────────────────────────────────
    function render(campanhas) {
        const container = document.getElementById('listaCampanhasAdmin');
        if (!container) return;
        container.innerHTML = '';

        if (!campanhas || campanhas.length === 0) {
            container.innerHTML = '<p class="text-muted" style="text-align:center;padding:2rem;">Nenhuma campanha cadastrada ainda.</p>';
            return;
        }

        campanhas.forEach(c => {
            const statusClass = c.urgente     ? 'badge-urgente'
                              : c.status === 'Ativa'      ? 'badge-aprovado'
                              : c.status === 'Encerrada'  ? 'badge-pendente'
                              : 'badge-pendente';
            const statusLabel = c.status || (c.urgente ? 'Urgente' : 'Ativa');
            const encerrada   = c.status === 'Encerrada';

            // Mini-lista de itens prioritários
            const itensHtml = (c.itensPrioritarios && c.itensPrioritarios.length > 0)
                ? `<div class="card-itens-lista">
                     ${c.itensPrioritarios.map(i => {
                         const urg = (i.urgencia || 'NORMAL').toUpperCase();
                         const cls = urg === 'ALTA' ? 'badge-urgente' : urg === 'MEDIA' ? 'badge-pendente' : 'badge-aprovado';
                         return `<span class="badge ${cls}" style="font-size:0.7rem;">
                             <i class="fa-solid fa-box"></i> ${i.nome}${i.meta ? ` (${i.meta})` : ''}
                         </span>`;
                     }).join('')}
                   </div>`
                : '';

            const card = document.createElement('div');
            card.className = 'card-campanha admin-campanha-card';
            card.innerHTML = `
                <div class="campanha-card-header">
                    <h3>${c.titulo}</h3>
                    <span class="badge ${statusClass}">${statusLabel}</span>
                </div>
                <p class="campanha-desc">${c.desc || 'Sem descrição.'}</p>
                ${itensHtml}
                <div class="campanha-card-footer">
                    ${c.dataLimite ? `<small class="text-muted"><i class="fa-solid fa-calendar-days"></i> Limite: ${c.dataLimite}</small>` : ''}
                    ${c.limiteHoras ? `<small class="text-muted"><i class="fa-solid fa-clock"></i> ${c.limiteHoras}h</small>` : ''}
                    <div class="campanha-actions">
                        <button class="btn-icon edit btn-editar-campanha" data-id="${c.id}" title="Editar campanha">
                            <i class="fa-solid fa-pen-to-square"></i>
                        </button>
                        <button class="btn-icon ${encerrada ? '' : 'times'} btn-encerrar-campanha"
                                data-id="${c.id}" title="${encerrada ? 'Já encerrada' : 'Encerrar campanha'}"
                                ${encerrada ? 'disabled' : ''}>
                            <i class="fa-solid fa-ban"></i>
                        </button>
                    </div>
                </div>
            `;
            container.appendChild(card);
        });
    }

    // ── BIND BOTÕES DOS CARDS ─────────────────────────────────────────────
    function bindBotoesCampanha(campanhas, onEditar, onEncerrar) {
        document.querySelectorAll('.btn-editar-campanha').forEach(btn => {
            btn.addEventListener('click', () => {
                const id = Number(btn.getAttribute('data-id'));
                const camp = campanhas.find(c => c.id === id);
                if (camp) onEditar(camp);
            });
        });
        document.querySelectorAll('.btn-encerrar-campanha:not([disabled])').forEach(btn => {
            btn.addEventListener('click', () => onEncerrar(Number(btn.getAttribute('data-id'))));
        });
    }

    // ── FORM NOVA CAMPANHA ────────────────────────────────────────────────
    function bindNovaCampanha(callback) {
        const form = document.getElementById('formNovaCampanha');
        if (!form) return;

        // Bind do botão "+ Adicionar Item"
        document.getElementById('btnAdicionarItemNova')
            ?.addEventListener('click', () => _adicionarLinhaItem('listaItensNovaCampanha'));

        form.addEventListener('submit', (e) => {
            e.preventDefault();
            const titulo    = document.getElementById('campanhaTitulo').value.trim();
            const descricao = document.getElementById('campanhaDesc').value.trim();
            const limite    = document.getElementById('campanhaLimite').value.trim();
            const itens     = _coletarItens('listaItensNovaCampanha');
            callback({ titulo, descricao, dataLimite: limite || null, itensPrioritarios: itens });
        });
    }

    // ── MODAL EDITAR CAMPANHA ─────────────────────────────────────────────
    /** Preenche o modal com os dados da campanha selecionada. */
    function preencherModalEdicao(campanha) {
        _set('editarCampanhaId', campanha.id);
        _set('editarTitulo',     campanha.titulo);
        _set('editarDesc',       campanha.desc || '');
        _set('editarLimite',     campanha.dataLimite || '');
        _set('editarLimiteHoras', campanha.limiteHoras || '');

        const selStatus = document.getElementById('editarStatus');
        if (selStatus) selStatus.value = campanha.status || 'Ativa';

        // Popula a lista de itens no modal
        const listaEl = document.getElementById('listaItensEditarCampanha');
        if (listaEl) {
            listaEl.innerHTML = '';
            (campanha.itensPrioritarios || []).forEach(item => {
                _adicionarLinhaItem('listaItensEditarCampanha', item);
            });
        }
    }

    /** Registra o submit do formulário de edição do modal. */
    function bindFormEdicao(callback) {
        const form = document.getElementById('formEditarCampanha');
        if (!form) return;

        document.getElementById('btnAdicionarItemEditar')
            ?.addEventListener('click', () => _adicionarLinhaItem('listaItensEditarCampanha'));

        form.addEventListener('submit', (e) => {
            e.preventDefault();
            const id          = Number(document.getElementById('editarCampanhaId').value);
            const titulo      = document.getElementById('editarTitulo').value.trim();
            const descricao   = document.getElementById('editarDesc').value.trim();
            const dataLimite  = document.getElementById('editarLimite').value || null;
            const status      = document.getElementById('editarStatus').value;
            const limiteHoras = parseFloat(document.getElementById('editarLimiteHoras').value) || null;
            const itensPrioritarios = _coletarItens('listaItensEditarCampanha');
            callback({ id, titulo, descricao, dataLimite, status, limiteHoras, itensPrioritarios });
        });
    }

    // ── HELPERS INTERNOS ─────────────────────────────────────────────────

    /** Adiciona uma linha de item (nome, meta, urgência) numa lista dinâmica. */
    function _adicionarLinhaItem(containerId, item = null) {
        const container = document.getElementById(containerId);
        if (!container) return;

        // Remove o placeholder "Nenhum item..." se existir
        const placeholder = container.querySelector('p.text-muted');
        if (placeholder) placeholder.remove();

        const row = document.createElement('div');
        row.className = 'item-dinamico-row';
        row.innerHTML = `
            <input type="text"   class="item-nome"     placeholder="Nome do item"      value="${item?.nome || ''}" required>
            <input type="number" class="item-meta"     placeholder="Meta (un.)"   min="1" value="${item?.meta ?? ''}">
            <select class="item-urgencia">
                <option value="NORMAL" ${(!item || item.urgencia === 'NORMAL') ? 'selected' : ''}>Normal</option>
                <option value="MEDIA"  ${item?.urgencia === 'MEDIA'  ? 'selected' : ''}>Média</option>
                <option value="ALTA"   ${item?.urgencia === 'ALTA'   ? 'selected' : ''}>Alta</option>
            </select>
            <button type="button" class="btn-icon times btn-remover-item" title="Remover item">
                <i class="fa-solid fa-xmark"></i>
            </button>
        `;
        row.querySelector('.btn-remover-item').addEventListener('click', () => row.remove());
        container.appendChild(row);
    }

    /** Coleta os itens das linhas dinâmicas de um container. */
    function _coletarItens(containerId) {
        const container = document.getElementById(containerId);
        if (!container) return [];
        return Array.from(container.querySelectorAll('.item-dinamico-row'))
            .map(row => ({
                nome:     row.querySelector('.item-nome').value.trim(),
                meta:     parseInt(row.querySelector('.item-meta').value) || null,
                urgencia: row.querySelector('.item-urgencia').value
            }))
            .filter(i => i.nome); // descarta linhas sem nome
    }

    function _set(id, value) {
        const el = document.getElementById(id);
        if (el) el.value = value ?? '';
    }

    return { render, bindNovaCampanha, bindBotoesCampanha, preencherModalEdicao, bindFormEdicao };
})();

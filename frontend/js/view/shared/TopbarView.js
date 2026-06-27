/* js/view/shared/TopbarView.js
   Gerencia as informações do usuário, barra de pesquisa e notificações no topbar. */

const TopbarView = (() => {

    function exibirUsuario(nome, role, tipo) {
        const nameEl = document.getElementById('userNameDisplay');
        const roleEl = document.getElementById('userRoleDisplay');
        if (nameEl) nameEl.textContent = nome;
        if (roleEl) roleEl.textContent = role;

        const sinoContainer = document.getElementById('notificacoesDropdownContainer');
        if (sinoContainer) {
            sinoContainer.style.display = tipo === 'aluno' ? 'block' : 'none';
        }
    }

    function initSearch() {
        const input = document.getElementById('topbarSearchInput');
        if (!input) return;
        input.addEventListener('input', (e) => {
            const termo = e.target.value.toLowerCase().trim();
            filtrarTelaAtual(termo);
        });
    }

    function filtrarTelaAtual(termo) {
        const secaoAtiva = document.querySelector('.page-section:not(.hidden)');
        if (!secaoAtiva) return;

        // 1. Filtrar linhas de tabelas (ex: doações, itens prioritários, relatórios)
        const rows = secaoAtiva.querySelectorAll('table tbody tr');
        rows.forEach(row => {
            const texto = row.textContent.toLowerCase();
            row.style.display = texto.includes(termo) ? '' : 'none';
        });

        // 2. Filtrar cards de campanha (admin/aluno)
        const cards = secaoAtiva.querySelectorAll('.card-campanha');
        cards.forEach(card => {
            const texto = card.textContent.toLowerCase();
            card.style.display = texto.includes(termo) ? '' : 'none';
        });
    }

    function initNotifications() {
        const btn = document.getElementById('btnNotificacoes');
        const dropdown = document.getElementById('dropdownNotificacoes');
        if (!btn || !dropdown) return;

        btn.addEventListener('click', (e) => {
            e.stopPropagation();
            dropdown.classList.toggle('hidden');
        });

        document.addEventListener('click', (e) => {
            if (!btn.contains(e.target) && !dropdown.contains(e.target)) {
                dropdown.classList.add('hidden');
            }
        });
    }

    function configurarNotificacoes(notificacoes) {
        const badge = document.getElementById('badgeNotificacoes');
        const dropdown = document.getElementById('dropdownNotificacoes');
        if (!badge || !dropdown) return;

        dropdown.innerHTML = '';

        if (!notificacoes || notificacoes.length === 0) {
            badge.classList.add('hidden');
            badge.textContent = '0';
            dropdown.innerHTML = '<p class="text-muted" style="text-align:center;font-size:0.8rem;margin:0.5rem 0;">Sem novas notificações.</p>';
            return;
        }

        badge.classList.remove('hidden');
        badge.textContent = notificacoes.length;

        notificacoes.forEach(n => {
            const item = document.createElement('div');
            item.className = 'notification-item';
            item.style.cssText = 'padding:0.5rem;border-bottom:1px solid var(--border);font-size:0.8rem;line-height:1.3;';
            
            let dataStr = n.dataEnvio || '';
            if (Array.isArray(dataStr)) {
                const [y, m, d, h, min] = dataStr;
                dataStr = `${String(d).padStart(2,'0')}/${String(m).padStart(2,'0')}/${y} ${String(h).padStart(2,'0')}:${String(min).padStart(2,'0')}`;
            } else if (typeof dataStr === 'string' && dataStr.includes('T')) {
                const partes = dataStr.split('T');
                const [y, m, d] = partes[0].split('-');
                const [h, min] = partes[1].split(':');
                dataStr = `${d}/${m}/${y} ${h}:${min}`;
            }

            item.innerHTML = `
                <div style="font-weight:600;margin-bottom:0.15rem;font-size:0.82rem;">Doação #${n.doacao ? n.doacao.id : ''}</div>
                <div style="font-size:0.78rem;color:var(--text-dark);">${n.mensagem}</div>
                <small style="color:var(--text-muted);font-size:0.68rem;display:block;margin-top:0.2rem;">${dataStr}</small>
            `;
            dropdown.appendChild(item);
        });

        // Remove a borda inferior do último item para melhor estética
        if (dropdown.lastChild) {
            dropdown.lastChild.style.borderBottom = 'none';
        }
    }

    document.addEventListener('DOMContentLoaded', () => {
        initSearch();
        initNotifications();
    });

    return { exibirUsuario, initSearch, filtrarTelaAtual, configurarNotificacoes };
})();

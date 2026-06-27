/* js/view/shared/SidebarView.js
   Gerencia a sidebar, menus e navegação entre seções. */

const SidebarView = (() => {

    const menuAdmin = document.getElementById('menuAdmin');
    const menuAluno = document.getElementById('menuAluno');
    const pageTitle = document.getElementById('pageTitle');

    function configurarPorTipo(tipo) {
        document.querySelectorAll('.sidebar-menu li').forEach(li => li.classList.remove('active'));
        document.querySelectorAll('.page-section').forEach(sec => sec.classList.add('hidden'));

        if (tipo === 'admin') {
            menuAdmin.style.display = 'block';
            menuAluno.style.display  = 'none';
            _ativarItem('menuAdmin', 'dashboardAdmin');
            pageTitle.textContent = 'Dashboard Administrativo';
        } else {
            menuAdmin.style.display  = 'none';
            menuAluno.style.display  = 'block';
            _ativarItem('menuAluno', 'dashboardAluno');
            pageTitle.textContent = 'Meu Painel';
        }
    }

    function _ativarItem(menuId, targetId) {
        const item  = document.querySelector(`#${menuId} li[data-target="${targetId}"]`);
        const secao = document.getElementById(targetId);
        if (item)  item.classList.add('active');
        if (secao) secao.classList.remove('hidden');
    }

    function bindNavegacao(onNavegar) {
        const itens = document.querySelectorAll('.sidebar-menu:not([style*="display: none"]) li');

        itens.forEach(item => {
            item.addEventListener('click', () => {
                itens.forEach(i => i.classList.remove('active'));
                item.classList.add('active');

                document.querySelectorAll('.page-section').forEach(sec => {
                    sec.classList.add('hidden');
                    sec.classList.remove('active');
                });

                const targetId = item.getAttribute('data-target');
                const secao = document.getElementById(targetId);
                if (secao) {
                    secao.classList.remove('hidden');
                    secao.classList.add('active');
                }

                pageTitle.textContent = item.textContent.trim();
                onNavegar(targetId);
            });
        });
    }

    function bindLogout(callback) {
        document.getElementById('btnLogout').addEventListener('click', callback);
    }

    return { configurarPorTipo, bindNavegacao, bindLogout };
})();

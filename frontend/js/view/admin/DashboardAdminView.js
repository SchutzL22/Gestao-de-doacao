/* js/view/admin/DashboardAdminView.js
   Renderiza os contadores do dashboard administrativo. */

const DashboardAdminView = (() => {

    function render(pendentes, horas) {
        document.getElementById('countPendentes').textContent = pendentes;
        document.getElementById('countHoras').textContent     = horas;
    }

    return { render };
})();

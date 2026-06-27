/* js/view/admin/RegistrarDoacaoView.js
   Gerencia o formulário de registro de doação. */

const RegistrarDoacaoView = (() => {

    function bindSubmit(callback) {
        document.getElementById('formRegistro').addEventListener('submit', (e) => {
            e.preventDefault();
            callback(e);
        });
    }

    return { bindSubmit };
})();

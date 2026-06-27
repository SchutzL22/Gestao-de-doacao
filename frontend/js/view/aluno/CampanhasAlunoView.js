/* js/view/aluno/CampanhasAlunoView.js
   Renderiza os cards de campanhas ativas para o aluno. */

const CampanhasAlunoView = (() => {

    function render(campanhas) {
        const container = document.getElementById('listaCampanhas');
        container.innerHTML = '';

        campanhas.forEach(c => {
            const div = document.createElement('div');
            div.className = 'card-campanha';
            div.innerHTML = `
                ${c.urgente ? '<span class="urgente-tag">Itens Urgentes!</span>' : ''}
                <h3>${c.titulo}</h3>
                <p>${c.desc}</p>
                <button class="btn-primary btn-ver-itens"
                        data-campanha-id="${c.id}"
                        data-campanha-nome="${c.titulo}"
                        style="padding: 0.5rem; font-size: 0.85rem;">
                    <i class="fa-solid fa-list-check"></i> Ver Itens Prioritários
                </button>
            `;
            container.appendChild(div);
        });
    }

    return { render };
})();

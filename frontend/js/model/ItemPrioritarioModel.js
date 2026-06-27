/* =====================================================
   COPER FMP | js/model/ItemPrioritarioModel.js
   CRUD de itens prioritários integrado ao backend.
   Endpoint base: GET/POST/PUT/DELETE /api/itens-prioritarios
   ===================================================== */

const ItemPrioritarioModel = (() => {

    function _mapItem(i) {
        return {
            id:       i.id,
            nome:     i.nome     || '—',
            meta:     i.meta     ?? null,
            urgencia: i.urgencia || 'NORMAL'
        };
    }

    async function listarTodos() {
        const res = await fetch(`${API_BASE_URL}/itens-prioritarios`);
        if (!res.ok) throw new Error('Erro ao listar itens prioritários.');
        const lista = await res.json();
        return lista.map(_mapItem);
    }

    async function criar(item) {
        const res = await fetch(`${API_BASE_URL}/itens-prioritarios`, {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify(item)
        });
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || 'Erro ao criar item prioritário.');
        }
        return _mapItem(await res.json());
    }

    async function atualizar(id, item) {
        const res = await fetch(`${API_BASE_URL}/itens-prioritarios/${id}`, {
            method:  'PUT',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify(item)
        });
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || 'Erro ao atualizar item prioritário.');
        }
        return _mapItem(await res.json());
    }

    async function deletar(id) {
        const res = await fetch(`${API_BASE_URL}/itens-prioritarios/${id}`, {
            method: 'DELETE'
        });
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || 'Erro ao excluir item prioritário.');
        }
    }

    return { listarTodos, criar, atualizar, deletar };
})();

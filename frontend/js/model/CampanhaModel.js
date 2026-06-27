/* =====================================================
   COPER FMP | js/model/CampanhaModel.js
   Dados e regras de negócio das campanhas integrados ao backend.
   ===================================================== */

// API_BASE_URL definida em js/config.js (carregado antes de todos os models)

const CampanhaModel = (() => {

    function mapCampanha(c) {
        return {
            id: c.id,
            titulo: c.titulo,
            desc: c.descricao || '',
            urgente: c.status === 'Urgente' || (c.descricao && c.descricao.toLowerCase().includes('urgente')),
            status: c.status,
            dataLimite: c.dataLimite,
            regrasConversao: c.regrasConversao,
            limiteHoras: c.limiteHoras,
            // Itens prioritários vinculados a esta campanha (OneToMany)
            itensPrioritarios: Array.isArray(c.itensPrioritarios)
                ? c.itensPrioritarios.map(i => ({
                    id:       i.id       || null,
                    nome:     i.nome     || '',
                    meta:     i.meta     ?? null,
                    urgencia: i.urgencia || 'NORMAL'
                  }))
                : []
        };
    }

    async function listarTodas() {
        try {
            const response = await fetch(`${API_BASE_URL}/campanhas`);
            if (!response.ok) {
                const errData = await response.json().catch(() => ({}));
                throw new Error(errData.erro || errData.message || 'Erro ao listar campanhas.');
            }
            const list = await response.json();
            return list.map(mapCampanha);
        } catch (error) {
            console.error("Erro em CampanhaModel.listarTodas:", error);
            throw error;
        }
    }

    async function buscarPorId(id) {
        try {
            const response = await fetch(`${API_BASE_URL}/campanhas/${id}`);
            if (!response.ok) {
                const errData = await response.json().catch(() => ({}));
                throw new Error(errData.erro || errData.message || 'Erro ao buscar campanha por ID.');
            }
            const c = await response.json();
            return mapCampanha(c);
        } catch (error) {
            console.error(`Erro em CampanhaModel.buscarPorId(${id}):`, error);
            throw error;
        }
    }

    async function salvar(campanha) {
        try {
            const response = await fetch(`${API_BASE_URL}/campanhas`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(campanha)
            });
            if (!response.ok) {
                const errData = await response.json().catch(() => ({}));
                throw new Error(errData.erro || errData.message || 'Erro ao salvar campanha.');
            }
            const novaCampanha = await response.json();
            return mapCampanha(novaCampanha);
        } catch (error) {
            console.error("Erro em CampanhaModel.salvar:", error);
            throw error;
        }
    }

    async function atualizar(id, campanhaDetails) {
        try {
            const response = await fetch(`${API_BASE_URL}/campanhas/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(campanhaDetails)
            });
            if (!response.ok) {
                const errData = await response.json().catch(() => ({}));
                throw new Error(errData.erro || errData.message || 'Erro ao atualizar campanha.');
            }
            const campanhaAtualizada = await response.json();
            return mapCampanha(campanhaAtualizada);
        } catch (error) {
            console.error(`Erro em CampanhaModel.atualizar(${id}):`, error);
            throw error;
        }
    }

    async function deletar(id) {
        try {
            const response = await fetch(`${API_BASE_URL}/campanhas/${id}`, {
                method: 'DELETE'
            });
            if (!response.ok) {
                const errData = await response.json().catch(() => ({}));
                throw new Error(errData.erro || errData.message || 'Erro ao deletar campanha.');
            }
        } catch (error) {
            console.error(`Erro em CampanhaModel.deletar(${id}):`, error);
            throw error;
        }
    }

    async function encerrar(id) {
        try {
            const response = await fetch(`${API_BASE_URL}/campanhas/${id}/encerrar`, {
                method: 'PUT'
            });
            if (!response.ok) {
                const errData = await response.json().catch(() => ({}));
                throw new Error(errData.erro || errData.message || 'Erro ao encerrar campanha.');
            }
            const campanhaAtualizada = await response.json();
            return mapCampanha(campanhaAtualizada);
        } catch (error) {
            console.error(`Erro em CampanhaModel.encerrar(${id}):`, error);
            throw error;
        }
    }

    return {
        listarTodas,
        buscarPorId,
        salvar,
        atualizar,
        deletar,
        encerrar
    };
})();

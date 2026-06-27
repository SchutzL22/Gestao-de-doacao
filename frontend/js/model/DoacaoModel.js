/* =====================================================
   COPER FMP | js/model/DoacaoModel.js
   Dados e regras de negócio da aplicação integrados ao backend.
   ===================================================== */

// API_BASE_URL definida em js/config.js (carregado antes de todos os models)

const DoacaoModel = (() => {

    /**
     * Tarefa 4 — Formatação de datas para DD/MM/YYYY.
     * Aceita os formatos que o Jackson pode devolver:
     *   - String ISO: "2025-06-25"
     *   - Array:  [2025, 6, 25]
     *   - Objeto: { year: 2025, monthValue: 6, dayOfMonth: 25 }
     *   - null / undefined: retorna '—'
     */
    function formatarDataBR(data) {
        if (!data) return '—';
        try {
            if (Array.isArray(data)) {
                // Jackson pode serializar LocalDate como [ano, mes, dia]
                const [ano, mes, dia] = data;
                return `${String(dia).padStart(2,'0')}/${String(mes).padStart(2,'0')}/${ano}`;
            }
            if (typeof data === 'object') {
                // { year, monthValue, dayOfMonth } — outra representação possível
                const { year, monthValue, dayOfMonth } = data;
                if (year && monthValue && dayOfMonth) {
                    return `${String(dayOfMonth).padStart(2,'0')}/${String(monthValue).padStart(2,'0')}/${year}`;
                }
            }
            if (typeof data === 'string') {
                if (data.includes('-')) {
                    // "2025-06-25" ou "2025-06-25T10:30:00"
                    const parteData = data.split('T')[0]; // ignora horário
                    const [ano, mes, dia] = parteData.split('-');
                    return `${dia.padStart(2,'0')}/${mes.padStart(2,'0')}/${ano}`;
                }
                if (data.includes('/')) {
                    return data; // já está no formato correto
                }
            }
        } catch (e) {
            console.warn('Erro ao formatar data:', data, e);
        }
        return String(data);
    }

    function mapDoacao(d) {
        let statusFormatado = 'Pendente';
        if (d.status === 'APROVADA') statusFormatado = 'Aprovada';
        else if (d.status === 'REPROVADA') statusFormatado = 'Reprovada';
        else if (d.status === 'AGUARDANDO_CORRECAO') statusFormatado = 'Aguardando Correção';

        return {
            id: d.id,
            // Tarefa 4: usa formatarDataBR() em vez da lógica inline anterior
            data: formatarDataBR(d.data),
            aluno: d.aluno ? d.aluno.nome : '',
            matricula: d.aluno ? d.aluno.matricula : '',
            item: d.quantidade && d.tipoItem ? `${d.quantidade}x ${d.tipoItem}` : (d.tipoItem || ''),
            quantidade: d.quantidade || 1,
            tipoItem: d.tipoItem || '',
            status: statusFormatado,
            horas: d.horasConcedidas || 0,
            campanha: d.campanha ? d.campanha.titulo : '',
            localEntrega: d.localEntrega || '',
            anexos: Array.isArray(d.anexos) ? d.anexos.map(a => a.caminhoArquivo) : []
        };
    }

    async function getDoacoes(alunoId) {
        try {
            const url = alunoId 
                ? `${API_BASE_URL}/doacoes/aluno/${alunoId}`
                : `${API_BASE_URL}/doacoes`;
            const response = await fetch(url);
            if (!response.ok) {
                const errData = await response.json().catch(() => ({}));
                throw new Error(errData.erro || errData.message || 'Erro ao listar doações.');
            }
            const list = await response.json();
            return list.map(mapDoacao);
        } catch (error) {
            console.error("Erro em DoacaoModel.getDoacoes:", error);
            throw error;
        }
    }

    async function buscarPorId(id) {
        try {
            const response = await fetch(`${API_BASE_URL}/doacoes/${id}`);
            if (!response.ok) {
                const errData = await response.json().catch(() => ({}));
                throw new Error(errData.erro || errData.message || 'Erro ao buscar campanha por ID.');
            }
            const d = await response.json();
            return mapDoacao(d);
        } catch (error) {
            console.error(`Erro em DoacaoModel.buscarPorId(${id}):`, error);
            throw error;
        }
    }

    async function registrarDoacao(matricula, campanhaId, tipoItem, quantidade, localEntrega, foto) {
        try {
            let alunoId = null;
            if (matricula && matricula.trim() !== '') {
                // 1. Buscar todos os usuários para encontrar o aluno por matrícula
                const responseUsers = await fetch(`${API_BASE_URL}/usuarios`);
                if (!responseUsers.ok) {
                    const errData = await responseUsers.json().catch(() => ({}));
                    throw new Error(errData.erro || errData.message || 'Erro ao verificar lista de alunos.');
                }
                const users = await responseUsers.json();
                const aluno = users.find(u => u.matricula === matricula.trim());
                if (!aluno) {
                    throw new Error(`Aluno não encontrado com a matrícula: ${matricula}`);
                }
                alunoId = aluno.id;
            }

            // 2. Enviar a doação para o backend usando FormData
            const formData = new FormData();
            if (alunoId !== null) {
                formData.append('alunoId', alunoId);
            }
            formData.append('campanhaId', parseInt(campanhaId, 10));
            formData.append('tipoItem', tipoItem);
            formData.append('quantidade', parseInt(quantidade, 10));
            formData.append('localEntrega', localEntrega || '');
            if (foto) {
                formData.append('foto', foto);
            }

            const responseReg = await fetch(`${API_BASE_URL}/doacoes`, {
                method: 'POST',
                // Sem setar Content-Type para deixar o navegador definir o boundary correto
                body: formData
            });
            if (!responseReg.ok) {
                const errData = await responseReg.json().catch(() => ({}));
                throw new Error(errData.erro || errData.message || 'Erro ao registrar doação.');
            }
            const novaDoacao = await responseReg.json();
            return mapDoacao(novaDoacao);
        } catch (error) {
            console.error("Erro em DoacaoModel.registrarDoacao:", error);
            throw error;
        }
    }

    async function validarDoacao(id, status, motivoReprovacao) {
        try {
            const response = await fetch(`${API_BASE_URL}/doacoes/${id}/validar`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ status, motivoReprovacao })
            });
            if (!response.ok) {
                const errData = await response.json().catch(() => ({}));
                throw new Error(errData.erro || errData.message || 'Erro ao validar doação.');
            }
            const doacaoValidada = await response.json();
            return mapDoacao(doacaoValidada);
        } catch (error) {
            console.error(`Erro em DoacaoModel.validarDoacao(${id}):`, error);
            throw error;
        }
    }

    async function deletar(id) {
        try {
            const response = await fetch(`${API_BASE_URL}/doacoes/${id}`, {
                method: 'DELETE'
            });
            if (!response.ok) {
                const errData = await response.json().catch(() => ({}));
                throw new Error(errData.erro || errData.message || 'Erro ao deletar doação.');
            }
        } catch (error) {
            console.error(`Erro em DoacaoModel.deletar(${id}):`, error);
            throw error;
        }
    }

    async function getTotalPendentes() {
        try {
            const doacoes = await getDoacoes();
            return doacoes.filter(d => d.status === 'Pendente').length;
        } catch (error) {
            console.error("Erro em DoacaoModel.getTotalPendentes:", error);
            return 0;
        }
    }

    async function getTotalHoras() {
        try {
            const doacoes = await getDoacoes();
            return doacoes.reduce((acc, d) => acc + d.horas, 0);
        } catch (error) {
            console.error("Erro em DoacaoModel.getTotalHoras:", error);
            return 0;
        }
    }

    async function editarDoacao(id, { quantidade, tipoItem, matricula }) {
        try {
            const response = await fetch(`${API_BASE_URL}/doacoes/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ quantidade, tipoItem, matricula })
            });
            if (!response.ok) {
                const errData = await response.json().catch(() => ({}));
                throw new Error(errData.erro || errData.message || 'Erro ao editar doação.');
            }
            const doacaoEditada = await response.json();
            return mapDoacao(doacaoEditada);
        } catch (error) {
            console.error(`Erro em DoacaoModel.editarDoacao(${id}):`, error);
            throw error;
        }
    }

    return { 
        getDoacoes, 
        buscarPorId,
        registrarDoacao,
        validarDoacao,
        deletar,
        editarDoacao,
        getTotalPendentes, 
        getTotalHoras 
    };
})();

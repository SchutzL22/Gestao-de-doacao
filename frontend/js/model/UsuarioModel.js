/* =====================================================
   COPER FMP | js/model/UsuarioModel.js
   Dados e regras de negócio da autenticação e usuários integrados ao backend.
   ===================================================== */

// API_BASE_URL definida em js/config.js (carregado antes de todos os models)

const UsuarioModel = (() => {

    function mapUsuario(user) {
        if (!user) return null;
        if (user.matricula !== undefined) {
            user.tipo = 'aluno';
            user.role = user.curso || 'Aluno de ADS';
        } else {
            user.tipo = 'admin';
            user.role = user.cargo || 'Administrador';
        }
        return user;
    }

    async function autenticar(email, senha) {
        try {
            const response = await fetch(`${API_BASE_URL}/usuarios/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, senha })
            });
            if (!response.ok) {
                const errData = await response.json().catch(() => ({}));
                throw new Error(errData.erro || errData.message || 'Credenciais inválidas.');
            }
            const user = await response.json();
            const usuarioMapeado = mapUsuario(user);
            // Persiste a sessão no localStorage após login bem-sucedido
            localStorage.setItem('coper_sessao', JSON.stringify(usuarioMapeado));
            return usuarioMapeado;
        } catch (error) {
            console.error("Erro em UsuarioModel.autenticar:", error);
            throw error;
        }
    }

    /** Retorna o usuário salvo no localStorage (ou null se não houver sessão). */
    function getSessaoSalva() {
        try {
            const raw = localStorage.getItem('coper_sessao');
            return raw ? JSON.parse(raw) : null;
        } catch {
            return null;
        }
    }

    /** Remove a sessão do localStorage e sessionStorage (logout). */
    function limparSessao() {
        localStorage.removeItem('coper_sessao');
        sessionStorage.removeItem('coper_sessao');
    }

    async function listarTodos() {
        try {
            const response = await fetch(`${API_BASE_URL}/usuarios`);
            if (!response.ok) {
                const errData = await response.json().catch(() => ({}));
                throw new Error(errData.erro || errData.message || 'Erro ao listar usuários.');
            }
            const list = await response.json();
            return list.map(mapUsuario);
        } catch (error) {
            console.error("Erro em UsuarioModel.listarTodos:", error);
            throw error;
        }
    }

    async function buscarPorId(id) {
        try {
            const response = await fetch(`${API_BASE_URL}/usuarios/${id}`);
            if (!response.ok) {
                const errData = await response.json().catch(() => ({}));
                throw new Error(errData.erro || errData.message || 'Erro ao buscar usuário por ID.');
            }
            const user = await response.json();
            return mapUsuario(user);
        } catch (error) {
            console.error(`Erro em UsuarioModel.buscarPorId(${id}):`, error);
            throw error;
        }
    }

    async function cadastrarAluno(aluno) {
        try {
            const response = await fetch(`${API_BASE_URL}/usuarios/alunos`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(aluno)
            });
            if (!response.ok) {
                const errData = await response.json().catch(() => ({}));
                throw new Error(errData.erro || errData.message || 'Erro ao cadastrar aluno.');
            }
            const novoAluno = await response.json();
            return mapUsuario(novoAluno);
        } catch (error) {
            console.error("Erro em UsuarioModel.cadastrarAluno:", error);
            throw error;
        }
    }

    async function cadastrarFuncionario(funcionario) {
        try {
            const response = await fetch(`${API_BASE_URL}/usuarios/funcionarios`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(funcionario)
            });
            if (!response.ok) {
                const errData = await response.json().catch(() => ({}));
                throw new Error(errData.erro || errData.message || 'Erro ao cadastrar funcionário.');
            }
            const novoFuncionario = await response.json();
            return mapUsuario(novoFuncionario);
        } catch (error) {
            console.error("Erro em UsuarioModel.cadastrarFuncionario:", error);
            throw error;
        }
    }

    async function alterarSenha(id, novaSenha) {
        try {
            const response = await fetch(`${API_BASE_URL}/usuarios/${id}/alterar-senha`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ novaSenha })
            });
            if (!response.ok) {
                const errData = await response.json().catch(() => ({}));
                throw new Error(errData.erro || errData.message || 'Erro ao alterar senha.');
            }
            const user = await response.json();
            return mapUsuario(user);
        } catch (error) {
            console.error(`Erro em UsuarioModel.alterarSenha(${id}):`, error);
            throw error;
        }
    }

    async function deletar(id) {
        try {
            const response = await fetch(`${API_BASE_URL}/usuarios/${id}`, {
                method: 'DELETE'
            });
            if (!response.ok) {
                const errData = await response.json().catch(() => ({}));
                throw new Error(errData.erro || errData.message || 'Erro ao deletar usuário.');
            }
        } catch (error) {
            console.error(`Erro em UsuarioModel.deletar(${id}):`, error);
            throw error;
        }
    }

    /**
     * Atualiza o perfil do usuário logado.
     * Envia PUT /usuarios/{id} com { nome, novaSenha }.
     * A senha só é alterada se 'novaSenha' vier preenchida.
     */
    async function atualizarPerfil(id, nome, novaSenha) {
        try {
            const payload = { nome };
            if (novaSenha && novaSenha.trim().length > 0) {
                payload.novaSenha = novaSenha.trim();
            }
            const response = await fetch(`${API_BASE_URL}/usuarios/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            if (!response.ok) {
                // Lê o campo 'erro' do JSON padronizado pelo GlobalExceptionHandler
                const errData = await response.json().catch(() => ({}));
                throw new Error(errData.erro || errData.message || 'Erro ao atualizar perfil.');
            }
            const user = await response.json();
            return mapUsuario(user);
        } catch (error) {
            console.error(`Erro em UsuarioModel.atualizarPerfil(${id}):`, error);
            throw error;
        }
    }

    /**
     * Busca um usuário (aluno) pelo e-mail — usado no fluxo "Esqueci a Senha".
     * Percorre a lista de alunos e retorna o primeiro com e-mail correspondente.
     * Retorna null se não encontrado (sem lançar exceção).
     */
    async function buscarPorEmail(email) {
        try {
            const response = await fetch(`${API_BASE_URL}/usuarios/alunos`);
            if (!response.ok) return null;
            const alunos = await response.json();
            const encontrado = alunos.find(a => a.email && a.email.toLowerCase() === email.toLowerCase());
            return encontrado ? mapUsuario(encontrado) : null;
        } catch {
            return null;
        }
    }

    return {
        autenticar,
        getSessaoSalva,
        limparSessao,
        atualizarPerfil,
        listarTodos,
        buscarPorId,
        buscarPorEmail,
        cadastrarAluno,
        cadastrarFuncionario,
        alterarSenha,
        deletar
    };
})();

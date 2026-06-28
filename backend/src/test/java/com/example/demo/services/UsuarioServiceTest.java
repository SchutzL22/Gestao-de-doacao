package com.example.demo.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.models.Aluno;
import com.example.demo.models.Funcionario;
import com.example.demo.models.Usuario;
import com.example.demo.repositories.AlunoRepository;
import com.example.demo.repositories.FuncionarioRepository;
import com.example.demo.repositories.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    public void testCadastrarAluno_Success() {
        Aluno aluno = new Aluno();
        aluno.setEmail("test@aluno.fmpsc.edu.br");
        aluno.setSenha("StrongPass123");

        when(usuarioRepository.existsByEmail("test@aluno.fmpsc.edu.br")).thenReturn(false);
        when(passwordEncoder.encode("StrongPass123")).thenReturn("hashedPassword");
        when(alunoRepository.save(any(Aluno.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Aluno saved = usuarioService.cadastrarAluno(aluno);
        assertNotNull(saved);
        assertEquals("hashedPassword", saved.getSenha());
        assertEquals(0.0, saved.getSaldoHoras());
        verify(alunoRepository, times(1)).save(aluno);
    }

    @Test
    public void testCadastrarAluno_InvalidEmail() {
        Aluno aluno = new Aluno();
        aluno.setEmail("test@gmail.com");
        aluno.setSenha("StrongPass123");

        assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.cadastrarAluno(aluno);
        });
    }

    @Test
    public void testCadastrarAluno_EmailDuplicated() {
        Aluno aluno = new Aluno();
        aluno.setEmail("test@aluno.fmpsc.edu.br");
        aluno.setSenha("StrongPass123");

        when(usuarioRepository.existsByEmail("test@aluno.fmpsc.edu.br")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.cadastrarAluno(aluno);
        });
    }

    @Test
    public void testCadastrarAluno_WeakPassword() {
        Aluno aluno = new Aluno();
        aluno.setEmail("test@aluno.fmpsc.edu.br");
        aluno.setSenha("weak");

        assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.cadastrarAluno(aluno);
        });
    }

    @Test
    public void testCadastrarFuncionario_Success() {
        Funcionario func = new Funcionario();
        func.setEmail("func@fmp.edu.br");
        func.setSenha("FuncPass123");

        when(usuarioRepository.existsByEmail("func@fmp.edu.br")).thenReturn(false);
        when(passwordEncoder.encode("FuncPass123")).thenReturn("hashedPass");
        when(funcionarioRepository.save(any(Funcionario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Funcionario saved = usuarioService.cadastrarFuncionario(func);
        assertNotNull(saved);
        assertEquals("hashedPass", saved.getSenha());
    }

    @Test
    public void testAutenticar_Success() {
        Aluno aluno = new Aluno();
        aluno.setId(1L);
        aluno.setEmail("test@aluno.fmpsc.edu.br");
        aluno.setSenha("hashedPassword");
        aluno.setTentativasFalhas(2);

        when(usuarioRepository.findByEmail("test@aluno.fmpsc.edu.br")).thenReturn(Optional.of(aluno));
        when(passwordEncoder.matches("StrongPass123", "hashedPassword")).thenReturn(true);
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario auth = usuarioService.autenticar("test@aluno.fmpsc.edu.br", "StrongPass123");
        assertNotNull(auth);
        assertEquals(0, auth.getTentativasFalhas());
        assertNull(auth.getBloqueadoAte());
    }

    @Test
    public void testAutenticar_Fail_LocksAccount() {
        Aluno aluno = new Aluno();
        aluno.setEmail("test@aluno.fmpsc.edu.br");
        aluno.setSenha("hashedPassword");
        aluno.setTentativasFalhas(2);

        when(usuarioRepository.findByEmail("test@aluno.fmpsc.edu.br")).thenReturn(Optional.of(aluno));
        when(passwordEncoder.matches("WrongPass", "hashedPassword")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.autenticar("test@aluno.fmpsc.edu.br", "WrongPass");
        });
        assertTrue(ex.getMessage().contains("temporariamente bloqueada"));
        assertNotNull(aluno.getBloqueadoAte());
    }

    @Test
    public void testAutenticar_ResetOnExpiredBlock() {
        Aluno aluno = new Aluno();
        aluno.setEmail("test@aluno.fmpsc.edu.br");
        aluno.setSenha("hashedPassword");
        aluno.setTentativasFalhas(3);
        aluno.setBloqueadoAte(LocalDateTime.now().minusMinutes(1)); // Block expired 1 min ago

        when(usuarioRepository.findByEmail("test@aluno.fmpsc.edu.br")).thenReturn(Optional.of(aluno));
        // Password is correct now
        when(passwordEncoder.matches("StrongPass123", "hashedPassword")).thenReturn(true);
        when(alunoRepository.findById(any())).thenReturn(Optional.of(aluno));
        
        Usuario auth = usuarioService.autenticar("test@aluno.fmpsc.edu.br", "StrongPass123");
        assertNotNull(auth);
        assertEquals(0, auth.getTentativasFalhas());
        assertNull(auth.getBloqueadoAte());
    }
}

package com.umbook.backend.service;

import com.umbook.backend.dto.LoginRequestDTO;
import com.umbook.backend.dto.RegistroRequestDTO;
import com.umbook.backend.dto.UsuarioResponseDTO;
import com.umbook.backend.exception.*;
import com.umbook.backend.model.Usuario;
import com.umbook.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private RegistroRequestDTO registroDTO;
    private Usuario usuarioGuardado;

    @BeforeEach
    void setUp() {
        registroDTO = new RegistroRequestDTO();
        registroDTO.setNombre("Juan");
        registroDTO.setApellido("Pérez");
        registroDTO.setEmail("juan@mail.com");
        registroDTO.setNombreUsuario("jperez");
        registroDTO.setContrasena("password123");
        registroDTO.setFechaNacimiento(LocalDate.of(2000, 1, 1));

        usuarioGuardado = new Usuario();
        usuarioGuardado.setId(1L);
        usuarioGuardado.setNombre("Juan");
        usuarioGuardado.setApellido("Pérez");
        usuarioGuardado.setEmail("juan@mail.com");
        usuarioGuardado.setNombreUsuario("jperez");
        usuarioGuardado.setContrasena("$2a$hash");
        usuarioGuardado.setFechaNacimiento(LocalDate.of(2000, 1, 1));
        usuarioGuardado.setActivo(true);
    }

    // ===================== UC-1: Registrarse =====================

    @Test
    @DisplayName("CP-20-01: Registro exitoso")
    void registrar_exitoso() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByNombreUsuario(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$hash");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

        UsuarioResponseDTO result = usuarioService.registrar(registroDTO);

        assertThat(result.getEmail()).isEqualTo("juan@mail.com");
        assertThat(result.getNombre()).isEqualTo("Juan");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("CP-20-04: Email duplicado impide el registro")
    void registrar_emailDuplicado_lanzaExcepcion() {
        when(usuarioRepository.existsByEmail("juan@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.registrar(registroDTO))
            .isInstanceOf(EmailDuplicadoException.class)
            .hasMessageContaining("juan@mail.com");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("CP-20-06: Nombre de usuario duplicado impide el registro")
    void registrar_nombreUsuarioDuplicado_lanzaExcepcion() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.existsByNombreUsuario("jperez")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.registrar(registroDTO))
            .isInstanceOf(NombreUsuarioDuplicadoException.class);

        verify(usuarioRepository, never()).save(any());
    }

    // ===================== UC-2: Iniciar Sesión =====================

    @Test
    @DisplayName("CP-21-01: Login exitoso")
    void login_exitoso() {
        LoginRequestDTO dto = loginDTO("juan@mail.com", "password123");
        when(usuarioRepository.findByEmail("juan@mail.com")).thenReturn(Optional.of(usuarioGuardado));
        when(passwordEncoder.matches("password123", "$2a$hash")).thenReturn(true);
        when(usuarioRepository.save(any())).thenReturn(usuarioGuardado);

        UsuarioResponseDTO result = usuarioService.iniciarSesion(dto);

        assertThat(result.getEmail()).isEqualTo("juan@mail.com");
    }

    @Test
    @DisplayName("CP-21-02: Contraseña incorrecta lanza excepción")
    void login_contraseniaIncorrecta_lanzaExcepcion() {
        LoginRequestDTO dto = loginDTO("juan@mail.com", "wrongpass");
        when(usuarioRepository.findByEmail("juan@mail.com")).thenReturn(Optional.of(usuarioGuardado));
        when(passwordEncoder.matches("wrongpass", "$2a$hash")).thenReturn(false);
        when(usuarioRepository.save(any())).thenReturn(usuarioGuardado);

        assertThatThrownBy(() -> usuarioService.iniciarSesion(dto))
            .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    @DisplayName("CP-21-03: Email no registrado lanza excepción")
    void login_emailNoRegistrado_lanzaExcepcion() {
        LoginRequestDTO dto = loginDTO("noexiste@mail.com", "password123");
        when(usuarioRepository.findByEmail("noexiste@mail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.iniciarSesion(dto))
            .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    @DisplayName("CP-21-07: Usuario deshabilitado no puede iniciar sesión")
    void login_usuarioDeshabilitado_lanzaExcepcion() {
        usuarioGuardado.setActivo(false);
        LoginRequestDTO dto = loginDTO("juan@mail.com", "password123");
        when(usuarioRepository.findByEmail("juan@mail.com")).thenReturn(Optional.of(usuarioGuardado));

        assertThatThrownBy(() -> usuarioService.iniciarSesion(dto))
            .isInstanceOf(UsuarioDeshabilitadoException.class);
    }

    @Test
    @DisplayName("CP-21-04 (Efecto): Usuario bloqueado no puede iniciar sesión")
    void login_usuarioBloqueado_lanzaExcepcion() {
        usuarioGuardado.setIntentosFallidos(10);
        usuarioGuardado.setBloqueadoHasta(LocalDateTime.now().plusMinutes(30));
        LoginRequestDTO dto = loginDTO("juan@mail.com", "password123");
        when(usuarioRepository.findByEmail("juan@mail.com")).thenReturn(Optional.of(usuarioGuardado));

        assertThatThrownBy(() -> usuarioService.iniciarSesion(dto))
            .isInstanceOf(UsuarioBloqueadoException.class);
    }

    @Test
    @DisplayName("CP-21-04: Al llegar a 10 intentos fallidos, se bloquea la cuenta")
    void login_10intentosFallidos_bloqueaCuenta() {
        usuarioGuardado.setIntentosFallidos(9);
        LoginRequestDTO dto = loginDTO("juan@mail.com", "wrongpass");
        when(usuarioRepository.findByEmail("juan@mail.com")).thenReturn(Optional.of(usuarioGuardado));
        when(passwordEncoder.matches("wrongpass", "$2a$hash")).thenReturn(false);
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> usuarioService.iniciarSesion(dto))
            .isInstanceOf(CredencialesInvalidasException.class);

        verify(usuarioRepository).save(argThat(u ->
            u.getIntentosFallidos() == 10 && u.getBloqueadoHasta() != null
        ));
    }

    // ===================== UC-7: Buscar Usuarios =====================

    @Test
    @DisplayName("CP-1.3-04: Búsqueda vacía lanza excepción")
    void buscar_criteriosVacios_lanzaExcepcion() {
        assertThatThrownBy(() -> usuarioService.buscarUsuarios(""))
            .isInstanceOf(BusquedaVaciaException.class);

        verify(usuarioRepository, never()).buscarPorTermino(any());
    }

    @Test
    @DisplayName("CP-1.3-01: Búsqueda exitosa por término")
    void buscar_porTermino_retornaResultados() {
        when(usuarioRepository.buscarPorTermino("Juan"))
            .thenReturn(List.of(usuarioGuardado));

        List<UsuarioResponseDTO> result = usuarioService.buscarUsuarios("Juan");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Juan");
    }

    @Test
    @DisplayName("CP-1.3-05: Búsqueda sin resultados retorna lista vacía")
    void buscar_sinResultados_retornaListaVacia() {
        when(usuarioRepository.buscarPorTermino("Zzzzzz"))
            .thenReturn(List.of());

        List<UsuarioResponseDTO> result = usuarioService.buscarUsuarios("Zzzzzz");

        assertThat(result).isEmpty();
    }

    private LoginRequestDTO loginDTO(String email, String contrasena) {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail(email);
        dto.setContrasena(contrasena);
        return dto;
    }
}

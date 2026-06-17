package com.umbook.backend.service;

import com.umbook.backend.dto.LoginRequestDTO;
import com.umbook.backend.dto.RegistroRequestDTO;
import com.umbook.backend.dto.UsuarioResponseDTO;
import com.umbook.backend.exception.*;
import com.umbook.backend.model.Usuario;
import com.umbook.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioService {

    private static final int MAX_INTENTOS = 10;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponseDTO registrar(RegistroRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new EmailDuplicadoException(dto.getEmail());
        }
        if (usuarioRepository.existsByNombreUsuario(dto.getNombreUsuario())) {
            throw new NombreUsuarioDuplicadoException(dto.getNombreUsuario());
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre().trim());
        usuario.setApellido(dto.getApellido().trim());
        usuario.setEmail(dto.getEmail().trim().toLowerCase());
        usuario.setNombreUsuario(dto.getNombreUsuario().trim());
        usuario.setContrasena(passwordEncoder.encode(dto.getContrasena()));
        usuario.setFechaNacimiento(dto.getFechaNacimiento());

        return UsuarioResponseDTO.from(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponseDTO iniciarSesion(LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail().trim().toLowerCase())
            .orElseThrow(CredencialesInvalidasException::new);

        if (!usuario.isActivo()) {
            throw new UsuarioDeshabilitadoException();
        }

        if (usuario.getBloqueadoHasta() != null
                && usuario.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
            throw new UsuarioBloqueadoException();
        }

        if (!passwordEncoder.matches(dto.getContrasena(), usuario.getContrasena())) {
            registrarIntentoFallido(usuario);
            throw new CredencialesInvalidasException();
        }

        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuarioRepository.save(usuario);

        return UsuarioResponseDTO.from(usuario);
    }

    public List<UsuarioResponseDTO> buscarUsuarios(String termino) {
        String q = termino != null ? termino.trim() : "";

        if (q.isEmpty()) {
            throw new BusquedaVaciaException();
        }

        return usuarioRepository.buscarPorTermino(q)
            .stream()
            .map(UsuarioResponseDTO::from)
            .toList();
    }

    private void registrarIntentoFallido(Usuario usuario) {
        int intentos = usuario.getIntentosFallidos() + 1;
        usuario.setIntentosFallidos(intentos);
        if (intentos >= MAX_INTENTOS) {
            usuario.setBloqueadoHasta(LocalDateTime.now().plusHours(1));
        }
        usuarioRepository.save(usuario);
    }
}

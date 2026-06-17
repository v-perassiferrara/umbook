package com.umbook.backend.controller;

import com.umbook.backend.dto.LoginRequestDTO;
import com.umbook.backend.dto.RegistroRequestDTO;
import com.umbook.backend.dto.UsuarioResponseDTO;
import com.umbook.backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // UC-1: Registrarse
    @PostMapping("/registrar")
    public ResponseEntity<UsuarioResponseDTO> registrar(@Valid @RequestBody RegistroRequestDTO dto) {
        UsuarioResponseDTO response = usuarioService.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // UC-2: Iniciar Sesión
    @PostMapping("/login")
    public ResponseEntity<UsuarioResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        UsuarioResponseDTO response = usuarioService.iniciarSesion(dto);
        return ResponseEntity.ok(response);
    }

    // UC-7: Buscar Usuarios
    @GetMapping("/buscar")
    public ResponseEntity<List<UsuarioResponseDTO>> buscar(
            @RequestParam(defaultValue = "") String q) {
        List<UsuarioResponseDTO> resultados = usuarioService.buscarUsuarios(q);
        return ResponseEntity.ok(resultados);
    }
}

package com.umbook.backend.dto;

import com.umbook.backend.model.Usuario;
import java.time.LocalDate;

public class UsuarioResponseDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String nombreUsuario;
    private LocalDate fechaNacimiento;
    private boolean activo;

    public static UsuarioResponseDTO from(Usuario u) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.id = u.getId();
        dto.nombre = u.getNombre();
        dto.apellido = u.getApellido();
        dto.email = u.getEmail();
        dto.nombreUsuario = u.getNombreUsuario();
        dto.fechaNacimiento = u.getFechaNacimiento();
        dto.activo = u.isActivo();
        return dto;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getEmail() { return email; }
    public String getNombreUsuario() { return nombreUsuario; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public boolean isActivo() { return activo; }
}

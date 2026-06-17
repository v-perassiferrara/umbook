package com.umbook.backend.repository;

import com.umbook.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    boolean existsByEmail(String email);

    boolean existsByNombreUsuario(String nombreUsuario);

    @Query("""
        SELECT u FROM Usuario u
        WHERE u.activo = true
          AND (
            LOWER(u.nombre) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(u.apellido) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(u.nombreUsuario) LIKE LOWER(CONCAT('%', :q, '%'))
          )
        ORDER BY u.apellido, u.nombre
        """)
    List<Usuario> buscarPorTermino(@Param("q") String q);
}

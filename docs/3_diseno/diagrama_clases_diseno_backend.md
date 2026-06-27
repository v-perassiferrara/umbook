# Diagrama de Clases de Diseño — Backend (Spring Boot)

> **Clases implementadas** (con detalle completo): `Usuario`, `UsuarioController`, `UsuarioService`, `UsuarioRepository` y los tres DTOs.
> **Entidades no implementadas**: se incluyen con sus atributos del modelo de dominio; verificar contra el diagrama de análisis original.

```mermaid
classDiagram
    direction TB

    %% ─────────────────────────────────────────────
    %% CAPA IMPLEMENTADA — Usuario
    %% ─────────────────────────────────────────────

    class UsuarioController {
        <<Controller, Singleton>>
        +registrar(dto: RegistroRequestDTO) ResponseEntity~UsuarioResponseDTO~
        +login(dto: LoginRequestDTO) ResponseEntity~UsuarioResponseDTO~
        +buscar(q: String) ResponseEntity~List~UsuarioResponseDTO~~
    }

    class UsuarioService {
        <<Service, Singleton>>
        +registrar(dto: RegistroRequestDTO) UsuarioResponseDTO
        +iniciarSesion(dto: LoginRequestDTO) UsuarioResponseDTO
        +buscarUsuarios(termino: String) List~UsuarioResponseDTO~
        -registrarIntentoFallido(usuario: Usuario) void
    }

    class UsuarioRepository {
        <<Repository, Singleton>>
        +findByEmail(email: String) Optional~Usuario~
        +findByNombreUsuario(nombreUsuario: String) Optional~Usuario~
        +existsByEmail(email: String) boolean
        +existsByNombreUsuario(nombreUsuario: String) boolean
        +buscarPorTermino(q: String) List~Usuario~
    }

    class Usuario {
        <<Entity>>
        -Long id
        -String nombre
        -String apellido
        -String email
        -String nombreUsuario
        -String contrasena
        -LocalDate fechaNacimiento
        -boolean activo
        -int intentosFallidos
        -LocalDateTime bloqueadoHasta
    }

    class RegistroRequestDTO {
        <<DTO>>
        -String nombre
        -String apellido
        -String email
        -String nombreUsuario
        -String contrasena
        -LocalDate fechaNacimiento
    }

    class LoginRequestDTO {
        <<DTO>>
        -String email
        -String contrasena
    }

    class UsuarioResponseDTO {
        <<DTO>>
        -Long id
        -String nombre
        -String apellido
        -String email
        -String nombreUsuario
        -LocalDate fechaNacimiento
        -boolean activo
        +UsuarioResponseDTO from(Usuario u)$
    }

    UsuarioController --> UsuarioService
    UsuarioService --> UsuarioRepository
    UsuarioController ..> RegistroRequestDTO
    UsuarioController ..> LoginRequestDTO
    UsuarioController ..> UsuarioResponseDTO
    UsuarioResponseDTO ..> Usuario

    %% ─────────────────────────────────────────────
    %% ENTIDADES DE DOMINIO (no implementadas)
    %% ─────────────────────────────────────────────

    class Muro {
        <<Entity>>
        -Long id
    }

    class Foto {
        <<Entity>>
        -Long id
        -String descripcion
        -String urlImagen
        -LocalDateTime fechaSubida
    }

    class Album {
        <<Entity>>
        -Long id
        -String nombre
        -String descripcion
    }

    class Comentario {
        <<Entity>>
        -Long id
        -String contenido
        -LocalDateTime fechaCreacion
    }

    class Grupo {
        <<Entity>>
        -Long id
        -String nombre
        -String descripcion
    }

    class SolicitudAmistad {
        <<Entity>>
        -Long id
        -LocalDateTime fechaEnvio
        -String estado
    }

    class Amistad {
        <<Entity>>
        -Long id
        -LocalDate fechaCreacion
    }

    class Notificacion {
        <<Entity>>
        -Long id
        -String mensaje
        -LocalDateTime fecha
        -boolean leida
    }

    class Mensaje {
        <<Entity>>
        -Long id
        -String contenido
        -LocalDateTime fechaEnvio
    }

    class Chat {
        <<Entity>>
        -Long id
    }

    class Admin {
        <<Entity>>
        -Long id
    }

    %% ─────────────────────────────────────────────
    %% RELACIONES ENTRE ENTIDADES DE DOMINIO
    %% ─────────────────────────────────────────────

    Usuario "1" --> "1" Muro
    Usuario "1" --> "*" Foto
    Foto "*" --> "1" Album
    Foto "1" --> "*" Comentario
    Usuario "1" --> "*" Comentario
    Usuario "invitador 1" --> "*" SolicitudAmistad
    Usuario "invitado 1" --> "*" SolicitudAmistad
    Usuario "*" --> "*" Amistad
    Usuario "*" --> "*" Grupo
    Grupo "1" --> "*" Chat
    Chat "1" --> "*" Mensaje
    Usuario "1" --> "*" Notificacion
    Admin "1" --> "*" Usuario
```

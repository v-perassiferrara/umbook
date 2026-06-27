# UMBook — Implementación

Implementación del examen final de Ingeniería de Software (RUP).  
Cubre los casos de uso **UC-1 Registrarse**, **UC-2 Iniciar Sesión** y **UC-7 Buscar Usuarios**.

---

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Spring Boot 3.4.3 (Java 21) |
| Frontend | Angular 18 (TypeScript) |
| Base de datos | MySQL 8.4 |
| ORM | JPA / Hibernate |
| Seguridad | Spring Security + BCrypt |
| Comunicación | REST API (JSON) |

---

## Estructura del proyecto

```
umbook/
├── umbook-backend/       <- Spring Boot
│   └── src/main/java/com/umbook/backend/
│       ├── config/       <- SecurityConfig (CORS + BCrypt)
│       ├── controller/   <- UsuarioController
│       ├── dto/          <- RegistroRequestDTO, LoginRequestDTO, UsuarioResponseDTO
│       ├── exception/    <- Excepciones personalizadas + GlobalExceptionHandler
│       ├── model/        <- Usuario (entidad JPA)
│       ├── repository/   <- UsuarioRepository (JPA + JPQL)
│       └── service/      <- UsuarioService (lógica de negocio)
│
└── umbook-frontend/      <- Angular 18
    └── src/app/
        ├── components/
        │   ├── login/        <- UC-2: Iniciar Sesión
        │   ├── register/     <- UC-1: Registrarse
        │   └── dashboard/    <- UC-7: Buscar Usuarios
        ├── services/     <- UserService (llamadas HTTP)
        ├── guards/       <- AuthGuard (protege /dashboard)
        └── models/       <- Interface Usuario
```

---

## Correr el proyecto — Docker

### Requisito

- Docker instalado y funcionando.

### Levantar todo

Desde la carpeta `umbook/` (donde está el `docker-compose.yml`):

```bash
docker compose up --build
```

Esto:
1. Descarga la imagen de MySQL 8.4
2. Compila el backend (Maven dentro del container)
3. Compila el frontend (Angular production build dentro del container)
4. Levanta los tres servicios en orden

La primera vez tarda varios minutos por las descargas y compilaciones. Las siguientes veces es más rápido.

Cuando termine, abrir `http://localhost` en el navegador.

### Apagar (mantiene los datos)

```bash
docker compose down
```

### Apagar y borrar todo (datos incluidos)

```bash
docker compose down -v
```

La próxima vez que se haga `docker compose up`, la base de datos arranca vacía.

### Reconstruir imágenes (si se cambia código)

```bash
docker compose up --build
```

---

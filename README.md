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

## Endpoints REST

| Método | Endpoint | Caso de uso | Descripción |
|---|---|---|---|
| `POST` | `/api/usuarios/registrar` | UC-1 | Crea una cuenta nueva |
| `POST` | `/api/usuarios/login` | UC-2 | Autentica al usuario |
| `GET` | `/api/usuarios/buscar?q=` | UC-7 | Busca usuarios activos por nombre o apellido |

---

## Flujo de demo

```
1. Ir a http://localhost/      ->  redirige a /login
2. Click en "Registrate"       ->  completar el formulario y crear cuenta
3. Iniciar sesión              ->  con el email y contraseña registrados
4. Dashboard                   ->  buscar usuarios por nombre y/o apellido
5. Cerrar sesión               ->  botón en la barra superior
```

---

## Reglas de negocio implementadas

- La contraseña debe tener **mínimo 8 caracteres** (validado en frontend y backend).
- El email y el nombre de usuario deben ser **únicos**.
- Tras **10 intentos fallidos** de login, la cuenta queda bloqueada **1 hora**.
- Los usuarios **deshabilitados** no pueden iniciar sesión ni aparecen en los resultados de búsqueda.
- La búsqueda requiere **al menos un criterio** (nombre o apellido).
- Las contraseñas se almacenan **hasheadas con BCrypt** — nunca en texto plano.

---

## Tests unitarios

No requieren MySQL (usan Mockito).

```bash
cd umbook-backend
mvn test
```

**12 tests** cubriendo los casos de prueba documentados (CP-20, CP-21, CP-1.3):

| Test | CP documentado |
|---|---|
| Registro exitoso | CP-20-01 |
| Email duplicado rechazado | CP-20-04 |
| Nombre de usuario duplicado rechazado | — |
| Login exitoso | CP-21-01 |
| Contraseña incorrecta rechazada | CP-21-02 |
| Email no registrado rechazado | CP-21-03 |
| Usuario deshabilitado rechazado | — |
| Usuario bloqueado rechazado | — |
| Bloqueo al llegar a 10 intentos | CP-21-04 (adaptado a 10) |
| Búsqueda vacía rechazada | CP-1.3-04 |
| Búsqueda por nombre exitosa | CP-1.3-01 |
| Búsqueda sin resultados retorna lista vacía | CP-1.3-05 |

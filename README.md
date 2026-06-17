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
4) Implementación/
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

## Formas de correr el proyecto

Hay dos opciones: **Docker** (recomendado, no requiere instalar nada más que Docker) o **local** (requiere Java, Maven, Node y MySQL instalados).

---

## Opción A — Docker (recomendado)

### Requisito

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado y corriendo.

### Levantar todo

Desde la carpeta `4) Implementación/` (donde está el `docker-compose.yml`):

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

## Opción B — Local (sin Docker)

### Requisitos

- Java 21
- Maven 3.9+
- Node.js 22+
- MySQL 8.4 instalado localmente

---

## Cómo correr el proyecto (Opción B — local)

Abrir **tres terminales**. Cada una corre una cosa.

---

### Terminal 1 — MySQL

Iniciar el servidor de MySQL (por ejemplo, como servicio o ejecutando el daemon):
```bash
sudo systemctl start mysql
```
o:
```bash
mysqld
```

Verificar que esté activo (esperar unos segundos antes):
```bash
mysql -u root -proot -e "SELECT 1;"
```

Si responde con `1`, MySQL está listo. Si dice `Can't connect`, esperar unos segundos y reintentar.

La base de datos `umbook_db` y la tabla `usuarios` se crean automáticamente cuando el backend inicia por primera vez.

---

### Terminal 2 — Backend (Spring Boot)

```bash
cd umbook-backend
mvn spring-boot:run
```

Esperar hasta ver en consola:

```
Started UmBookApplication in X.XXX seconds
```

Queda disponible en `http://localhost:8080`.

Si falla con **"Port 8080 already in use"**, hay un proceso Java anterior colgado. Matarlo y reintentar:
```bash
kill -9 $(lsof -t -i:8080)
```

---

### Terminal 3 — Frontend (Angular)

```bash
cd umbook-frontend
npx @angular/cli@18 serve
```

Esperar hasta ver:

```
Application bundle generation complete.
Local:   http://localhost:4200/
```

Abrir `http://localhost:4200` en el navegador.

> Usar siempre `npx @angular/cli@18` con la versión explícita. `@angular/cli@latest` puede requerir una versión de Node más reciente y fallar.

---

## Orden de inicio obligatorio

```
1. MySQL    ->  esperar hasta que SELECT 1; devuelva resultado
2. Backend  ->  esperar hasta "Started UmBookApplication"
3. Frontend ->  abrir http://localhost:4200
```

No iniciar el backend antes de que MySQL esté activo o fallará con error de conexión.

---

## Apagar todo y resetear desde cero

### Apagar

**Matar el backend y el frontend:** `Ctrl+C` en cada terminal.

**Matar MySQL:**
Si se inició como servicio:
```bash
sudo systemctl stop mysql
```
Si se inició como daemon:
```bash
killall mysqld
```

Verificar que no quede nada corriendo:
```bash
mysql -u root -proot -e "SELECT 1;"
```
Debe decir `Can't connect` si MySQL se apagó bien.

---

### Resetear la base de datos (borrar todos los datos)

Con MySQL activo, conectarse y borrar la base:
```bash
mysql -u root -proot -e "DROP DATABASE IF EXISTS umbook_db;"
```

La próxima vez que se levante el backend, `umbook_db` y la tabla `usuarios` se recrean automáticamente vacías.

---

### Empezar desde cero como si clonaran el repo

1. Apagar todo (ver arriba)
2. Borrar la base de datos (ver arriba)
3. Arrancar MySQL, backend y frontend en ese orden (ver "Cómo correr el proyecto")

No hace falta tocar ningún archivo de código. El backend recrea el schema solo.

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
1. Ir a http://localhost:4200  ->  redirige a /login
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

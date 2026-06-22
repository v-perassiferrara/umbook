# Diagrama de Secuencia de Diseño: CU-2 Iniciar Sesión ()

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant UI as LoginComponent (Angular)
    participant C as UsuarioController (Spring)
    participant S as UsuarioService (Spring)
    participant R as UsuarioRepository (JPA)

    %% Camino Básico
    Note over U, R: Camino Básico - Inicio de Sesión Exitoso
    U ->> UI: ingresarDatos(email, contrasena)
    Note over UI: checkBloqueo() en localStorage<br/>(isBlocked == false)
    UI ->> C: POST /api/usuarios/login (LoginRequestDTO)
    C ->> S: iniciarSesion(LoginRequestDTO)
    
    S ->> R: findByEmail(email)
    R -->> S: Optional.of(Usuario)
    Note over S: Verifica activo == true &<br/>bloqueadoHasta no vencido
    
    Note over S: passwordEncoder.matches() == true
    S ->> S: resetear intentos y bloqueo (0, null)
    S ->> R: save(Usuario)
    R -->> S: Usuario (guardado)
    S -->> C: UsuarioResponseDTO
    C -->> UI: 200 OK (UsuarioResponseDTO)
    Note over UI: limpia contadores de localStorage<br/>guarda sesión y navega a /dashboard
    UI -->> U: redirigeADashboard()

    %% Alternativa 2.1 - Bloqueo previo en el cliente
    Note over U, UI: Alternativa 2.1 - Bloqueo Previo en Cliente (localStorage)
    U ->> UI: ingresarDatos(email, contrasena)
    Note over UI: checkBloqueo() == true<br/>(bloqueado por intentos locales)
    UI -->> U: mostrarError("Tu acceso ha sido bloqueado temporalmente...")<br/>(No se envía llamada HTTP al backend)

    %% Alternativa 3.1 - Credenciales Inválidas (Incremento intentos)
    Note over U, R: Alternativa 3.1 - Credenciales Inválidas (Intento < 10)
    U ->> UI: ingresarDatos(email, contrasena_incorrecta)
    UI ->> C: POST /api/usuarios/login (LoginRequestDTO)
    C ->> S: iniciarSesion(LoginRequestDTO)
    S ->> R: findByEmail(email)
    R -->> S: Optional.of(Usuario)
    Note over S: passwordEncoder.matches() == false
    S ->> S: registrarIntentoFallido() (intentos = intentos + 1)
    S ->> R: save(Usuario) (persiste el intento sin rollback)
    Note over S: Lanza CredencialesInvalidasException
    S -->> C: Exception interceptada
    Note over C: GlobalExceptionHandler captura error
    C -->> UI: 401 Unauthorized ("Las credenciales ingresadas no son válidas.")
    Note over UI: Incrementa contador en localStorage
    UI -->> U: mostrarError(mensaje)

    %% Alternativa 3.2 - Bloqueo de Cuenta en el Servidor (Intento 10)
    Note over U, R: Alternativa 3.2 - Bloqueo de Cuenta (Intento fallido 10)
    U ->> UI: ingresarDatos(email, contrasena_incorrecta)
    UI ->> C: POST /api/usuarios/login (LoginRequestDTO)
    C ->> S: iniciarSesion(LoginRequestDTO)
    S ->> R: findByEmail(email)
    R -->> S: Optional.of(Usuario)
    Note over S: passwordEncoder.matches() == false
    S ->> S: registrarIntentoFallido() (intentos = 10)<br/>establece bloqueadoHasta = now + 1 hora
    S ->> R: save(Usuario)
    Note over S: Lanza CredencialesInvalidasException
    S -->> C: Exception interceptada
    C -->> UI: 401 Unauthorized
    Note over UI: Incrementa local a 10<br/>guarda blocked_until en localStorage
    UI -->> U: mostrarError("Tu acceso ha sido bloqueado temporalmente...")

    %% Alternativa 3.3 - Cuenta ya bloqueada en Servidor
    Note over U, R: Alternativa 3.3 - Intento en cuenta bloqueada en Servidor
    U ->> UI: ingresarDatos(email, contrasena)
    UI ->> C: POST /api/usuarios/login (LoginRequestDTO)
    C ->> S: iniciarSesion(LoginRequestDTO)
    S ->> R: findByEmail(email)
    R -->> S: Optional.of(Usuario)
    Note over S: Verifica bloqueadoHasta != null &<br/>isAfter(LocalDateTime.now())
    Note over S: Lanza UsuarioBloqueadoException
    S -->> C: Exception interceptada
    Note over C: GlobalExceptionHandler captura error
    C -->> UI: 429 Too Many Requests ("Tu cuenta fue bloqueada...")
    UI -->> U: mostrarError(mensaje)
```

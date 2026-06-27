# Diagrama de Secuencia de Diseño: CU-1 Registrarse (v2)

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant UI as RegisterComponent (Angular)
    participant SVC as UserService (Angular)
    participant C as UsuarioController (Spring)
    participant S as UsuarioService (Spring)
    participant R as UsuarioRepository (JPA)

    %% Camino Básico
    Note over U, R: Camino Básico - Registro Exitoso
    U ->> UI: ingresarDatos(nombre, apellido, email, nombreUsuario, contrasena, fechaNacimiento)
    Note over UI: form.markAllAsTouched()<br/>form.invalid == false → continúa
    UI ->> SVC: registrar(form.value)
    SVC ->> C: POST /api/usuarios/registrar (RegistroRequestDTO)
    Note over C: Validación declarativa (@Valid)<br/>email válido & contrasena >= 8 chars
    C ->> S: registrar(RegistroRequestDTO)
    S ->> R: existsByEmail(email)
    R -->> S: false
    S ->> R: existsByNombreUsuario(nombreUsuario)
    R -->> S: false
    Note over S: passwordEncoder.encode(contrasena) → hash BCrypt
    S ->> R: save(Usuario)
    R -->> S: Usuario persistido con id generado
    S -->> C: UsuarioResponseDTO
    C -->> SVC: 201 Created: UsuarioResponseDTO
    SVC -->> UI: Observable: usuario
    UI -->> U: successMsg visible → redirige a /login tras 1.8s

    %% Alternativa 1.1 - Datos inválidos en el formulario (frontend)
    Note over U, UI: Alternativa 1.1 - Datos Inválidos (Validación de Formulario Angular)
    U ->> UI: ingresarDatos(datos inválidos)
    Note over UI: form.markAllAsTouched()<br/>form.invalid == true → return<br/>(no se envía llamada HTTP)
    UI -->> U: muestra errores de campo: required, minLength, email

    %% Alternativa 1.2 - Email duplicado
    Note over U, R: Alternativa 1.2 - Email Duplicado
    U ->> UI: ingresarDatos(email ya registrado)
    Note over UI: form.invalid == false → continúa
    UI ->> SVC: registrar(form.value)
    SVC ->> C: POST /api/usuarios/registrar (RegistroRequestDTO)
    C ->> S: registrar(RegistroRequestDTO)
    S ->> R: existsByEmail(email)
    R -->> S: true
    Note over S: Lanza EmailDuplicadoException
    S -->> C: EmailDuplicadoException interceptada
    Note over C: GlobalExceptionHandler.handleEmailDuplicado()
    C -->> SVC: 409 Conflict: El email ya se encuentra registrado.
    SVC -->> UI: Observable: error
    UI -->> U: errorMsg = err.error

    %% Alternativa 1.3 - Nombre de usuario duplicado
    Note over U, R: Alternativa 1.3 - Nombre de Usuario Duplicado
    U ->> UI: ingresarDatos(nombreUsuario ya registrado)
    Note over UI: form.invalid == false → continúa
    UI ->> SVC: registrar(form.value)
    SVC ->> C: POST /api/usuarios/registrar (RegistroRequestDTO)
    C ->> S: registrar(RegistroRequestDTO)
    S ->> R: existsByEmail(email)
    R -->> S: false
    S ->> R: existsByNombreUsuario(nombreUsuario)
    R -->> S: true
    Note over S: Lanza NombreUsuarioDuplicadoException
    S -->> C: NombreUsuarioDuplicadoException interceptada
    Note over C: GlobalExceptionHandler.handleNombreUsuarioDuplicado()
    C -->> SVC: 409 Conflict: El nombre de usuario ya se encuentra registrado.
    SVC -->> UI: Observable: error
    UI -->> U: errorMsg = err.error
```

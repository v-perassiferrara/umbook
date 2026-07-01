# Diagrama de Secuencia de Diseño: CU-1 Registrarse

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant UI as RegisterComponent (Angular)
    participant SVC as UserService (Angular)
    participant C as <<singleton>><br/>UsuarioController (Spring)
    participant S as <<singleton>><br/>UsuarioService (Spring)
    participant R as <<singleton>><br/>UsuarioRepository (JPA)

    %% Camino Básico
    Note over U, R: Camino Básico - Registro Exitoso
    U ->> UI: clicks submit / llama onSubmit()
    Note over UI: form.markAllAsTouched()<br/>form.invalid == false → continúa
    Note over UI: loading = true<br/>errorMsg = ''<br/>successMsg = ''
    UI ->> SVC: registrar(this.form.value)
    SVC ->> C: POST /api/usuarios/registrar (RegistroRequestDTO)
    Note over C: Validación declarativa (@Valid)<br/>email válido & contrasena >= 8 chars
    C ->> S: registrar(RegistroRequestDTO)
    S ->> R: existsByEmail(email)
    R -->> S: false
    S ->> R: existsByNombreUsuario(nombreUsuario)
    R -->> S: false
    Note over S: passwordEncoder.encode(contrasena) → hash BCrypt
    S ->> R: save(Usuario)
    R -->> S: Usuario
    S -->> C: UsuarioResponseDTO
    C -->> SVC: 201 Created: UsuarioResponseDTO
    SVC -->> UI: Observable: next()
    Note over UI: successMsg = 'Cuenta creada exitosamente...'
    UI -->> U: successMsg visible → redirige a /login tras 1.8s (1800ms)

    %% Alternativa 1.1 - Datos inválidos en el formulario (frontend)
    Note over U, UI: Alternativa 1.1 - Datos Inválidos (Validación de Formulario Angular)
    U ->> UI: clicks submit / llama onSubmit()
    Note over UI: form.markAllAsTouched()<br/>form.invalid == true → return<br/>(no se altera loading ni se envía HTTP)
    UI -->> U: muestra errores de campo (esInvalido)

    %% Alternativa 1.2 - Email duplicado
    Note over U, R: Alternativa 1.2 - Email Duplicado
    U ->> UI: clicks submit / llama onSubmit()
    Note over UI: form.invalid == false → continúa<br/>loading = true, errorMsg = '', successMsg = ''
    UI ->> SVC: registrar(this.form.value)
    SVC ->> C: POST /api/usuarios/registrar (RegistroRequestDTO)
    C ->> S: registrar(RegistroRequestDTO)
    S ->> R: existsByEmail(email)
    R -->> S: true
    Note over S: Lanza EmailDuplicadoException
    S -->> C: EmailDuplicadoException
    Note over C: GlobalExceptionHandler.handleEmailDuplicado()
    C -->> SVC: 409 Conflict: El email '<email>' ya se encuentra registrado.
    SVC -->> UI: Observable: error(err)
    Note over UI: errorMsg = err.error || 'Error al...'<br/>loading = false
    UI -->> U: errorMsg visible

    %% Alternativa 1.3 - Nombre de usuario duplicado
    Note over U, R: Alternativa 1.3 - Nombre de Usuario Duplicado
    U ->> UI: clicks submit / llama onSubmit()
    Note over UI: form.invalid == false → continúa<br/>loading = true, errorMsg = '', successMsg = ''
    UI ->> SVC: registrar(this.form.value)
    SVC ->> C: POST /api/usuarios/registrar (RegistroRequestDTO)
    C ->> S: registrar(RegistroRequestDTO)
    S ->> R: existsByEmail(email)
    R -->> S: false
    S ->> R: existsByNombreUsuario(nombreUsuario)
    R -->> S: true
    Note over S: Lanza NombreUsuarioDuplicadoException
    S -->> C: NombreUsuarioDuplicadoException
    Note over C: GlobalExceptionHandler.handleNombreUsuarioDuplicado()
    C -->> SVC: 409 Conflict: El nombre de usuario '<nombreUsuario>' ya está en uso.
    SVC -->> UI: Observable: error(err)
    Note over UI: errorMsg = err.error || 'Error al...'<br/>loading = false
    UI -->> U: errorMsg visible
```

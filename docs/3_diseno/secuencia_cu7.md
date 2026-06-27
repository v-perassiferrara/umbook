# Diagrama de Secuencia de Diseño: CU-7 Buscar Usuarios

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario
    participant UI as DashboardComponent (Angular)
    participant SVC as UserService (Angular)
    participant C as UsuarioController (Spring)
    participant S as UsuarioService (Spring)
    participant R as UsuarioRepository (JPA)

    %% Camino Básico
    Note over U, R: Camino Básico - Búsqueda Exitosa
    U ->> UI: ingresarTermino(termino) e iniciar búsqueda
    Note over UI: termino.trim() no vacío → continúa
    UI ->> SVC: buscar(termino)
    SVC ->> C: GET /api/usuarios/buscar?q=termino
    C ->> S: buscarUsuarios(termino)
    Note over S: q = termino.trim()<br/>q no vacío → continúa
    S ->> R: buscarPorTermino(q)
    Note over R: JPQL: busca coincidencias (LIKE %q%)<br/>en nombre, apellido, nombreUsuario<br/>filtra activo = true<br/>ordena por apellido, nombre
    R -->> S: List~Usuario~
    S -->> C: List~UsuarioResponseDTO~
    C -->> SVC: 200 OK: List~UsuarioResponseDTO~
    SVC -->> UI: Observable: usuarios
    Note over UI: resultados.length > 0
    UI -->> U: renderiza tarjetas: avatar, nombre, nombreUsuario, botón Enviar solicitud

    %% Alternativa 1.1 - Campo de búsqueda vacío (frontend)
    Note over U, UI: Alternativa 1.1 - Búsqueda con Campo Vacío (Validación Frontend)
    U ->> UI: iniciar búsqueda con campo vacío
    Note over UI: termino.trim() == "" → return<br/>(no se llama a UserService)
    UI -->> U: errorMsg: Debe ingresar un nombre o apellido para buscar.

    %% Alternativa 2.1 - Búsqueda sin resultados
    Note over U, R: Alternativa 2.1 - Búsqueda sin Resultados
    U ->> UI: ingresarTermino("Zzzzzz") e iniciar búsqueda
    UI ->> SVC: buscar("Zzzzzz")
    SVC ->> C: GET /api/usuarios/buscar?q=Zzzzzz
    C ->> S: buscarUsuarios("Zzzzzz")
    S ->> R: buscarPorTermino("Zzzzzz")
    R -->> S: List~Usuario~ vacía
    S -->> C: List~UsuarioResponseDTO~ vacía
    C -->> SVC: 200 OK: lista vacía
    SVC -->> UI: Observable: lista vacía
    Note over UI: resultados.length == 0<br/>sinResultados = true
    UI -->> U: No se encontraron usuarios con el criterio ingresado.
```

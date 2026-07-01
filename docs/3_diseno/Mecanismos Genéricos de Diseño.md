# **Mecanismos Genéricos de Diseño — UMBook**
## **1. Persistencia**
**Problemática:** Las entidades del sistema necesitan ser almacenadas, recuperadas y modificadas de forma persistente entre sesiones.
**Solución:** Se utiliza **Hibernate** como ORM (Object-Relational Mapping) junto con **JPA** (Java Persistence API). Cada entidad del dominio se anota con @Entity y se mapea a una tabla en **MySQL**. Para el acceso a datos, cada entidad tiene una interfaz Repository que extiende JpaRepository<Entidad, Long>, y Spring genera la implementación automáticamente en tiempo de ejecución.
**Ejemplo:** UsuarioRepository extends JpaRepository<Usuario, Long> permite guardar, buscar y eliminar usuarios sin escribir SQL manualmente.

## **2. Seguridad y Autenticación**
**Problemática:** El sistema debe garantizar que las contraseñas de los usuarios se almacenen de forma segura y que el backend acepte peticiones únicamente desde orígenes permitidos.
**Solución:** Se utiliza **Spring Security** para la configuración de seguridad. Las contraseñas se almacenan hasheadas mediante **BCryptPasswordEncoder**, de modo que nunca se persisten en texto plano. Además, se configura **CORS** (Cross-Origin Resource Sharing) para que el backend solo acepte peticiones desde el frontend Angular (http://localhost:4200).
**Ejemplo:** Al registrarse, UsuarioService llama a passwordEncoder.encode(contrasena) para hashear la contraseña antes de persistirla. Al iniciar sesión, se utiliza passwordEncoder.matches(contrasena, hash) para verificar las credenciales sin necesidad de decodificar el hash.

## **3. Distribución Transparente**
**Problemática:** El sistema está distribuido en múltiples nodos (Browser, Servidor Web, Servidor de Aplicación, Base de Datos) y las capas deben comunicarse de forma transparente.
**Solución:** La comunicación entre el frontend (**Angular**) y el backend (**Spring MVC**) se realiza mediante una **API REST** con HTTP/HTTPS. Spring MVC gestiona el ruteo de requests y la serialización/deserialización de objetos Java a JSON automáticamente. La comunicación entre el Servidor de Aplicación y la Base de Datos se realiza mediante **JDBC/Hibernate** sobre **TCP/IP**.
**Ejemplo:** Cuando Angular llama a GET /api/usuarios/buscar?q=X, Spring MVC rutea el request a UsuarioController.buscar() y serializa el List<UsuarioResponseDTO> de respuesta a JSON automáticamente.

## **4. Manejo de Errores**
**Problemática:** El sistema debe manejar situaciones excepcionales de forma controlada, informando al usuario de manera clara sin exponer detalles internos del sistema.
**Solución:** Se utilizan **excepciones personalizadas** para cada situación de error (ej: EmailDuplicadoException, CredencialesInvalidasException, UsuarioBloqueadoException). Spring provee @RestControllerAdvice como mecanismo centralizado para capturar estas excepciones y transformarlas en respuestas HTTP con el código de estado correspondiente (409 Conflict, 401 Unauthorized, 403 Forbidden, 429 Too Many Requests, etc.).
**Ejemplo:** Si al registrarse el email ya existe, UsuarioService lanza EmailDuplicadoException, que es capturada por el GlobalExceptionHandler y devuelta al cliente como ResponseEntity(409, "El email '<email>' ya se encuentra registrado.").

## **5. Transacciones**
**Problemática:** Ciertas operaciones involucran múltiples cambios en la base de datos que deben ejecutarse de forma atómica — si una falla, todas deben revertirse.
**Solución:** Se utiliza la anotación @Transactional de Spring en los métodos de Service que involucran múltiples operaciones de persistencia. Si ocurre una excepción durante la transacción, Spring revierte automáticamente todos los cambios realizados hasta ese punto.
**Ejemplo:** Al registrar un usuario, UsuarioService.registrar() verifica que no exista el email (existsByEmail) ni el nombre de usuario (existsByNombreUsuario), y luego persiste el nuevo Usuario. Todas estas operaciones van dentro de una misma transacción — si el save falla, las verificaciones previas no generan efectos colaterales inconsistentes.

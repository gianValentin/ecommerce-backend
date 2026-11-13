---
name: tdd
description: Use when implementing or changing Java/Spring behavior, fixing bugs, or refactoring behavior with tests; guide the work through the TDD Red-Green-Refactor cycle.
---

# TDD para este proyecto

Aplica TDD para cambios de comportamiento. No fuerces el ciclo para cambios solo documentales o mecánicos que no alteren comportamiento. Si la regla de negocio esperada no está clara, aclárala antes de codificar.

## Ciclo de trabajo

1. **Entender el caso:** lee `AGENTS.md`, el código de la capa afectada y sus tests cercanos. Elige una unidad pequeña y observable; prueba comportamiento público, no detalles internos. Para un bug, reproduce primero el fallo con un test.
2. **Red:** añade un test que describa un solo resultado esperado y ejecuta solo ese caso. Confirma que falla por la aserción esperada; si falla al compilar o por configuración, corrige el test/entorno antes de continuar.
3. **Green:** implementa el cambio mínimo para pasar el test enfocado y vuelve a ejecutarlo. No adelantes cambios que el test todavía no necesita.
4. **Refactor:** mejora nombres o estructura sin cambiar el comportamiento y vuelve a ejecutar el test.
5. **Verificar:** ejecuta los tests relacionados y, antes de terminar un cambio de comportamiento, `./mvnw test`. Si la suite completa ya se ejecutó y no hubo cambios posteriores, no hace falta repetirla. Informa qué comandos pasaron o fallaron.

## Convenciones del repositorio

- Java 21 / Spring Boot 3.5; el código está bajo `src/main/java/com/app/core` y los tests bajo `src/test/java/com/app/core`. Mantén los tests en el paquete correspondiente a la clase o capa probada.
- Usa `./mvnw test -Dtest=NombreTest#metodo` para un caso, `./mvnw test -Dtest=NombreTest` para una clase y `./mvnw test` para la suite. Maven activa H2 por defecto; no se requiere PostgreSQL para la suite local normal.
- Prefiere tests unitarios para lógica de servicio aislable (JUnit/Mockito, como `DefaultCartServiceTest`). Usa tests con Spring/JPA/controlador cuando la integración de esa capa sea parte del comportamiento que quieres verificar; evita levantar el contexto completo para probar lógica pura.
- Para requests, cubre la validación con `@Valid`; las listas usan `ValidList<T>` para validar cada elemento. Los errores se centralizan en `exception/ControllerAdvice`.
- Conserva los patrones existentes de DTO, servicios y repositorios. Si el cambio afecta JWT, carrito anónimo/fusión o notificaciones de orden por STOMP, comprueba los tests de esa capa y sus permisos/identidad relevantes.
- No dupliques la implementación en las aserciones ni añadas tests redundantes. Cada test debe proteger una regla o regresión concreta.

## Comandos

```bash
# Un método (sustituir clase y método)
./mvnw test -Dtest=DefaultCartServiceTest#calculateCartCaseEmptyEntriesSetsTotalsToZero

# Una clase o toda la suite
./mvnw test -Dtest=DefaultCartServiceTest
./mvnw test
```

CI no tiene un paso de tests dedicado: construye Docker y el `Dockerfile` corre `./mvnw clean package`, que ejecuta los tests. Para la iteración TDD local, usa los comandos Maven enfocados anteriores.

# Repartija — Gastos del viaje

Aplicación web para registrar y dividir gastos de un viaje entre amigos (recitales, Mundial 2026, lo que sea). MVP. Stack: Java 17, Spring Boot, Spring MVC, JPA, Thymeleaf, Bootstrap. Base de datos: H2 en local, PostgreSQL en producción.

## Requisitos

- Java 17  
- **No hace falta instalar Maven**: el proyecto incluye Maven Wrapper (`./mvnw`).

## Ejecutar en local

1. Clonar o abrir el proyecto.
2. Sin variables de entorno: usa H2 en memoria y puerto 8080.

```bash
./mvnw spring-boot:run
```

O compilar y ejecutar el JAR:

```bash
./mvnw clean package -DskipTests
java -jar target/app.jar
```

3. Abrir en el navegador: [http://localhost:8080](http://localhost:8080).

Consola H2 (opcional): [http://localhost:8080/h2-console](http://localhost:8080/h2-console) — JDBC URL: `jdbc:h2:mem:splitwise`, user: `sa`, password vacío.

## Desplegar en Render.com

1. Crear un **Web Service** en Render.
2. Conectar el repositorio y configurar:
   - **Build command:** `./mvnw clean package -DskipTests` (o `mvn clean package -DskipTests` si tienes Maven instalado)
   - **Start command:** `java -jar target/app.jar`
3. Añadir base de datos **PostgreSQL** en Render y copiar **Internal Database URL** (o usar host, database, user, password).
4. En el servicio, pestaña **Environment**, definir:
   - `SPRING_PROFILES_ACTIVE` = `prod`
   - `DB_URL` = URL de PostgreSQL (ej. `jdbc:postgresql://...`)
   - `DB_USER` = usuario de la BD
   - `DB_PASSWORD` = contraseña de la BD
   - `PORT` = lo asigna Render (opcional, por defecto 8080)

No hardcodear credenciales; usar siempre variables de entorno.

## Uso

- **Inicio:** crear un viaje y ver la lista de viajes.
- **Vista de viaje:** agregar personas, registrar gastos (descripción, monto, quién pagó, entre quiénes se divide).
- **Resumen:** ver por persona total pagado, total consumido y saldo (positivo = le deben, negativo = debe).

Los gastos se dividen en partes iguales solo entre las personas seleccionadas. El cálculo de saldos se hace en el backend.

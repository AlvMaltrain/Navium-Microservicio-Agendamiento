# Microservicio de Agendamiento Navium

## Descripción

Este microservicio forma parte del sistema Navium y se encarga de gestionar los agendamientos de camiones para operaciones de carga y descarga. Proporciona funcionalidades CRUD para crear, consultar, actualizar y cancelar agendamientos, además de búsquedas avanzadas por diferentes criterios.

## Tecnologías Utilizadas

- **Java 21**
- **Spring Boot 4.0.6** 
- **Spring Data JPA** para persistencia
- **PostgreSQL** como base de datos
- **RabbitMQ** para mensajería asíncrona
- **Spring Security** con JWT para autenticación
- **OpenAPI/Swagger** para documentación de APIs
- **Maven** para gestión de dependencias

## Requisitos Previos

- JDK 21 o superior
- Maven 3.6+
- PostgreSQL 12+
- RabbitMQ (o acceso a instancia en la nube)

## Configuración

### Base de Datos
Crear una base de datos PostgreSQL llamada `agendamiento_db` con usuario `postgres` y contraseña `simurdiera`, o modificar las propiedades en `application.properties`.

### RabbitMQ
El servicio está configurado para conectarse a una instancia de RabbitMQ en la nube (CloudAMQP). Si se requiere una configuración local, modificar las propiedades correspondientes.

### JWT
La clave secreta para firmar tokens JWT está configurada en `application.properties`. En producción, usar variables de entorno.

## Instalación y Ejecución

1. Clonar el repositorio
2. Navegar al directorio del proyecto
3. Ejecutar `mvn clean install`
4. Ejecutar `mvn spring-boot:run` o usar el wrapper: `./mvnw spring-boot:run`

El servicio estará disponible en `http://localhost:8081`

## API Endpoints

### Base URL: `/api/agendamientos`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/` | Crear nuevo agendamiento |
| PUT | `/{id}/estado` | Actualizar estado de agendamiento |
| GET | `/` | Listar todos los agendamientos |
| GET | `/rut/{rut}` | Buscar por RUT del chofer |
| GET | `/{id}` | Obtener agendamiento por ID |
| GET | `/estado/{estado}` | Listar por estado |
| PUT | `/{id}/transporte` | Actualizar transporte (patente y RUT) |
| PUT | `/{id}/cancelar` | Cancelar agendamiento |
| GET | `/patente/{patente}` | Buscar por patente |
| GET | `/fechas` | Buscar por rango de fechas |
| GET | `/consulta` | Consulta flexible por patente o ID |

### Estados de Agendamiento
- `PENDIENTE`
- `CONFIRMADO`
- `EN_TRANSITO`
- `COMPLETADO`
- `CANCELADO`

### Tipos de Operación
- `CARGA`
- `DESCARGA`

## Documentación API

La documentación completa de la API está disponible en Swagger UI:
`http://localhost:8081/swagger-ui.html`

## Seguridad

El microservicio utiliza autenticación JWT. Todas las peticiones requieren un token Bearer en el header `Authorization`.

## Pruebas

Ejecutar las pruebas con:
```bash
mvn test
```

## Arquitectura

- **Controller**: Maneja las peticiones HTTP y respuestas
- **Service**: Contiene la lógica de negocio
- **Repository**: Interfaz con la base de datos usando JPA
- **Model**: Entidades JPA
- **DTO**: Objetos de transferencia de datos
- **Config**: Configuraciones de seguridad, RabbitMQ y OpenAPI
- **Exception**: Manejo global de excepciones

## Mensajería

El servicio publica mensajes en RabbitMQ para notificaciones y eventos relacionados con cambios en agendamientos.

## Contribución

1. Crear rama feature desde `main`
2. Implementar cambios
3. Ejecutar pruebas
4. Crear pull request


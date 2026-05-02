# AI Cartoon Generator API

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.13-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-Reactive-47A248?style=for-the-badge&logo=mongodb&logoColor=white)
![RapidAPI](https://img.shields.io/badge/RapidAPI-Cartoon%20Generator-0055DA?style=for-the-badge&logo=rapidapi&logoColor=white)
![WebFlux](https://img.shields.io/badge/Spring%20WebFlux-Reactive-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

Microservicio reactivo que convierte imágenes a estilo cartoon mediante IA usando RapidAPI, construido con Spring WebFlux y persistencia reactiva en MongoDB.

</div>

---

## Tabla de Contenidos

- [Descripción](#descripción)
- [Stack Tecnológico](#stack-tecnológico)
- [Dependencias Maven](#dependencias-maven)
- [Base de Datos](#base-de-datos)
- [Configuración](#configuración)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Endpoints REST](#endpoints-rest)
- [Integración RapidAPI](#integración-rapidapi)
- [Swagger UI](#swagger-ui)
- [Flujo de Procesamiento](#flujo-de-procesamiento)

---

## Descripción

**AI Cartoon Generator** es un microservicio reactivo que permite subir imágenes (JPEG, PNG, BMP, WEBP) y transformarlas a estilo cartoon usando inteligencia artificial. El proceso es asíncrono: al generar la tarea se retorna un `task_id` con el que se puede consultar el resultado cuando esté listo. Todos los estados se persisten en MongoDB.

Características principales:

- Procesamiento completamente reactivo con Project Reactor
- Integración con RapidAPI — AI Cartoon Generator
- Subida de imágenes vía `multipart/form-data`
- Tarea asíncrona: genera un `task_id` y luego consulta el resultado
- Persistencia reactiva con Spring Data MongoDB Reactive
- Soporte para múltiples estilos de cartoon mediante índice numérico
- Documentación interactiva con Swagger UI
- Registro de estado por operación (`pending` / `completed` / `failed`)

---

## Stack Tecnológico

| Tecnología             | Versión | Uso                                      |
|------------------------|---------|------------------------------------------|
| Java                   | 21      | Lenguaje principal                       |
| Spring Boot            | 3.5.13  | Framework base                           |
| Spring WebFlux         | 6.1.x   | API REST reactiva no bloqueante          |
| Project Reactor        | 3.6.x   | Programación reactiva (Mono / Flux)      |
| Spring Data MongoDB R. | 3.x     | Acceso reactivo a MongoDB                |
| Lombok                 | 1.18.x  | Reducción de boilerplate                 |
| SpringDoc OpenAPI      | 2.8.15  | Documentación Swagger UI                 |
| spring-dotenv          | 4.0.0   | Carga de variables de entorno            |

---

## Dependencias Maven

### Spring WebFlux + MongoDB Reactivo

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
</dependency>
<dependency>
    <groupId>io.projectreactor</groupId>
    <artifactId>reactor-test</artifactId>
    <scope>test</scope>
</dependency>
```

### Swagger (SpringDoc para WebFlux)

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
    <version>2.8.15</version>
</dependency>
```

### Lombok

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

### spring-dotenv (Variables de Entorno)

```xml
<dependency>
    <groupId>me.paulschwarz</groupId>
    <artifactId>spring-dotenv</artifactId>
    <version>4.0.0</version>
</dependency>
```

---

## Base de Datos

El proyecto usa **MongoDB** con la colección `cartoon`. No requiere SQL ni migraciones; MongoDB crea la colección automáticamente al insertar el primer documento.

### Estructura del documento

```json
{
  "_id": "ObjectId",
  "imageName": "foto.jpg",
  "cartoonIndex": 1,
  "taskId": "abc123",
  "taskType": "async",
  "requestId": "req_xyz",
  "logId": "log_xyz",
  "errorCode": 0,
  "errorMsg": null,
  "resultUrl": "https://cdn.rapidapi.com/result.jpg",
  "taskStatus": 2,
  "status": "completed",
  "deleted": false,
  "createdAt": "2026-04-06T10:00:00",
  "updatedAt": "2026-04-06T10:05:00"
}
```

### Descripción de campos

| Campo         | Tipo         | Descripción                                              |
|---------------|--------------|----------------------------------------------------------|
| `_id`         | ObjectId     | Identificador único de MongoDB                           |
| `imageName`   | String       | Nombre del archivo de imagen subido                      |
| `cartoonIndex`| Integer      | Índice del estilo cartoon seleccionado                   |
| `taskId`      | String       | ID de tarea retornado por RapidAPI                       |
| `taskType`    | String       | Tipo de tarea (`async`)                                  |
| `requestId`   | String       | ID de solicitud retornado por RapidAPI                   |
| `logId`       | String       | Log ID retornado por RapidAPI                            |
| `errorCode`   | Integer      | Código de error de la API (`0` = sin error)              |
| `errorMsg`    | String       | Mensaje de error si el procesamiento falla               |
| `resultUrl`   | String       | URL de la imagen cartoon generada                        |
| `taskStatus`  | Integer      | Estado numérico: `0`=queued, `1`=processing, `2`=success |
| `status`      | String       | Estado legible: `pending` \| `completed` \| `failed`    |
| `deleted`     | Boolean      | Borrado lógico: `false`=activo, `true`=eliminado         |
| `createdAt`   | LocalDateTime| Fecha y hora de creación del registro                    |
| `updatedAt`   | LocalDateTime| Fecha y hora de última actualización                     |

---

## Configuración

### 1. Variables de entorno (`.env`)

Crea un archivo `.env` en la raíz del proyecto:

```env
MONGO_DATABASE=cartoon_db
MONGO_URI=mongodb+srv://<usuario>:<contraseña>@<cluster>.mongodb.net
RAPIDAPI_KEY=<tu_api_key>
RAPIDAPI_HOST=<rapidapi_host>
RAPIDAPI_BASE_URL=https://<rapidapi_host>
```

> **Nota:** Nunca subas el archivo `.env` a Git. Agrégalo al `.gitignore`.

### 2. `application.yaml`

```yaml
spring:
  application:
    name: ai-cartoon-generator
  data:
    mongodb:
      database: ${MONGO_DATABASE}
      uri: ${MONGO_URI}

rapidapi:
  key: ${RAPIDAPI_KEY}
  host: ${RAPIDAPI_HOST}
  base-url: ${RAPIDAPI_BASE_URL}
  endpoint-generate: /image/effects/generate_cartoonized_image
  endpoint-result: /api/rapidapi/query-async-task-result

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true

server:
  port: 8085
```

### 3. `.gitignore` recomendado

```gitignore
.env
target/
*.class
*.jar
.idea/
*.iml
```

---

## Estructura del Proyecto

```
src/main/java/jhon/silva/cartoon/
  ├── CartoonApplication.java          # Clase principal
  ├── Config/
  │   ├── SwaggerConfig.java           # Configuración OpenAPI
  │   └── WebClientConfig.java         # WebClient para RapidAPI
  ├── Model/
  │   └── CartoonResult.java           # Entidad MongoDB
  ├── Repository/
  │   └── CartoonRepository.java       # ReactiveMongoRepository
  ├── Rest/
  │   └── CartoonRest.java             # Controlador REST
  └── Service/
      ├── ICartoonService.java         # Interfaz del servicio
      └── impl/
          └── CartoonServiceImpl.java  # Lógica de negocio

src/main/resources/
  └── application.yaml

.env                                   # Variables de entorno (NO subir a Git)
```

---

## Endpoints REST

**Base URL:** `http://localhost:8085/api/cartoon`

| Método   | Path                        | Descripción                                  |
|----------|-----------------------------|----------------------------------------------|
| `POST`   | `/api/cartoon/generate`     | Subir imagen y generar tarea cartoon         |
| `GET`    | `/api/cartoon/task/{taskId}`| Consultar resultado de una tarea asíncrona   |
| `GET`    | `/api/cartoon/all`          | Listar todos los registros (no eliminados)   |
| `GET`    | `/api/cartoon/{id}`         | Buscar un registro por su ID de MongoDB      |
| `GET`    | `/api/cartoon/status/{status}` | Filtrar registros por estado              |
| `PUT`    | `/api/cartoon/{id}`         | Actualizar registro regenerando cartoon      |
| `DELETE` | `/api/cartoon/{id}`         | Eliminar registro (borrado lógico)           |

---

### POST `/api/cartoon/generate` — Generar cartoon

**Content-Type:** `multipart/form-data`

| Campo   | Tipo   | Descripción                             |
|---------|--------|-----------------------------------------|
| `image` | File   | Imagen a convertir (JPEG/PNG/BMP/WEBP)  |
| `index` | String | Índice del estilo cartoon (ej: `"1"`)   |

**Respuesta:**

```json
{
  "id": "664f1a2b3c4d5e6f7a8b9c0d",
  "imageName": "foto.jpg",
  "cartoonIndex": 1,
  "taskId": "task_abc123",
  "taskType": "async",
  "requestId": "req_xyz",
  "logId": "log_xyz",
  "errorCode": 0,
  "errorMsg": null,
  "resultUrl": null,
  "taskStatus": null,
  "status": "pending",
  "createdAt": "2026-04-06T10:00:00"
}
```

---

### GET `/api/cartoon/task/{taskId}` — Consultar resultado

Consulta el estado actual de la tarea en RapidAPI y actualiza el registro en MongoDB.

**Respuesta cuando está completada:**

```json
{
  "id": "664f1a2b3c4d5e6f7a8b9c0d",
  "taskId": "task_abc123",
  "taskStatus": 2,
  "status": "completed",
  "resultUrl": "https://cdn.rapidapi.com/cartoon_result.jpg"
}
```

---

### Estados posibles

| Estado      | `taskStatus` | Descripción                                          |
|-------------|--------------|------------------------------------------------------|
| `pending`   | `0` o `1`    | Tarea en cola o procesándose                         |
| `completed` | `2`          | Imagen cartoon generada, `resultUrl` disponible      |
| `failed`    | —            | Error durante el procesamiento (ver `errorMsg`)      |

---

### PUT `/api/cartoon/{id}` — Actualizar cartoon

Actualiza un registro existente regenerando el cartoon con nueva imagen/índice usando la API de IA.

**Content-Type:** `multipart/form-data`

| Campo   | Tipo   | Descripción                             |
|---------|--------|-----------------------------------------|
| `image` | File   | Nueva imagen a convertir                |
| `index` | String | Nuevo índice del estilo cartoon         |

**Respuesta:**

```json
{
  "id": "664f1a2b3c4d5e6f7a8b9c0d",
  "imageName": "nueva_foto.jpg",
  "cartoonIndex": 2,
  "taskId": "task_new123",
  "status": "pending",
  "deleted": false,
  "updatedAt": "2026-04-29T15:30:00"
}
```

---

### DELETE `/api/cartoon/{id}` — Eliminar registro (borrado lógico)

Marca un registro como eliminado sin borrarlo físicamente de la base de datos.

**Respuesta:**

```json
{
  "id": "664f1a2b3c4d5e6f7a8b9c0d",
  "imageName": "foto.jpg",
  "deleted": true,
  "updatedAt": "2026-04-29T15:35:00"
}
```

**Nota:** Los registros eliminados no aparecen en las consultas GET `/all` o `/status/{status}`.

---

### Estados posibles

| Estado      | `taskStatus` | Descripción                                          |
|-------------|--------------|------------------------------------------------------|
| `pending`   | `0` o `1`    | Tarea en cola o procesándose                         |
| `completed` | `2`          | Imagen cartoon generada, `resultUrl` disponible      |
| `failed`    | —            | Error durante el procesamiento (ver `errorMsg`)      |

---

## Integración RapidAPI

| Propiedad  | Valor                                                       |
|------------|-------------------------------------------------------------|
| Nombre     | AI Cartoon Generator                                        |
| Endpoint 1 | `POST /image/effects/generate_cartoonized_image`            |
| Endpoint 2 | `GET /api/rapidapi/query-async-task-result?task_id={id}`    |
| Autenticación | Header `x-rapidapi-key`                                  |

### Parámetros — Generar (POST multipart)

| Parámetro   | Tipo    | Descripción                              |
|-------------|---------|------------------------------------------|
| `image`     | File    | Imagen en formato binario                |
| `index`     | Integer | Índice del estilo cartoon                |
| `task_type` | String  | Siempre `"async"`                        |

### Parámetros — Consultar resultado (GET)

| Parámetro | Tipo   | Descripción                        |
|-----------|--------|------------------------------------|
| `task_id` | String | ID de tarea retornado al generar   |

---

## Swagger UI

Una vez iniciada la aplicación:

| Recurso       | URL                                    |
|---------------|----------------------------------------|
| Swagger UI    | http://localhost:8085/swagger-ui.html  |
| OpenAPI JSON  | http://localhost:8085/api-docs         |

---

## Flujo de Procesamiento

```
Cliente
  │
  ▼
POST /api/cartoon/generate  (multipart: image + index)
  │
  ▼
CartoonServiceImpl
  │
  ├── 1. Lee los bytes de la imagen
  │
  ├── 2. POST RapidAPI → /generate_cartoonized_image
  │         └── Retorna task_id + request_id
  │
  ├── 3. Guarda registro en MongoDB con status = "pending"
  │
  └── Retorna CartoonResult al cliente (con task_id)


Cliente (después de unos segundos)
  │
  ▼
GET /api/cartoon/task/{taskId}
  │
  ▼
CartoonServiceImpl
  │
  ├── 1. GET RapidAPI → /query-async-task-result?task_id=...
  │
  ├── 2a. task_status = 2  ──► status = "completed" + guarda resultUrl
  │
  ├── 2b. task_status = 0/1 ──► status = "pending"  (reintentar luego)
  │
  └── 2c. error_code ≠ 0   ──► status = "failed"
                   │
                   ▼
      Registro actualizado en MongoDB y retornado al cliente
```
# AI Cartoon Generator con Spring Boot

**Servicio de IA con Rapid API**, AI Cartoon Generator convierte imágenes reales a estilo cartoon/animación usando inteligencia artificial. El proceso es asíncrono: se envía la imagen, se obtiene un `task_id` y luego se consulta el resultado cuando la tarea finaliza. Los resultados se persisten en MongoDB.

---

## Tecnologías

**1. Cognitive Services**

<img src="https://wakeupandcode.com/wp-content/uploads/2019/08/azure-cognitive-services-bootcamp-event-image.png" align="right" style="width: 200px"/>

- Rapid API — AI Cartoon Generator
- Conversión de imágenes a estilo cartoon con IA
- Procesamiento asíncrono mediante `task_id`

**2. Spring Boot**

<img src="https://miro.medium.com/v2/resize:fit:716/1*98O4Gb5HLSlmdUkKg1DP1Q.png" align="right" style="height:60px; width: 200px"/>

- Java: JDK 21
- IDE: IntelliJ IDEA | Visual Studio Code | Codespace
- Maven: Apache Maven
- Framework: Spring Boot 3.5.13

**3. Maven Dependencias**

<img src="https://upload.wikimedia.org/wikipedia/commons/thumb/5/52/Apache_Maven_logo.svg/1280px-Apache_Maven_logo.svg.png" align="right" style="width: 200px"/>

- `spring-boot-starter-webflux`
- `spring-boot-starter-data-mongodb-reactive`
- `springdoc-openapi-starter-webflux-ui`
- `spring-dotenv`
- `lombok`
- `reactor-test`

---

## Dependencias Spring WebFlux + MongoDB (NoSQL)

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

## Dependencia Swagger para Spring WebFlux

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
    <version>2.8.15</version>
</dependency>
```

## Dependencia spring-dotenv (variables de entorno)

```xml
<dependency>
    <groupId>me.paulschwarz</groupId>
    <artifactId>spring-dotenv</artifactId>
    <version>4.0.0</version>
</dependency>
```

---

## Configuración — `application.yaml`

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

server:
  port: 8080
```

Las variables sensibles se leen desde un archivo `.env` en la raíz del proyecto.



## Flujo de la Aplicación

```
Cliente
  │
  ├─► POST /api/cartoon/generate  (imagen + index)
  │       │
  │       └─► RapidAPI: genera cartoon (async) → devuelve task_id
  │               └─► Guarda en MongoDB con status = "pending"
  │
  └─► GET /api/cartoon/task/{taskId}
          │
          └─► RapidAPI: consulta resultado por task_id
                  └─► Actualiza MongoDB:
                        task_status=2  → status = "completed" + result_url
                        task_status=1  → status = "pending"
                        task_status=0  → status = "pending" (en cola)
                        error_code≠0   → status = "failed"
```

---

## Endpoints REST

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/cartoon/generate` | Sube imagen y genera cartoon (async) |
| `GET` | `/api/cartoon/task/{taskId}` | Consulta resultado de tarea asíncrona |
| `GET` | `/api/cartoon/all` | Lista todos los documentos en MongoDB |
| `GET` | `/api/cartoon/{id}` | Busca documento por ID de MongoDB |
| `GET` | `/api/cartoon/status/{status}` | Filtra por estado: `pending`, `completed`, `failed` |

### Ejemplo: Generar cartoon

```
POST /api/cartoon/generate
Content-Type: multipart/form-data

image  = <archivo JPEG/PNG/JPG/BMP/WEBP>
index  = 1   (estilo de cartoon: 1–N según la API)
```

### Ejemplo: Consultar resultado

```
GET /api/cartoon/task/abc123xyz
```

---

## Modelo MongoDB — `CartoonResult`

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | String | ID del documento (MongoDB) |
| `imageName` | String | Nombre del archivo subido |
| `cartoonIndex` | Integer | Estilo de cartoon seleccionado |
| `taskId` | String | ID de tarea devuelto por RapidAPI |
| `taskType` | String | Tipo de tarea (`async`) |
| `requestId` | String | ID de solicitud de RapidAPI |
| `logId` | String | Log ID de RapidAPI |
| `errorCode` | Integer | Código de error (0 = sin error) |
| `errorMsg` | String | Mensaje de error si aplica |
| `resultUrl` | String | URL de la imagen cartoon resultante |
| `taskStatus` | Integer | 0=en cola, 1=procesando, 2=completado |
| `status` | String | `pending`, `completed`, `failed` |
| `createdAt` | LocalDateTime | Fecha de creación |

---

## Documentación Swagger

Una vez levantado el servicio, accede a la UI interactiva en:

```
http://localhost:8080/swagger-ui.html
```# AS241S5_AEJ_38-be

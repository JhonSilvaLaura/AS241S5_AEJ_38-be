# Article Extractor & Summarizer API

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.13-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Neon-336791?style=for-the-badge&logo=postgresql&logoColor=white)
![RapidAPI](https://img.shields.io/badge/RapidAPI-Article%20Summarizer-0055DA?style=for-the-badge&logo=rapidapi&logoColor=white)
![WebFlux](https://img.shields.io/badge/Spring%20WebFlux-Reactive-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

Microservicio reactivo que extrae y resume artículos web mediante IA usando RapidAPI, construido con Spring WebFlux y persistencia reactiva en PostgreSQL (Neon).

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

**Article Extractor & Summarizer** es un microservicio reactivo que permite procesar URLs de artículos web y generar resúmenes automáticos en múltiples idiomas usando inteligencia artificial. Los resultados se almacenan de forma asíncrona en una base de datos PostgreSQL alojada en la nube de Neon.

Características principales:

- Procesamiento completamente reactivo con Project Reactor
- Integración con RapidAPI — Article Extractor and Summarizer
- Persistencia reactiva con R2DBC sobre PostgreSQL (Neon Cloud)
- Soporte multi-idioma para generación de resúmenes
- Documentación interactiva con Swagger UI
- Registro de estado por operación (pending / generated / failed)

---

## Stack Tecnológico

| Tecnología | Versión | Uso |
|---|---------|---|
| Java | 21      | Lenguaje principal |
| Spring Boot | 3.5.13  | Framework base |
| Spring WebFlux | 6.1.12  | API REST reactiva no bloqueante |
| Project Reactor | 3.6.9   | Programación reactiva (Mono / Flux) |
| Spring Data R2DBC | 3.3.3   | Acceso reactivo a BD relacional |
| r2dbc-postgresql | 1.0.5   | Driver R2DBC para PostgreSQL |
| Lombok | 1.18.34 | Reducción de boilerplate |
| SpringDoc OpenAPI | 2.3.0   | Documentación Swagger UI |
| dotenv-java | 3.0.0   | Carga de variables de entorno |

---

## Dependencias Maven

### Spring WebFlux + PostgreSQL (SQL)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-r2dbc</artifactId>
</dependency>
<dependency>
    <groupId>io.projectreactor</groupId>
    <artifactId>reactor-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>r2dbc-postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

### Swagger (SpringDoc para WebFlux)

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
    <version>2.3.0</version>
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

### dotenv-java (Variables de Entorno)

```xml
<dependency>
    <groupId>io.github.cdimascio</groupId>
    <artifactId>dotenv-java</artifactId>
    <version>3.0.0</version>
</dependency>
```

---

## Base de Datos

### Crear la base de datos

```sql
CREATE DATABASE IA_Article_Extractor;
```

### Esquema de tabla

```sql
CREATE TABLE IF NOT EXISTS article_summaries (
    id            BIGSERIAL PRIMARY KEY,
    url           TEXT NOT NULL,
    summary       TEXT,
    language      VARCHAR(10) DEFAULT 'es',
    length        INTEGER DEFAULT 3,
    status        VARCHAR(20) DEFAULT 'pending',
    error_message TEXT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Descripción de columnas

| Columna | Tipo | Descripción |
|---|---|---|
| `id` | BIGSERIAL PK | Identificador único autoincremental |
| `url` | TEXT NOT NULL | URL del artículo procesado |
| `summary` | TEXT | Resumen generado por la IA |
| `language` | VARCHAR(10) | Idioma del resumen (`es`, `en`, etc.) |
| `length` | INTEGER | Número de oraciones del resumen |
| `status` | VARCHAR(20) | Estado: `pending` \| `generated` \| `failed` |
| `error_message` | TEXT | Mensaje de error si el procesamiento falla |
| `created_at` | TIMESTAMP | Fecha y hora de creación del registro |

---

## Configuración

### 1. Variables de entorno (`.env`)

Crea un archivo `.env` en la raíz del proyecto:

```env
DATABASE_URL=r2dbc:postgresql://<host>/<database>?sslmode=require
DATABASE_USERNAME=<usuario>
DATABASE_PASSWORD=<contraseña>
RAPIDAPI_KEY=<tu_api_key>
```

> **Nota:** Nunca subas el archivo `.env` a Git. Agrégalo al `.gitignore`.

### 2. `application.yaml`

```yaml
spring:
  application:
    name: article-summarizer
  r2dbc:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}

rapidapi:
  key: ${RAPIDAPI_KEY}
  host: article-extractor-and-summarizer.p.rapidapi.com
  base-url: https://article-extractor-and-summarizer.p.rapidapi.com

springdoc:
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
  api-docs:
    path: /api-docs

server:
  port: 8081
```

### 3. Cargar dotenv en el Main

```java
@SpringBootApplication
public class ArticleExtractorApplication {

    public static void main(String[] args) {
        Dotenv.configure().ignoreIfMissing().systemProperties().load();
        SpringApplication.run(ArticleExtractorApplication.class, args);
    }
}
```

---

## Estructura del Proyecto

```
src/main/java/jhon/silva/articleExtractor/
  ├── ArticleExtractorApplication.java   # Clase principal
  ├── Config/
  │   ├── SwaggerConfig.java             # Configuracion OpenAPI
  │   └── WebClientConfig.java           # WebClient para RapidAPI
  ├── Model/
  │   ├── ArticleRequest.java            # DTO de entrada
  │   └── ArticleSummary.java            # Entidad de base de datos
  ├── Repository/
  │   └── ArticleRepository.java         # ReactiveCrudRepository
  ├── Rest/
  │   └── ArticleRest.java               # Controlador REST
  └── Service/
      └── impl
      |     └── ArticleServiceImpl.java   # Logica de negocio
      └── IArticleService.java           

src/main/resources/
  └── application.yaml

.env                                     # Variables de entorno (NO subir a Git)
```

---

## Endpoints REST

**Base URL:** `http://localhost:8081/api/articles`

| Método | Path | Descripción |
|---|---|---|
| `POST` | `/api/articles` | Resumir un artículo dado su URL |
| `GET` | `/api/articles` | Listar todos los resúmenes almacenados |
| `GET` | `/api/articles/{id}` | Buscar un resumen por su ID |
| `GET` | `/api/articles/status/{status}` | Filtrar resúmenes por estado |
| `GET` | `/api/articles/lang/{lang}` | Filtrar resúmenes por idioma |

### POST `/api/articles` — Request Body

```json
{
  "url": "https://elperuano.pe/noticia/292757-...",
  "lang": "es",
  "length": 3
}
```

### Respuesta exitosa

```json
{
  "id": 1,
  "url": "https://elperuano.pe/noticia/292757-...",
  "summary": "El articulo trata sobre...",
  "language": "es",
  "length": 3,
  "status": "generated",
  "errorMessage": null,
  "createdAt": "2026-04-05T22:00:00"
}
```

### Estados posibles

| Estado | Descripción |
|---|---|
| `pending` | Registro creado, procesamiento iniciado |
| `generated` | Resumen generado exitosamente por la IA |
| `failed` | Error durante el procesamiento (ver `error_message`) |

---

## Integración RapidAPI

| Propiedad | Valor |
|---|---|
| Nombre | Article Extractor and Summarizer |
| Host | `article-extractor-and-summarizer.p.rapidapi.com` |
| Endpoint | `GET /summarize` |
| Autenticación | Header `X-RapidAPI-Key` |

### Parámetros de query

| Parámetro | Tipo | Descripción |
|---|---|---|
| `url` | string (requerido) | URL del artículo a resumir |
| `length` | integer | Número de oraciones (default: `3`) |
| `lang` | string | Idioma del resumen (default: `es`) |
| `engine` | integer | Motor de IA a utilizar (valor: `2`) |

---

## Swagger UI

Una vez iniciada la aplicación:

| Recurso | URL |
|---|---|
| Swagger UI | http://localhost:8081/swagger-ui.html |
| OpenAPI JSON | http://localhost:8081/api-docs |

---

## Flujo de Procesamiento

```
Cliente
  │
  ▼
POST /api/articles
  │
  ▼
ArticleService
  │
  ├── 1. Crea registro en BD con status = "pending"
  │
  ├── 2. WebClient → GET https://rapidapi.com/summarize?url=...
  │
  ├── 3a. Exito  ──► actualiza summary + status = "generated"
  │
  └── 3b. Error  ──► actualiza error_message + status = "failed"
                           │
                           ▼
              Registro guardado retornado al cliente
```
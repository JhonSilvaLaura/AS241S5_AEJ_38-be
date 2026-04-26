# Manifiestos Kubernetes - AI Cartoon Generator

## Estructura de Archivos

```
manifest-jhon-silva-38/
├── jhon-silva-38-namespace.yml      # Namespace para el despliegue
├── jhon-silva-38-secret.yml          # Secret con variables de entorno
├── jhon-silva-38-service.yml        # Servicio para exponer la API
├── jhon-silva-38-deployment.yml      # Deployment de la aplicación
└── README.md                         # Este archivo

Dockerfile (en raíz del proyecto)     # Construcción de imagen Docker
```

## Despliegue

### 1. Construir imagen Docker
```bash
docker build -t jhon-silva-38/cartoon-generator:latest .
```

### 2. Aplicar manifiestos en orden
```bash
kubectl apply -f manifest-jhon-silva-38/jhon-silva-38-namespace.yml
kubectl apply -f manifest-jhon-silva-38/jhon-silva-38-secret.yml
kubectl apply -f manifest-jhon-silva-38/jhon-silva-38-service.yml
kubectl apply -f manifest-jhon-silva-38/jhon-silva-38-deployment.yml
```

### 3. Verificar despliegue
```bash
kubectl get all -n jhon-silva-38
```

### 4. Acceder a la aplicación
```bash
# Port forward para pruebas locales
kubectl port-forward svc/jhon-silva-38-service 8085:8085 -n jhon-silva-38

# Acceder a Swagger UI
http://localhost:8085/swagger-ui.html
```

## Configuración

- **Namespace:** `jhon-silva-38`
- **Replicas:** 2
- **Puerto:** 8085
- **Base de datos:** MongoDB Atlas
- **API externa:** RapidAPI AI Cartoon Generator

## Variables de Entorno (Secret)

Las variables sensibles están almacenadas en el Secret `jhon-silva-38-secret`:
- `MONGO_DATABASE`
- `MONGO_URI`
- `RAPIDAPI_KEY`
- `RAPIDAPI_HOST`
- `RAPIDAPI_BASE_URL`

## Health Checks

La aplicación expone endpoints de health en `/actuator/health` configurados para:
- **Liveness Probe:** Verifica si el contenedor está vivo
- **Readiness Probe:** Verifica si la aplicación está lista para recibir tráfico

# Instrucciones de Despliegue - Jhon Silva 38

## 📋 Requisitos Previos

1. Tener Kubernetes instalado y funcionando (Docker Desktop, GitHub Codespace o Killercoda)
2. Tener kubectl configurado
3. Imagen Docker disponible: `jhonbrayansilvalaura/article-summarizer:latest`

## 🚀 Pasos de Despliegue

### 1. Crear Namespace
```bash
kubectl apply -f jhon-silva-38-namespace.yml
```

### 2. Crear Secret
```bash
kubectl apply -f jhon-silva-38-secret.yml
```

### 3. Crear Service
```bash
kubectl apply -f jhon-silva-38-service.yml
```

### 4. Crear Deployment
```bash
kubectl apply -f jhon-silva-38-deployment.yml
```

### 5. Verificar Despliegue
```bash
# Verificar namespace
kubectl get namespace jhon-silva-38

# Verificar pods
kubectl get pods -n jhon-silva-38

# Verificar service
kubectl get service -n jhon-silva-38

# Verificar deployment
kubectl get deployment -n jhon-silva-38

# Verificar logs
kubectl logs -f deployment/jhon-silva-38-deployment -n jhon-silva-38
```

### 6. Acceder a la Aplicación
```bash
# Obtener URL del servicio (para LoadBalancer)
kubectl get service jhon-silva-38-service -n jhon-silva-38

# Para Docker Desktop, accede via:
# http://localhost:8081
```

## 🧪 Validación

### Prueba de Dependencia del Secret
Para validar la dependencia del secret:

1. Eliminar el secret:
```bash
kubectl delete secret jhon-silva-38-secret -n jhon-silva-38
```

2. Eliminar y volver a crear el deployment:
```bash
kubectl delete deployment jhon-silva-38-deployment -n jhon-silva-38
kubectl apply -f jhon-silva-38-deployment.yml
```

3. El deployment fallará porque las variables de entorno no están disponibles sin el secret.

## 📁 Estructura de Archivos

```
manifest-jhon-silva/
├── jhon-silva-38-namespace.yml    # Namespace aislado
├── jhon-silva-38-secret.yml        # Secretos BD y APIs
├── jhon-silva-38-service.yml       # Service LoadBalancer
├── jhon-silva-38-deployment.yml    # Deployment con 2 réplicas
└── INSTRUCCIONES_DESPLIEGUE.md     # Este archivo
```

## 🔧 Configuración Técnica

- **Aplicación**: Spring WebFlux (reactivo)
- **Base de Datos**: PostgreSQL en AWS Neutron
- **APIs AI**: RapidAPI Article Extractor & Summarizer
- **Replicas**: 2
- **Puerto**: 8081
- **Health Checks**: /actuator/health
- **Recursos**: 256Mi-512Mi RAM, 250m-500m CPU

## ⚠️ Notas Importantes

- Los secretos están codificados en Base64
- El deployment depende completamente del secret para variables de entorno
- Los health checks tienen delays altos para permitir inicio de la aplicación
- El service tipo LoadBalancer permite acceso externo

# Kubernetes Manifests - Jhon Silva 38

## Archivos de Manifiestos

- `jhon-silva-38-namespace.yml` - Namespace para el despliegue
- `jhon-silva-38-secret.yml` - Secret con credenciales de BD y API
- `jhon-silva-38-service.yml` - Service tipo LoadBalancer
- `jhon-silva-38-deployment.yml` - Deployment con 2 réplicas

## Comandos de Despliegue

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

### Despliegue Completo (todos los archivos)
```bash
kubectl apply -f .
```

## Verificación

### Verificar Namespace
```bash
kubectl get namespace jhon-silva-38
```

### Verificar Secret
```bash
kubectl get secret jhon-silva-38-secret -n jhon-silva-38
```

### Verificar Service
```bash
kubectl get service jhon-silva-38-service -n jhon-silva-38
```

### Verificar Deployment
```bash
kubectl get deployment jhon-silva-38-deployment -n jhon-silva-38
```

### Verificar Pods
```bash
kubectl get pods -n jhon-silva-38
```

### Verificar Logs
```bash
kubectl logs -f deployment/jhon-silva-38-deployment -n jhon-silva-38
```

## Acceso a la Aplicación

### Obtener URL del Service
```bash
kubectl get service jhon-silva-38-service -n jhon-silva-38 -o wide
```

### Port Forward (para pruebas locales)
```bash
kubectl port-forward service/jhon-silva-38-service 8081:8081 -n jhon-silva-38
```

## Limpieza

### Eliminar todos los recursos
```bash
kubectl delete -f .
```

### Eliminar namespace (eliminará todos los recursos)
```bash
kubectl delete namespace jhon-silva-38
```

## Notas Importantes

- El deployment depende del secret `jhon-silva-38-secret`
- Si eliminas el secret, el deployment fallará
- El service es tipo LoadBalancer para acceso externo
- La aplicación escucha en el puerto 8081
- Se incluyen health checks para liveness y readiness probes

# Kubernetes Deployment Guide: Order Processing System

This guide provides step-by-step instructions to deploy the `order-processing-system` and its required PostgreSQL database to a Kubernetes cluster (specifically Minikube).

## Prerequisites
- **Docker**: Installed and running.
- **Minikube**: Installed and running (`minikube start`).
- **kubectl**: Installed and configured to talk to Minikube.
- **OpenSSL**: For generating secure passwords (usually pre-installed on macOS/Linux).

## Architecture Overview
The deployment consists of two main components:
1.  **PostgreSQL Database**:
    -   **ConfigMap**: Stores non-sensitive configuration (DB name, user).
    -   **Secret**: Stores the database password (generated securely).
    -   **PersistentVolumeClaim (PVC)**: Ensures data persists across pod restarts.
    -   **Deployment**: Runs the `postgres:15-alpine` container.
    -   **Service**: Exposes the database internally on port 5432.
2.  **Order Processing System (Application)**:
    -   **Deployment**: Runs the Spring Boot application.
    -   **Service**: Exposes the application externally (via `NodePort`).
    -   **Configuration**: Connects to the database using environment variables injected from the Postgres ConfigMap and Secret.

---

## Deployment Steps

### 1. Build and Load Application Image
Use the provided build script to build the image with the correct version and load it into Minikube.

```bash
# Run the build script
./order-processing-system/build.sh
```

### 2. Deploy PostgreSQL Database
Before deploying the application, set up the database infrastructure.

#### 2.1 Generate Database Password
For security, we generate a random password and store it directly as a Kubernetes Secret.

```bash
# Generate a random password and create the secret 'postgres-secret'
kubectl create secret generic postgres-secret --from-literal=POSTGRES_PASSWORD=$(openssl rand -base64 16)
```

#### 2.2 Apply Postgres Manifests
Deploy the ConfigMap, PVC, Deployment, and Service.

```bash
kubectl apply -f order-processing-system/k8s/postgres/
```

#### 2.3 Verify Database Status
Ensure the database pod is running and ready.

```bash
kubectl get pods -l app=postgres
```

### 3. Deploy Order Processing System
Once the database is ready, deploy the application.

```bash
# Apply the application manifests (Deployment and Service)
kubectl apply -f order-processing-system/k8s/ops/
```

### 4. Access the Application
The application is exposed via a `NodePort`. You can access it using the Minikube service URL.

```bash
# Get the URL
minikube service order-processing-system --url

# Or open it directly in your default browser
minikube service order-processing-system
```

### 5. Scaling
To scale the application to multiple replicas (e.g., 3 pods):

```bash
kubectl scale deployment order-processing-system --replicas=3
```

Verify the scaling:
```bash
kubectl get pods -l app=order-processing-system
```

---

## Troubleshooting

### "ImagePullBackOff" or "ErrImageNeverPull"
- **Cause**: Minikube cannot find the image `order-processing-system:1.0.1-SNAPSHOT`.
- **Fix**: Ensure you ran `minikube image load order-processing-system:1.0.1-SNAPSHOT`.

### Database Connection Errors
- **Cause**: Application cannot connect to `postgres:5432`.
- **Fix**:
    1.  Check if Postgres pod is running: `kubectl get pods -l app=postgres`
    2.  Check logs: `kubectl logs -l app=order-processing-system`
    3.  Ensure the Secret `postgres-secret` exists: `kubectl get secret postgres-secret`

### Accessing NodePort on macOS (Docker Driver)
- **Issue**: `localhost:30080` doesn't work.
- **Fix**: Use `minikube service order-processing-system` to create a tunnel, or use `kubectl port-forward`.

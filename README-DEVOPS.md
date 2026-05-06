# blog-service DevOps Guide

## Docker

Build the Spring Boot jar first:

```bash
mvn clean package -DskipTests
docker build -t pidevfreelancy/blog-service:latest .
docker run --name blog-service -p 8050:8050 pidevfreelancy/blog-service:latest
```

Cleanup:

```bash
docker stop blog-service
docker rm blog-service
```

## Jenkins CI only

This pipeline is CI only:

- Git checkout
- Maven test
- SonarQube analysis
- Quality Gate
- Maven package
- Docker build
- Docker push to Docker Hub

It does **not** deploy to Kubernetes.

SonarQube note:

- the pipeline uses `org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar`
- if Sonar analysis or Quality Gate fails in Jenkins, the stage stays visible and the pipeline becomes `UNSTABLE` instead of stopping before Docker build
- this fallback was added because Sonar/Jenkins compatibility can fail even when Maven tests and packaging are correct

Branch placeholder:

- `blog-management`

Jenkins credential IDs:

- Docker Hub credentials ID: `docker-hub-credentials`
- Sonar token credentials ID: `sonar-token`
- Sonar server name in Jenkins: `SonarQube`

Important when using Jenkins inside the Vagrant VM without commit/push:

- create a Pipeline job in Jenkins
- paste the content of `Jenkinsfile-CI`
- this pipeline automatically uses `/home/vagrant/DEVOPS/blog-service` if that shared folder exists
- if later you use a real Git repository with these same changes committed, the pipeline can also fall back to `checkout scm`

## SonarQube preparation

Run SonarQube locally inside the VM or Docker host:

```bash
docker run -d --name sonarqube -p 9000:9000 sonarqube
```

Open:

- `http://localhost:9000`

Then:

- change password
- create token
- add token to Jenkins credentials
- keep Jenkins Sonar server name as `SonarQube`
- add SonarQube webhook in Sonar server:
  `http://localhost:8080/sonarqube-webhook/`

Optional Maven command used in Jenkins:

```bash
mvn sonar:sonar
```

## Kubernetes CD manifests

The Kubernetes files are CD preparation only:

- Docker Hub image is pushed first
- Kubernetes pulls the image from Docker Hub
- Deployment and Service are created in namespace `freelancy`
- centralized CD manifests are available in root folder `k8s/`

Apply manifests:

```bash
kubectl create namespace freelancy
kubectl apply -f ../k8s/namespace.yaml
kubectl apply -f ../k8s/deployments/
kubectl apply -f ../k8s/services/
kubectl apply -f ../k8s/monitoring/
kubectl get pods -n freelancy
kubectl get svc -n freelancy
kubectl logs <pod-name> -n freelancy
minikube service blog-service -n freelancy --url
```

Important:

- this service deployment already uses the required values:
  `SPRING_DATASOURCE_URL=jdbc:mysql://192.168.56.1:3306/microservices_db?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false`
  `SPRING_DATASOURCE_USERNAME=root`
  `SPRING_DATASOURCE_PASSWORD=`
  `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://192.168.56.10:8761/eureka/`

## Jenkins CD for this service

This service also has `Jenkinsfile-CD` with the required order:

- Checkout
- Docker pull
- Deploy to Kubernetes namespace `freelancy`
- Rollout status
- Smoke test

## Monitoring preparation

Actuator and Prometheus registry are enabled so these endpoints are available:

- `/actuator/health`
- `/actuator/info`
- `/actuator/prometheus`

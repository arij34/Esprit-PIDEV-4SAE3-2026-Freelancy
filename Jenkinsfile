pipeline {
    agent any

    environment {
        DOCKER_IMAGE  = "ameni221/user-service:v${BUILD_NUMBER}"
        SERVICE_DIR   = "user"
        SONAR_ORG     = "ameny323"
        SONAR_PROJECT = "ameny323_user-service"
    }

    stages {
        stage('Git Checkout') {
            steps {
                git(
                    url: 'https://github.com/arij34/Esprit-PIDEV-4SAE3-2026-Freelancy.git',
                    branch: 'main',
                    credentialsId: 'github-credentials'
                )
            }
        }

        stage('Build with Maven') {
            steps {
                dir("${SERVICE_DIR}") {
                    sh 'mvn clean install -DskipTests'
                }
            }
        }

        stage('Unit Tests') {
            steps {
                dir("${SERVICE_DIR}") {
                    sh 'mvn test'
                }
            }
            post {
                always {
                    junit "${SERVICE_DIR}/target/surefire-reports/*.xml"
                }
            }
        }

        stage('SonarCloud Analysis') {
            steps {
                dir("${SERVICE_DIR}") {
                    withCredentials([string(credentialsId: 'jenkins-sonar', variable: 'SONAR_TOKEN')]) {
                        sh """
                            mvn sonar:sonar \
                            -Dsonar.login=${SONAR_TOKEN} \
                            -Dsonar.host.url=https://sonarcloud.io \
                            -Dsonar.organization=${SONAR_ORG} \
                            -Dsonar.projectKey=${SONAR_PROJECT} \
                            -Dsonar.projectName=user-service \
                            -Dsonar.java.source=17
                        """
                    }
                }
            }
        }

        stage('Docker Build') {
            steps {
                dir("${SERVICE_DIR}") {
                    sh "docker build -t ${DOCKER_IMAGE} ."
                }
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'docker-hub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh """
                        echo ${DOCKER_PASS} | docker login -u ${DOCKER_USER} --password-stdin
                        docker push ${DOCKER_IMAGE}
                    """
                }
            }
        }

        stage('Deploy') {
            steps {
                sh """
                    sed -i 's|ameni221/user-service:.*|${DOCKER_IMAGE}|' docker-compose.yml
                    docker-compose pull user-service
                    docker-compose up -d --no-deps user-service
                """
            }
        }
    }

    post {
        success { echo '✅ user-service deployed successfully.' }
        failure { echo '❌ Pipeline failed!' }
    }
}
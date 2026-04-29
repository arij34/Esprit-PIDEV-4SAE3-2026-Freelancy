pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "ameni221/front-end:v${BUILD_NUMBER}"
        SERVICE_DIR  = "frontend"
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

        stage('Install dependencies') {
            steps {
                dir("${SERVICE_DIR}") {
                    sh 'npm install'
                }
            }
        }

        stage('Build Angular') {
            steps {
                dir("${SERVICE_DIR}") {
                    sh 'npm run build -- --configuration production'
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
                    sed -i 's|ameni221/front-end:.*|${DOCKER_IMAGE}|' docker-compose.yml
                    docker-compose pull front-end
                    docker-compose up -d --no-deps front-end
                """
            }
        }
    }

    post {
        success { echo '✅ frontend deployed successfully.' }
        failure { echo '❌ Pipeline failed!' }
    }
}
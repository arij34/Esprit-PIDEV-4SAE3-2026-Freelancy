pipeline {
    agent any

    stages {
        stage('CLEAN') {
            steps {
                deleteDir()
            }
        }

        stage('GIT') {
            steps {
                git branch: 'Matching_Module',
                    url: 'https://github.com/arij34/Esprit-PIDEV-4SAE3-2026-Freelancy.git'
            }
        }

        stage('BUILD') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('TEST') {
            steps {
                sh 'mvn test'
            }
        }

        stage('SONARQUBE') {
            steps {
                sh 'mvn sonar:sonar'
            }
        }

        stage('DOCKER BUILD') {
            steps {
                sh 'docker build -t matching-module .'
            }
        }
    }

    post {
        success {
            echo 'Pipeline terminé avec succès !'
        }
        failure {
            echo 'Pipeline échoué !'
        }
    }
}
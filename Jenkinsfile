pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
        timestamps()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'git rev-parse HEAD'
                sh 'git branch --show-current'
            }
        }

        stage('Test') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean test --tests "com.project.hugme.infra.ai.intent.DocumentIntentFieldMapperTest" --no-daemon'
            }
        }
    }

    post {
        success {
            echo 'Backend Checkout / Test 성공'
        }

        failure {
            echo 'Backend Checkout / Test 실패'
        }
    }
}
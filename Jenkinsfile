pipeline {
    agent any

    environment {
        AWS_REGION = 'ap-northeast-2'
        ECR_REGISTRY = '852891424427.dkr.ecr.ap-northeast-2.amazonaws.com'
        IMAGE_NAME = 'hugme-backend'
    }

    options {
        skipDefaultCheckout(true)
        timestamps()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Prepare') {
            steps {
                script {
                    env.GIT_SHA = sh(
                        script: 'git rev-parse HEAD',
                        returnStdout: true
                    ).trim()

                    env.IMAGE_URI = "${env.ECR_REGISTRY}/${env.IMAGE_NAME}:${env.GIT_SHA}"
                }

                sh 'echo "GIT_SHA=$GIT_SHA"'
                sh 'echo "IMAGE_URI=$IMAGE_URI"'
            }
        }

        stage('Test') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean test --tests "com.project.hugme.infra.ai.intent.DocumentIntentFieldMapperTest" --no-daemon'
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t "$IMAGE_URI" .'
                sh 'docker image inspect "$IMAGE_URI" --format "{{.Id}}"'
            }
        }
    }

    post {
        success {
            echo 'Backend Test / Docker Build 성공'
        }

        failure {
            echo 'Backend Test / Docker Build 실패'
        }
    }
}
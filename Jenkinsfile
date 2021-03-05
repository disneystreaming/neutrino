pipeline {
    agent {
        docker {
            image 'azul/zulu-openjdk:8'
        }
    }
    stages {
        stage('build') {
            steps {
                sh "./gradlew clean build"
            }
        }
    }
    post {
        always {
            cleanWs()
        }
    }
}

pipeline {
    agent { dockerfile true }
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

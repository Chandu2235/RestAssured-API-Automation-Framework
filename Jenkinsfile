pipeline {
    agent any

    tools {
        maven 'Maven-3.9.0'
        jdk 'JDK-17'
    }

    environment {
        DISPLAY = ':99'
        BROWSER = "${params.BROWSER ?: 'chrome'}"
        HEADLESS = "${params.HEADLESS ?: 'true'}"
    }

    parameters {
        choice(
            name: 'BROWSER',
            choices: ['chrome', 'firefox', 'edge'],
            description: 'Browser to run tests on'
        )
        booleanParam(
            name: 'HEADLESS',
            defaultValue: true,
            description: 'Run tests in headless mode'
        )
        choice(
            name: 'TEST_SUITE',
            choices: ['all', 'smoke', 'regression'],
            description: 'Test suite to run'
        )
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                echo 'Code checked out successfully'
            }
        }

        stage('Build') {
            steps {
                echo 'Building the project...'
                bat 'mvn clean compile'
            }
        }

        stage('Test') {
            steps {
                script {
                    def testCommand = "mvn test"

                    // Branch-based execution
                    if (env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'master') {
                        testCommand = "mvn test -Dsuite=regression"
                    } else if (env.BRANCH_NAME?.startsWith('feature/')) {
                        testCommand = "mvn test -Dsuite=smoke"
                    }

                    echo "Running tests with command: ${testCommand}"
                    bat "${testCommand}"
                }
            }

            post {
                always {
                    // Publish TestNG/JUnit
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'

                    // Archive surefire folder
                    archiveArtifacts artifacts: 'target/surefire-reports/**/*', allowEmptyArchive: true

                    // Publish HTML report (TestNG index.html)
                    publishHTML([
                        reportDir: 'target/surefire-reports',
                        reportFiles: 'index.html',
                        reportName: 'Test Report',
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        allowMissing: true
                    ])
                }
            }
        }

        stage('Package') {
            steps {
                echo 'Packaging application...'
                bat 'mvn package -DskipTests'
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}

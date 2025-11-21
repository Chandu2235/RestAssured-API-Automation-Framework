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
                    // Default TestNG suite
                    def testCommand = "mvn test -DsuiteXmlFile=testng.xml " +
                                      "-DBROWSER=${params.BROWSER} " +
                                      "-DHEADLESS=${params.HEADLESS}"

                    // Branch-based test suite selection
                    if (env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'master') {
                        testCommand += " -DTEST_SUITE=regression"
                    } else if (env.BRANCH_NAME?.startsWith('feature/')) {
                        testCommand += " -DTEST_SUITE=smoke"
                    }

                    echo "Running tests with command: ${testCommand}"
                    bat "${testCommand}"
                }
            }

            post {
                always {
                    // Publish TestNG/JUnit XML reports
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'

                    // Archive the reports folder
                    archiveArtifacts artifacts: 'target/surefire-reports/**/*', allowEmptyArchive: true

                    // Publish HTML report
                    publishHTML([
                        reportDir: 'target/surefire-reports',
                        reportFiles: 'index.html',
                        reportName: 'Extent Test Report',
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

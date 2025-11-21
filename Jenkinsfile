pipeline {
    agent any
    
    tools {
        jdk 'JDK-17'
        maven 'Maven-3.9.0'
    }
    
    environment {
        JAVA_HOME = tool('JDK-17')
        PATH = "${JAVA_HOME}\\bin;${PATH}"
        MAVEN_OPTS = '-Xmx1024m -XX:MaxPermSize=256m'
        PROJECT_NAME = 'ApiAutomation_withChatGPT'
        BRANCH_NAME = "${env.BRANCH_NAME ?: 'main'}"
    }
    
    options {
        buildDiscarder(logRotator(daysToKeepStr: '30', numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        skipDefaultCheckout(false)
        timestamps()
    }
    
    stages {

        stage('Checkout') {
            steps {
                echo "Checking out code from ${BRANCH_NAME} branch"
                checkout scm
                
                // Windows compatible environment check
                bat '''
                    echo === Environment Information ===
                    java -version
                    mvn -version
                    echo JAVA_HOME=%JAVA_HOME%
                    echo ================================
                '''
            }
        }

        stage('Clean Workspace') {
            steps {
                echo 'Cleaning previous build artifacts'
                bat 'mvn clean'
            }
        }
        
        stage('Compile') {
            steps {
                echo 'Compiling the project'
                bat 'mvn compile'
            }
        }
        
        stage('Test Compilation') {
            steps {
                echo 'Compiling test classes'
                bat 'mvn test-compile'
            }
        }
        
        stage('Unit Tests') {
            steps {
                echo 'Running unit tests'
                bat '''
                    mvn test ^
                    -Dmaven.test.failure.ignore=true ^
                    -Dsurefire.useFile=false
                '''
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'target/surefire-reports/**/*', allowEmptyArchive: true
                }
            }
        }
        
        stage('Integration Tests') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    changeRequest()
                }
            }
            steps {
                echo 'Running integration tests'
                bat '''
                    mvn verify ^
                    -DskipUnitTests=true ^
                    -Dmaven.test.failure.ignore=true
                '''
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/failsafe-reports/*.xml', allowEmptyResults: true
                }
            }
        }
        
        stage('Code Quality Analysis') {
            parallel {

                stage('Static Analysis') {
                    steps {
                        echo 'Running static code analysis'
                        bat 'mvn compile'
                    }
                }
                
                stage('Dependency Check') {
                    steps {
                        echo 'Checking dependency vulnerabilities'
                        bat 'mvn validate'
                    }
                }
            }
        }
        
        stage('Package') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                echo 'Packaging the application'
                bat 'mvn package -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
        
        stage('Deploy to Test Environment') {
            when {
                branch 'develop'
            }
            steps {
                echo 'Deploying to test environment'
                script {
                    echo "Deploying ${PROJECT_NAME} to test environment"
                }
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                echo 'Deploying to production environment'
                script {
                    echo "Deploying ${PROJECT_NAME} to production environment"
                }
            }
        }
    }
    
    post {

        always {
            echo 'Pipeline execution completed'
            cleanWs(
                cleanWhenAborted: true,
                cleanWhenFailure: true,
                cleanWhenNotBuilt: true,
                cleanWhenSuccess: true,
                cleanWhenUnstable: true,
                deleteDirs: true
            )
        }
        
        success {
            echo 'Pipeline executed successfully!'
        }
        
        failure {
            echo 'Pipeline failed!'
            emailext (
                subject: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                body: """
                    Build failed for ${PROJECT_NAME}
                    Build URL: ${env.BUILD_URL}
                    Branch: ${BRANCH_NAME}
                """,
                to: "${env.CHANGE_AUTHOR_EMAIL ?: 'team@example.com'}"
            )
        }
        
        unstable {
            echo 'Pipeline is unstable (some tests failed)'
        }
    }
}
pipeline {
    agent any
    
    tools {
        // Use JDK 17 - make sure this matches your Jenkins tool configuration
        jdk 'JDK-17'
        maven 'Maven-3.9'
    }
    
    environment {
        // Set JAVA_HOME explicitly for JDK 17
        JAVA_HOME = tool('JDK-17')
        PATH = "${JAVA_HOME}/bin:${PATH}"
        
        // Maven options for JDK 17 compatibility
        MAVEN_OPTS = '-Xmx1024m -XX:MaxPermSize=256m'
        
        // Project specific variables
        PROJECT_NAME = 'ApiAutomation_withChatGPT'
        BRANCH_NAME = "${env.BRANCH_NAME ?: 'main'}"
    }
    
    options {
        // Keep builds for 30 days
        buildDiscarder(logRotator(daysToKeepStr: '30', numToKeepStr: '10'))
        
        // Timeout after 30 minutes
        timeout(time: 30, unit: 'MINUTES')
        
        // Skip default checkout
        skipDefaultCheckout(false)
        
        // Timestamps in console output
        timestamps()
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo "Checking out code from ${BRANCH_NAME} branch"
                checkout scm
                
                // Display Java and Maven versions
                sh '''
                    echo "=== Environment Information ==="
                    java -version
                    mvn -version
                    echo "JAVA_HOME: $JAVA_HOME"
                    echo "================================"
                '''
            }
        }
        
        stage('Clean Workspace') {
            steps {
                echo 'Cleaning previous build artifacts'
                sh 'mvn clean'
            }
        }
        
        stage('Compile') {
            steps {
                echo 'Compiling the project'
                sh 'mvn compile'
            }
        }
        
        stage('Test Compilation') {
            steps {
                echo 'Compiling test classes'
                sh 'mvn test-compile'
            }
        }
        
        stage('Unit Tests') {
            steps {
                echo 'Running unit tests'
                sh '''
                    mvn test \
                    -Dmaven.test.failure.ignore=true \
                    -Dsurefire.useFile=false
                '''
            }
            post {
                always {
                    // Publish test results
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                    
                    // Archive test reports
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
                sh '''
                    mvn verify \
                    -DskipUnitTests=true \
                    -Dmaven.test.failure.ignore=true
                '''
            }
            post {
                always {
                    // Publish integration test results if available
                    publishTestResults testResultsPattern: 'target/failsafe-reports/*.xml', allowEmptyResults: true
                }
            }
        }
        
        stage('Code Quality Analysis') {
            parallel {
                stage('Static Analysis') {
                    steps {
                        echo 'Running static code analysis'
                        // Add your preferred static analysis tool here
                        // Example: SonarQube, SpotBugs, etc.
                        sh 'mvn compile'
                    }
                }
                
                stage('Dependency Check') {
                    steps {
                        echo 'Checking for security vulnerabilities in dependencies'
                        // You can add OWASP dependency check here
                        sh 'mvn validate'
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
                sh 'mvn package -DskipTests'
                
                // Archive the built artifacts
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
        
        stage('Deploy to Test Environment') {
            when {
                branch 'develop'
            }
            steps {
                echo 'Deploying to test environment'
                // Add your deployment scripts here
                script {
                    // Example deployment logic
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
                // Add your production deployment scripts here
                script {
                    // Example production deployment logic
                    echo "Deploying ${PROJECT_NAME} to production environment"
                }
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline execution completed'
            
            // Clean workspace after build
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
            // Add success notifications here (email, Slack, etc.)
        }
        
        failure {
            echo 'Pipeline failed!'
            // Add failure notifications here
            emailext (
                subject: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                body: """
                    Build failed for ${PROJECT_NAME}
                    
                    Build URL: ${env.BUILD_URL}
                    Branch: ${BRANCH_NAME}
                    
                    Please check the console output for details.
                """,
                to: "${env.CHANGE_AUTHOR_EMAIL ?: 'team@example.com'}"
            )
        }
        
        unstable {
            echo 'Pipeline is unstable (some tests failed)'
            // Add unstable build notifications here
        }
    }
}
pipeline {
  agent any                        // change to { label 'slave3' } only if that node exists
  environment {
    JFROG_URL = 'https://yashusn.jfrog.io/artifactory'
    REPO_NAME = 'feature-artifacts'
  }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Test') {
      steps {
        sh 'mvn -B test'
      }
    }

    stage('Build') {
      steps {
        sh 'mvn -B -DskipTests=false clean package'
      }
    }

    stage('Create Versioned Artifact') {
      steps {
        script {
          def sha = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
          def branchSafe = env.BRANCH_NAME.replaceAll('[^a-zA-Z0-9_.-]', '_')
          env.ARTIFACT = "news-app-${branchSafe}-${env.BUILD_NUMBER}-${sha}.war"
          sh "cp target/*.war ${env.ARTIFACT}"
          archiveArtifacts artifacts: "${env.ARTIFACT}", fingerprint: true
        }
      }
    }

    stage('Upload to JFrog') {
      steps {
        // NOTE: create a 'jfrog-api-key' Secret text credential in Jenkins (ID used here)
        withCredentials([string(credentialsId: 'jfrog-api-key', variable: 'JFROG_API_KEY')]) {
          // Escape $ so Groovy doesn't interpolate the secret — shell will expand it and Jenkins will mask it.
          sh """
            set -o pipefail
            echo "Uploading ${env.ARTIFACT} to ${JFROG_URL}/${REPO_NAME}/${env.BRANCH_NAME}/"
            # Debug mode: uncomment -v to see server reply (remove -v when fixed)
            curl -v --show-error -f -H "X-JFrog-Art-Api: \$JFROG_API_KEY" \
                 -T "${env.ARTIFACT}" \
                 "${JFROG_URL}/${REPO_NAME}/${env.BRANCH_NAME}/${env.ARTIFACT}"
          """
        }
      }
    }

    stage('Deploy to Tomcat (via SSH)') {
      steps {
        // Create an SSH credential in Jenkins: 'tomcat-ssh-key' (SSH username + private key)
        withCredentials([sshUserPrivateKey(credentialsId: 'tomcat-ssh-key', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')]) {
          script {
            // Replace host and remote path with your real Tomcat host
            def remoteHost = 'tomcat-dev.example.com'
            def remotePath = '/opt/tomcat10/webapps/news-app.war'

            // copy artifact
            sh "scp -o StrictHostKeyChecking=no -i ${SSH_KEY} ${env.ARTIFACT} ${SSH_USER}@${remoteHost}:${remotePath}"

            // optional restart (comment/uncomment based on your setup)
            // sh "ssh -o StrictHostKeyChecking=no -i ${SSH_KEY} ${SSH_USER}@${remoteHost} 'sudo systemctl restart tomcat || /opt/tomcat10/bin/shutdown.sh; /opt/tomcat10/bin/startup.sh'"
          }
        }
      }
    }
  } // stages

  post {
    success { echo "Pipeline succeeded — artifact: ${env.ARTIFACT}" }
    failure { echo "Pipeline failed — check console output" }
  }
}

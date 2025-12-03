pipeline {
  agent { label 'slave3' }
  environment {
    JFROG_URL = 'https://yashusn.jfrog.io/artifactory'
    REPO_NAME = 'news-app-libs-snapshot-local'
  }
  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build') {
      steps {
        // Run your build to produce target/*.war
        sh 'mvn -B clean package'
      }
    }

    stage('Create Versioned Artifact') {
      steps {
        script {
          // short commit SHA
          def sha = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
          // sanitize branch name
          def branchSafe = (env.BRANCH_NAME ?: 'no-branch').replaceAll('[^a-zA-Z0-9_.-]', '_')
          env.ARTIFACT = "news-app-${branchSafe}-${env.BUILD_NUMBER}-${sha}.war"

          // Ensure target exists
          sh 'mkdir -p target'

          // Find the built war
          def warFiles = sh(script: "ls target/*.war 2>/dev/null || true", returnStdout: true).trim()
          if (!warFiles) {
            error "No WAR produced in target/ — build failed or produced no .war files."
          }

          // If more than one war, pick the first; otherwise pick the single war
          def warToUse = warFiles.split('\\n')[0].trim()

          // Copy/rename into workspace root (or keep under target)
          sh "cp \"${warToUse}\" \"${env.ARTIFACT}\""

          // Archive artifact
          archiveArtifacts artifacts: "${env.ARTIFACT}", fingerprint: true
          echo "Created artifact: ${env.ARTIFACT}"
        }
      }
    }

    stage('Upload to JFrog') {
      steps {
        withCredentials([string(credentialsId: 'JFROG_API_KEY', variable: 'JFROG_API_KEY')]) {
          sh """
            set -o pipefail
            curl -f -H "X-JFrog-Art-Api: ${JFROG_API_KEY}" \\
                 -T "${env.ARTIFACT}" \\
                 "${JFROG_URL}/${REPO_NAME}/${env.BRANCH_NAME}/${env.ARTIFACT}"
          """
        }
      }
    }
  }
}

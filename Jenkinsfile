pipeline {
  agent { label 'slave3' }
	environment {
    JFROG_URL = 'https://yashusn.jfrog.io/artifactory'
    REPO_NAME = 'news-app-libs-snapshot-local'      // JFrog repo for feature branches
  }
	stages {
	 stage('Create Versioned Artifact') {
      steps {
        script {
          def branchSafe = env.BRANCH_NAME.replaceAll('[^a-zA-Z0-9_.-]', '_')

          env.ARTIFACT = "news-app-${branchSafe}-${env.BUILD_NUMBER}"

          sh "cp /home/ubuntu/workspace/news-app-Job_feature-1/target/*.war ${env.ARTIFACT}-f1.war"
		  sh "cp /home/ubuntu/workspace/news-app-Job_feature-2/target/*.war ${env.ARTIFACT}-f2.war"
		  
          archiveArtifacts artifacts: "${env.ARTIFACT}-f1.war", fingerprint: true
			archiveArtifacts artifacts: "${env.ARTIFACT}-f2.war", fingerprint: true
        }
      }
    }

    stage('Upload to JFrog') {
      steps {
        withCredentials([string(credentialsId: 'JFROG_API_KEY', variable: 'JFROG_API_KEY')]) {
          sh """
            curl -f -H "X-JFrog-Art-Api: ${JFROG_API_KEY}" \
                -T "${env.ARTIFACT}-f1.war" \
                "${JFROG_URL}/${REPO_NAME}/${env.BRANCH_NAME}/${env.ARTIFACT}-f1.war"
          """
		  sh """
            curl -f -H "X-JFrog-Art-Api: ${JFROG_API_KEY}" \
                -T "${env.ARTIFACT}-f2.war" \
                "${JFROG_URL}/${REPO_NAME}/${env.BRANCH_NAME}/${env.ARTIFACT}-f2.war"
          """
        }
      }
    }
    }
}

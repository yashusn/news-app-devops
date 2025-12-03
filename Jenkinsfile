pipeline {
  agent { label 'slave3' }
	environment {
    JFROG_URL = 'https://yashusn.jfrog.io/artifactory'
    REPO_NAME = 'news-app-libs-snapshot-local'      // JFrog repo for feature branches
    //feature1_war = "/home/ubuntu/workspace/"
    //feature2_war = '/home/ubuntu/workspace/news-app-Job_feature-2/target/news-app.war"
         }
  
 stages {
   stages {
	 stage('Checkout') {
      steps {
        checkout scm
      }
    }
	  stage('Create Versioned Artifact') {
      steps {
		  sh "sudo cp /home/ubuntu/workspace/news-app-Job_feature-1/target/news-app.war /home/ubuntu/workspace/news-app-Job_feature-3/target/"
        script {
          def sha = sh(
            script: 'git rev-parse --short HEAD',
            returnStdout: true
          ).trim()

          def branchSafe = env.BRANCH_NAME.replaceAll('[^a-zA-Z0-9_.-]', '_')

          env.ARTIFACT = "news-app-${branchSafe}-${env.BUILD_NUMBER}-${sha}.war"

          sh "cp target/*.war ${env.ARTIFACT}"
          archiveArtifacts artifacts: "${env.ARTIFACT}", fingerprint: true
        }
      }
    }

    stage('Upload to JFrog') {
      steps {
        withCredentials([string(credentialsId: 'JFROG_API_KEY', variable: 'JFROG_API_KEY')]) {
          sh """
            curl -f -H "X-JFrog-Art-Api: ${JFROG_API_KEY}" \
                -T "${env.ARTIFACT}" \
                "${JFROG_URL}/${REPO_NAME}/${env.BRANCH_NAME}/${env.ARTIFACT}"
          """
        }
      }
    }
    }
}

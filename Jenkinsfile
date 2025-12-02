pipeline {
  agent { label 'slave3' }
 stages {
    stage('Test') {
      steps {
        sh 'mvn test'
      }
    }

    stage('Build') {
      steps {
        sh 'mvn clean package'
      }
    }
    stage('Deploy to Tomcat') {
      steps {
			sh "sudo rm -rf /opt/tomcat10/webapps/news-app"
			//sudo rm /opt/tomcat10/webapps/news-app.war
			sh "sudo cp /home/ubuntu/workspace/news-app-Job_feature-2/target/news-app.war /opt/tomcat10/webapps"
		  	sh "sudo /opt/tomcat10/bin/shutdown.sh"
			sh "sudo /opt/tomcat10/bin/startup.sh"
        }
      }
    }
}

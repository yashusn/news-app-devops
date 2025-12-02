pipeline {
  agent { label 'slave3' }

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
			sudo rm -rf /opt/tomcat10/webapps/news-app
			sudo rm /opt/tomcat10/webapps/news-app.war
			sh "sudo cp /home/ubuntu/workspace/job_hello_word_jenkin/target/hello-world-war-1.0.0.war /opt/apache-tomcat-10.1.49/webapps"
        }
      }
    }

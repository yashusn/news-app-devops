pipeline {
agent { label 'slave3' }
stages { 
    stage ('hello-world-war') {
        
        //stage('Checkout') {
		//agent { label 'Java' }
            steps {
			//{
				//sh "rm -rf hello-world-war"
               //sh "git clone https://github.com/yashusn/news-app-devops.git"
            //}
        //}
		//}
		stage('test') {
            steps {
                sh "mvn test"
              }
        }
        stage('Build') {
            steps {
                sh "mvn clean package"
              }
        }
        stage('Deploy') {
            steps {
                sh "sudo cp /home/ubuntu/news-app-devops/target/news-app-devops.war /opt/tomcat10/webapps"
                           }
        }
    
}
}
}

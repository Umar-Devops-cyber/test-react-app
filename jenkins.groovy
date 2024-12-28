pipeline {
    agent {label "test-agent"}
    
    environment {
        DOCKER_IMAGE = "react-app:latest"
        SLACK_CHANNEL= "#jenkins-department"
    }
    
    stages{
        
        stage ("code"){
            
            steps{
                
                echo "This is clodng the code from Github"
                git url: "https://github.com/Umar-Devops-cyber/test-react-app.git", branch: "docker-build"
                echo "code clonned successfully"
            }
            
        }
            
        stage ("build"){
            
            steps{
                
                echo "THis is building the application via npm  from code"
                sh "whoami"
                sh "docker build --no-cache -t ${DOCKER_IMAGE} ."
                sh 'docker image prune -f --filter "dangling=true"'
                echo "Production image built successfully, and unused images removed"
                echo "image build successfully"
                
            }
        }
        
        stage("Scan") {
            steps {
                echo "Scanning for vulnerabilities with Trivy"
                sh """
                trivy image --exit-code 1 --severity HIGH,CRITICAL ${DOCKER_IMAGE}
                """
                }
            
        }
        
        stage ("test"){
            
            steps{
                
                echo "This is testing the build application"
            }
            
            
        }
        
        stage ("Pushed to Docker hub"){
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                    sh """
                    docker login -u \$DOCKER_USERNAME -p \$DOCKER_PASSWORD
                    docker tag react-app:latest \$DOCKER_USERNAME/react-app:latest
                    docker push \$DOCKER_USERNAME/react-app:latest
                    docker rmi $(docker images)
                    """
    
            
                }
                
            }
        }

          
        stage ("deploy"){
            
            steps{
                
                echo "THis is deploying the tested code"
                sh 'docker-compose up -d'
                echo "Docker container run successfully"

            }
            
        }
        
             
    }
    
    post {
        success {
            // Send Slack notification on success
            slackSend(
                channel: "${SLACK_CHANNEL}",
                message: "Pipeline Success: The build has completed successfully. :white_check_mark:",
                color: "good"
            )
        }

        failure {
            // Send Slack notification on failure
            slackSend(
                channel: "${SLACK_CHANNEL}",
                message: "Pipeline Failed: The build has failed. :x:",
                color: "danger"
            )
        }
    }
    
     
}
//end of pipeline here

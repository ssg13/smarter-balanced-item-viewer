#!/bin/bash

#enable access to EPEL managed repos for Redis
sudo yum-config-manager --enable epel
#Run updates
sudo yum update -y
#Install Redis
sudo yum install redis -y
#Install Java 8
sudo yum install java-1.8.0-openjdk-devel -y
#Install AWS Code Deploy Agent and Dependencies
sudo yum install ruby -y
sudo yum install wget -y
wget --directory-prefix=/home/ec2-user https://aws-codedeploy-us-west-2.s3.amazonaws.com/latest/install
chmod +x /home/ec2-user/install
sudo /home/ec2-user/install auto
#The service should start after installation but this makes sure.
sudo service codedeploy-agent start
redis-server &
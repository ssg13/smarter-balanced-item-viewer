#! /bin/bash

{
# Install pip, awscli
curl -O https://bootstrap.pypa.io/get-pip.py
sudo python get-pip.py
sudo pip install awscli

S3_CONFIG=cass-tomcat-config
S3_WAR_DEPLOY=cass-sb-itemviewerservice-war-deployment
ITEM_VWR_SRVC_WAR=itemviewerservice.war
DICTIONARY_WAR=Dictionary.war
CONFIG_ZIP=config.zip
CONFIG=config

aws s3 cp s3://$S3_WAR_DEPLOY/$ITEM_VWR_SRVC_WAR $ITEM_VWR_SRVC_WAR
aws s3 cp s3://$S3_WAR_DEPLOY/$DICTIONARY_WAR $DICTIONARY_WAR
aws s3 cp s3://$S3_CONFIG/$CONFIG_ZIP $CONFIG_ZIP

sudo apt-get update && sudo apt-get install -y openjdk-7-jdk tomcat7 tomcat7-admin nginx unzip 

sudo mkdir -p /home/tomcat7/content
sudo chown -R tomcat7:tomcat7 /home/tomcat7
sudo chown -R tomcat7:tomcat7 /usr/share/tomcat7

unzip $CONFIG_ZIP > /dev/null

# Tomcat config files
TOMCAT_CONF_DIR=/etc/tomcat7/
sudo cp -f $CONFIG/context.xml $TOMCAT_CONF_DIR
sudo cp -f $CONFIG/tomcat-users.xml $TOMCAT_CONF_DIR
sudo cp -f $CONFIG/web.xml $TOMCAT_CONF_DIR

sudo rm -rf /var/lib/tomcat7/webapps/ROOT
sudo cp -f $ITEM_VWR_SRVC_WAR /var/lib/tomcat7/webapps/ROOT.war
sudo cp -f $DICTIONARY_WAR /var/lib/tomcat7/webapps/

# Nginx config
sudo mv -f $CONFIG/nginx_config /etc/nginx/sites-available/default

sudo service tomcat7 restart
sudo service nginx restart
} 1> /home/ubuntu/init_output.txt 2> /home/ubuntu/init_errors.txt # Redirect stdout/stderr to files

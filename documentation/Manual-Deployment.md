## Overview
This project is hosted on Amazon Web Services EC2 using Ubuntu 14.04.

## Manual Deployment

### AWS Prerequisites
- Create a security group to allow access to certain ports:

#### Inbound
| Type | Protocol | Port Range | Source |
| --- | --- | --- | --- |
|HTTP | TCP | 80 | 0.0.0.0/0 |
| SSH | TCP | 22 | 0.0.0.0/0 | 

#### Outbound
| Type | Protocol | Port Range | Source |
| --- | --- | --- | --- |    
| All Traffic | All | All | 0.0.0.0/0 | 

### AWS Setup
Launch an Amazon Web Services instance with the following configurations:

1. Use AMI: Ubuntu Server 14.04 LTS (HVM), SSD Volume Type (ami-d732f0b7).
2. Select a suitable instance size.
6. Select ```Review and Launch```.
7. Next to ```Security Groups```, select ```Edit Security Groups``` and add the security group created in the __Prerequisites__ section.
8. Launch your instance.

### Installation
In the AWS instance launched, update packages:
```sudo apt-get update```

- Install openjdk-7:
```sudo apt-get install openjdk-7-jdk```

- Install tomcat7 and tomcat7-admin:
```sudo apt-get install tomcat7 tomcat7-admin```

- Install nginx for port forwarding:
```sudo apt-get install nginx```

### Tomcat Configuration
- Create a directory for tomcat give it permissions:

```sudo mkdir -p /home/tomcat7/content```

```sudo chown -R tomcat7:tomcat7 /home/tomcat7```

```sudo chown -R tomcat7:tomcat7 /usr/share/tomcat7```

- Update the tomcat configuration files:

1. Create the file ```/etc/tomcat7/context.xml``` with the following text:

__TODO__ Put this file somewhere to copy, too long to paste here
    
2. Create the file ```/etc/tomcat7/tomcat-users.xml``` with the following text (be sure to add a password):

```
<?xml version="1.0" encoding="UTF-8"?>
<!-- Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License. -->
-<tomcat-users version="1.0" xsi:schemaLocation="http://tomcat.apache.org/xml tomcat-users.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://tomcat.apache.org/xml">
<role rolename="manager"/>
<role rolename="admin"/>
<role rolename="manager-gui"/>
<role rolename="manager-script"/?
<user roles="admin,manager,manager-gui,manager-script" password="" username="admin"/>
</tomcat-users>
```

3. Create the file ```/etc/tomcat7/web.xml``` with the following text":

__TODO__ Put this file somewhere to copy, too long to paste here

### Deploying the ```.war``` file
- Remove the tomcat ROOT file, copy ```itemviewerservice.war``` file to tomcat's webapps directory:

```sudo rm -rf /var/lib/tomcat7/webapps/ROOT```

```sudo mv -f itemviewerservice.war /var/lib/tomcat7/webapps/ROOT.war```


### Configure nginx
- Replace ```/etc/nginx/sites-available/default``` with the following text (requires root permissions):
```
server {
    listen 80;
    location / {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection keep-alive;
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }
}
```

### Launch Application
- Restart tomcat7 and nginx:

```sudo service tomcat7 restart```

```sudo service nginx restart```
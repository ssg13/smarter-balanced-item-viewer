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

- Create an IAM role that grants access to the S3 bucket containing the content packages

### AWS Setup
Launch an Amazon Web Services instance with the following configurations:

1. Use AMI: Ubuntu Server 14.04 LTS (HVM), SSD Volume Type (ami-d732f0b7).
2. Select a suitable instance size.
3. Select `Next: COnfigure Instance Details`
4. Add the IAM role that grants S3 bucket access
5. Select ```Review and Launch```.
6. Next to ```Security Groups```, select ```Edit Security Groups``` and add the security group created in the __Prerequisites__ section.
7. Launch your instance.

### Installation
In the AWS instance launched, update packages:
```apt-get update```

- Install openjdk-7:
```apt-get install openjdk-7-jdk```

- Install tomcat7 and tomcat7-admin:
```apt-get install tomcat7 tomcat7-admin```

- Install nginx for port forwarding:
```apt-get install nginx```

### Tomcat Configuration
- Create a directory for tomcat give it permissions:

```mkdir -p /home/tomcat7/content```

```chown -R tomcat7:tomcat7 /home/tomcat7```

```chown -R tomcat7:tomcat7 /usr/share/tomcat7```

- Update the tomcat configuration files:

1. Modify the file ```/etc/tomcat7/context.xml```, adding the following text:
```xml
<Parameter name="tds.iris.EncryptionKey" override="false" value="24 characters alphanumeric Encryption key" />
<Parameter name="tds.dictionary.key.TDS_Dict_Collegiate" override="false" value="---Key for Merriam-Webster Dictionary ---"/>
<Parameter name="tds.dictionary.url.TDS_Dict_Collegiate" override="false" value="http://www.dictionaryapi.com/api/v1/references/collegiate/xml/"/>
<Parameter name="tds.dictionary.key.TDS_Dict_Learners" override="false" value="---Key for Dictionary ---"/>
<Parameter name="tds.dictionary.url.TDS_Dict_Learners" override="false" value="http://www.dictionaryapi.com/api/v1/references/learners/xml/"/>
<Parameter name="tds.dictionary.key.TDS_Dict_SD2" override="false" value="---Key for Dictionary ---"/>
<Parameter name="tds.dictionary.url.TDS_Dict_SD2" override="false" value="http://www.dictionaryapi.com/api/v1/references/sd2/xml/"/>
<Parameter name="tds.dictionary.key.TDS_Dict_SD3" override="false" value="---Key for Dictionary ---"/>
<Parameter name="tds.dictionary.url.TDS_Dict_SD3" override="false" value="http://www.dictionaryapi.com/api/v1/references/sd3/xml/"/>
<Parameter name="tds.dictionary.key.TDS_Dict_SD4" override="false" value="---Key for Dictionary ---"/>
<Parameter name="tds.dictionary.url.TDS_Dict_SD4" override="false" value="http://www.dictionaryapi.com/api/v1/references/sd4/xml/"/>
<Parameter name="tds.dictionary.key.thesaurus" override="false" value="---Key for Dictionary ---"/>
<Parameter name="tds.dictionary.url.thesaurus" override="false" value="http://www.dictionaryapi.com/api/v1/references/ithesaurus/xml/"/>
<Parameter name="tds.dictionary.key.spanish" override="false" value="---Key for Dictionary ---"/>
<Parameter name="tds.dictionary.url.spanish" override="false" value="http://www.dictionaryapi.com/api/v1/references/spanish/xml/"/>
```

2. Modify the file ```/etc/tomcat7/web.xml```, adding the following text":
```xml
<filter>
  <filter-name>CorsFilter</filter-name>
  <filter-class>org.apache.catalina.filters.CorsFilter</filter-class>
</filter>
<filter-mapping>
  <filter-name>CorsFilter</filter-name>
  <url-pattern>/*</url-pattern>
</filter-mapping>
```

### Deploying the ```.war``` files
- Remove the tomcat ROOT file, move ```itemviewerservice.war``` file to tomcat's webapps directory
- Move the `Dictionary.war` file to tomcat's webapps directory

```rm -rf /var/lib/tomcat7/webapps/ROOT```

```mv -f itemviewerservice.war /var/lib/tomcat7/webapps/ROOT.war```

`mv -f Dictionary.war /var/lib/tomcat7/webapps/Dictionary.war`


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

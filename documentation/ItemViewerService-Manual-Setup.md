# Item Viewer Service Manual Setup
To build and run the item viewer service you will need the following.

# Building The Item Viewer Service

## Compile time Dependencies
Compile time dependencies are built into the Maven POM file.
Java 7 is required to build and run the Item Viewer Service.

The Item Viewer Service depends on the Iris and Student Library packages of TDS_Student.

### Run time Dependencies
- Apache Tomcat 7 or newer
- Amazon Web Services S3.
- Smarter Balanced Dictionary
- Read and write access to the local file system


## Configuration
### Item Viewer Service
#### General Configuration
The itemviewerservice.properties file has two variables that need to be set. It uses the Java properties file format.
The name of the Amazon S3 bucket you are using to store content packages in needs to be set for the `S3bucket` variable. The region the bucket is in needs to be set for the `S3region` variable.

#### Logging
The item viewer service uses [SLF4J](http://www.slf4j.org/) bound to [Logback Classic](http://logback.qos.ch/) for logging. The log settings are found in logback.xml. For basic logging to a file you will need to set the file location for the file appender. For a full reference on configuring the log output levels and locations please refer to the Logback Classic [documentation](http://logback.qos.ch/manual/configuration.html).

#### Amazon Web Services
 The item viewer service uses Amazon's default credentials provider chain to authenticate with AWS S3.
 If the AWS S3 bucket is not publicly accessible AWS credentials will need to be specified using Amazon's default credential provider chain.
 Please see Amazon's [documentation](https://docs.aws.amazon.com/java-sdk/latest/developer-guide/credentials.html#id6) for more details.

 Content packages should have an MD5 checksum set on their custom metadata in Amazon S3. The key metadata key must be `x-amz-meta-md5`. The value must be the MD5 checksum for the content zip.
 To generate the md5 checksum for a file on Linux you can use the `md5sum $FILE` command where `$FILE` is the name of the file you want to generate the MD5 checksum for. To generate the md5 checksum for a file on Windows you can use the `certutil -hashfile $FILE MD5` command where `$FILE` is the name of the file you want to generate the MD5 checksum for.
 The `certutil` command will insert spaces between the hexadecimal representation of the bytes in the MD5 checksum it outputs, this is not a part of the checksum and can be ignored. It is safe to set the MD5 checksum in the Amazon S3 metadata with the whitespace included. All whitespace is removed by the item viewer service before the MD5 checksum it calculates is compared with the MD5 checksum set in the Amazon S3 metadata.

 To add the required header field to a content package on S3.
 1. Select the content package zip file in S3  
 2. From the "Actions" drop down menu select "Properties"  
 3. Expand the "Metadata" area  
 4. Click "Add more metadata"  
 5. In the key field enter "x-amz-meta-md5" without quotes  
 6. In the value field enter the 32 character hexadecimal representation of the file's MD5 hash  
 7. Save the new key value pair  

#### Local System
The user running the item viewer service needs read and write permissions to their home directory. The application requires read and write permissions to the Iris content directory specified in settings-mysql.xml.

### Dictionary Configuration
The dictionary accommodation requires the Smarter Balanced Dictionary application to be running. To run the Smarter Balanced Dictionary deploy the WAR file in Tomcat and add the required configuration.

 The following parameters must be set in $TOMCAT_HOME/conf/context.xml under the context element.
```xml
<Parameter value="---Key for Merriam-Webster Dictionary ---" override="false" name="tds.dictionary.key.TDS_Dict_Collegiate"/>

<Parameter value="http://www.dictionaryapi.com/api/v1/references/collegiate/xml/" override="false" name="tds.dictionary.url.TDS_Dict_Collegiate"/>

<Parameter value="---Key for Dictionary ---" override="false" name="tds.dictionary.key.TDS_Dict_Learners"/>

<Parameter value="http://www.dictionaryapi.com/api/v1/references/learners/xml/" override="false" name="tds.dictionary.url.TDS_Dict_Learners"/>

<Parameter value="---Key for Dictionary ---" override="false" name="tds.dictionary.key.TDS_Dict_SD2"/>

<Parameter value="http://www.dictionaryapi.com/api/v1/references/sd2/xml/" override="false" name="tds.dictionary.url.TDS_Dict_SD2"/>

<Parameter value="efad70cc-b3ea-462f-b594-0d8294d5cc41" override="false" name="tds.dictionary.key.TDS_Dict_SD3"/>

<Parameter value="http://www.dictionaryapi.com/api/v1/references/sd3/xml/" override="false" name="tds.dictionary.url.TDS_Dict_SD3"/>

<Parameter value="86b89e2e-990c-40b6-834f-4f2195604ada" override="false" name="tds.dictionary.key.TDS_Dict_SD4"/>

<Parameter value="http://www.dictionaryapi.com/api/v1/references/sd4/xml/" override="false" name="tds.dictionary.url.TDS_Dict_SD4"/>

<Parameter value="---Key for Dictionary ---" override="false" name="tds.dictionary.key.thesaurus"/>

<Parameter value="http://www.dictionaryapi.com/api/v1/references/ithesaurus/xml/" override="false" name="tds.dictionary.url.thesaurus"/>

<Parameter value="---Key for Dictionary ---" override="false" name="tds.dictionary.key.spanish"/>

<Parameter value="http://www.dictionaryapi.com/api/v1/references/spanish/xml/" override="false" name="tds.dictionary.url.spanish"/>

<Parameter value="AIR" override="true" name="tds.dictionary.groupName"/>
```

The dictionary API call is made as a cross origin request. A CORS filter needs to be added to the Tomcat $TOMCAT_HOME/conf/web.xml file.

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

Tomcat has detailed [documentation](http://tomcat.apache.org/tomcat-8.0-doc/config/filter.html#CORS_Filter) on setting up CORS filtering. Please refer to it if you want to  set up a more detailed filter.



### Iris Configuration
Iris requires a 25 character alphanumeric numeric encryption key set as a parameter in $TOMCAT_HOME/conf/context.xml under the context element.
The entry follows the form `<Parameter value="YOUR KEY ENCRYPTION KEY HERE" override="false" name="tds.iris.EncryptionKey"/>`.

The `iris.ContentPath` variable in the settings-mysql.xml file needs to be set to the local directory where the content packages are going to be stored. If this directory does not exist, or the application can not access it, it will fail to launch.


In order to use the dictionary you need to set the `iris.DictionaryUrl` value in the settings-mysql.xml config file for the Iris.
This should be the URL of the Smarter balanced Dictionary that you deployed.

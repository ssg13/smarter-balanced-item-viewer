# Item Viewer Service Reference Documentation

## Dependencies
Compile time dependencies are built into the Maven POM file.

### Compile time
- Java 7

##### Smarter Balanced
- Iris
- Student Library

### Run time
- Tomcat 7 or newer
- Amazon Web Services S3.

## Configuration
- Item Viewer Service
- Iris
- Tomcat settings
- Logging


## Configuration
### Item Viewer Service
#### Logging
The item viewer service uses [SLF4J](http://www.slf4j.org/) bound to [Logback Classic](http://logback.qos.ch/) for logging. The log settings are found in logback.xml. For basic logging to a file you will need to set the file location for the file appender. For a full reference on configuring the log output levels and locations please refer to the Logback Classic [documentation](http://logback.qos.ch/manual/configuration.html).

#### Amazon Web Services
Amazon Web Services Simple Storage Service (AWS S3) is used as a location where content packages are hosted externally. The item viewer service uses Amazon's default credentials provider chain

#### Local System
The user running the item viewer service needs read and write permissions to their home directory.  The application requires read and write permissions to the Iris content directory specified in settings-mysql.xml.


### Iris
Iris requires a 25 character alphanumeric numeric encryption key set as a parameter in $TOMCAT_HOME/conf/context.xml under the context element.
The key follows the form `<Parameter value="YOUR KEY ENCRYPTION KEY HERE" override="false" name="tds.iris.EncryptionKey"/>`

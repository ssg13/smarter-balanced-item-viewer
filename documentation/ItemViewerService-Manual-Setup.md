# Item Viewer Service Manual Setup
To build and run it outside of Amazon web services you will need the following.

# Building The Item Viewer Service

## Compile time Dependencies
Compile time dependencies are built into the Maven POM file.
Java 7 is required to build and run the Item Viewer Service.

The Item Viewer Service depends on the Iris and Student Library packages of TDS_Student.

### Run time Dependencies
- Apache Tomcat 7 or newer
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
 The item viewer service uses Amazon's default credentials provider chain to authenticate with AWS S3.

#### Local System
The user running the item viewer service needs read and write permissions to their home directory.  The application requires read and write permissions to the Iris content directory specified in settings-mysql.xml.


### Iris Configuration
Iris requires a 25 character alphanumeric numeric encryption key set as a parameter in $TOMCAT_HOME/conf/context.xml under the context element.
The entry follows the form `<Parameter value="YOUR KEY ENCRYPTION KEY HERE" override="false" name="tds.iris.EncryptionKey"/>`.
The `iris.ContentPath` variable in the settings-mysql.xml file needs to be set to the location with the content packages.

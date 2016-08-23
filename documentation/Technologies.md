# Item Viewer Service

This document explains the main parts of the item viewer service and, its important dependencies.
The Item viewer service provides an API to load a single item and accommodations in a page. The item and accommodations are specified as URL parameters.
The Smarter Balanced Iris is used as a Maven WAR overlay to extend the scripts, styling and functionality of the Iris application into the item viewer service.
The Iris application displays a window for users with a text box where they can enter a JSON token to load an item and accommodations. The item and accommodations are loaded in an iFrame embedded in the page with the text box.
The iFrame with the items and accommodations is the front end part of the Iris that the item viewer service makes use of. It loads only the iFrame and selects which item and accommodations are loaded from the URL.

The item viewer service excludes some files from the Iris WAR overlay. It excludes the Iris web.xml file because it requires different servlet mappings. It excludes the JNA 3.0.9 jar because it causes a dependency conflict with the Operating System Hardware Information library which depends on JNA 4.2.2. Finally it excludes the IrisPages directory because it does not need the page templates it contains.

The item viewer service extends the Iris application by adding its own controllers for loading items and accommodations by URL, and the diagnostic API.
In the backend it adds the diagnostic API logic, accommodation code to type lookup, and a service that fetches content packages from Amazon Web Services S3.

## Item Viewer Service Modules
The Item Viewer service is divided into three layers, the App, the Core, and the Data Access Layer or dal.
Each layer is a Maven submodule that is part of the main item viewer service Maven application.

### App
The App module has the web application frontend parts and application configuration. It contains the web application controllers, JavaScript, page templates, and application configuration files.

#### Spring
Spring is used for controllers and scheduled services. The item viewer service uses a mixture of xml and annotation configuration for Spring. Controllers use the `@RequestMapping` annotation to map the item and diagnostic API URLs. The item viewer service servlet is configured with xml and used to map the item page template directory and .jsp file extension.


The controller for loading items is mapped to `/item/itemID`.
The item ID must match the regex `d+[-]d+`, that is one or more numbers, a dash, and one or more numbers in that order.
Optional accommodations are specified with the `isaap` URL parameter and are semicolon delimited. Only the accommodation code should be specified. Accommodation type is not specified.
For example, the URL to load item 200-12344 with the word list glossary and expandable passages would look like   http://viewer.smarterbalanced.org/item/200-12344?isaap=TDS_WL_Glossary;TDS_ExpandablePassages1.


The diagnostic API is mapped to /status per the diagnostic API [requirements](http://www.smarterapp.org/documents/DiagnosticApi.html). The diagnostic API supports levels 0 through 5 as required in the requirements. The diagnostic level is specified as a URL parameter. For example the level 3 diagnostic API would be accessed with http://viewer.smarterbalanced.org/status?level=3.

The service that polls Amazon Web Services S3 for updated content packages and downloads them to the local file system is run as a Spring scheduled service.
It is configured with annotations to run every 5 minutes after the previous run of the service has finished.

#### JavaScript
The item viewer service includes all of the JavaScript from Iris required to display items and accommodations.

#### Configuration
The App layer contains the logging

### Core
The Core module contains the application's business logic. It contains the diagnostic API, item request processing logic, and the tool that checks for updated content packages on Amazon S3.

#### Diagnostic API
The Diagnostic API is implemented using the requirements listed in the SmarterApp Web Diagnostic API [documentation](http://www.smarterapp.org/documents/DiagnosticApi.html).
It supports five levels of diagnostics; system, configuration, database read, database write, and external providers.
The system diagnostic uses the Operating System Hardware Information (OSHI) library to gather information on memory usage and file system space.
The configuration diagnostic checks for the existence of the application configuration file and configuration variables. The database read diagnostic makes sure the Iris content path variable is readable, and that it contains content items.
The database write diagnostic makes sure that the Iris content directory is writeable, then performs a write and a delete in the content directory.
The providers diagnostic checks the status of the word list handler, the black box, the item viewer service API, the Amazon S3 content bucket, and the content packages.
It performs an HTTP get request to get the status of the word list handler, black box, and item viewer service API.
It uses the Amazon AWS Java SDK to connect to Amazon S3 and get a list of content packages.

#### Item Request Translation
When the item viewer service receives a request for an item the request is translated into a JSON token that the Iris will accept.
The item viewer service parses the item bank and key out of the URL as well as any accommodation codes.
The Iris requires both the accommodation type and code for each accommodation. The item viewer service only requires codes.
A reverse lookup is performed to get the accommodation type for each accommodation code.
Finally the item bank and key, and accommodation types and codes are serialized into a JSON token that Iris can parse.

#### S3 Content Package Update Checker
The S3 content package update checker keeps the content packages in the Iris content directory up to date with the content packages stored in an Amazon S3 bucket.
It polls the Amazon S3 bucket configured in the application settings for new, changed or removed content packages, and fetches content packages from S3.
Spring is configured to run the S3 package update checker when the application starts, then every five minutes.
The five minute interval between runs is calculated from the completion of one update check to the start of another.

When the S3 content package update checker runs it checks for the existence of a package list file called "packageList" in the content directory. That file is used to track the last set of content packages that were downloaded from the S3 bucket. The package list file uses the Java configuration style key value pair format to track content package names and last modified time stamps as they appear in the S3 bucket. If that file does not exist, or the package names and time stamps do not match those in the S3 bucket when an update check is run, then the S3 update checker will refresh the contents of the local Iris content directory.


When the content package update checker refreshes the contents of the Iris content directory it downloads the contents of the S3 content package bucket to a temporary directory in the home directory of the user running the Item Viewer Service application.
The names and timestamps of the downloaded files are written to the "packageList" file. The zip archives that were downloaded are extracted into the temporary directory.
 All of the files in the Iris content directory are deleted and the contents of the temporary directory are moved to the Iris content directory.
 Finally, a request is sent to Iris to rescan the items in the content directory so it picks up the changes.

### Data Access Layer
The Data Access Layer contains the classes used to download content packages from Amazon S3, extract zip archives, and move files on the local file system.

#### Amazon File API
The Amazon File API is used to get the details of content package files stored on Amazon S3, and to download them to the local file system.

When downloading content packages the Amazon file API checks if there is an md5 sum in the S3 object metatadata of file. If there is an md5 sum is the md5 sum of the content package will be compared against that value when the file is downloaded.
If the sums do not match an error will be logged and the file will be redownloaded.
If the md5 sums still do not match another error is logged and the last download is kept.

#### Zip Archive Extraction
Java's built in Zip and File libraries are used to extract a zip archive to the local file system.

## Third Party Libraries

### Amazon Web Services Java SDK
The Amazon Web Services (AWS) Java SDK is used to connect to the AWS S3 service. It is used for authentication, and S3 file information and downloads.

### Apache Commons
The Apache Commons IO library is used in the data access layer to take the file stream provided by the AWS Java SDK S3 client and write the file to disk. It is used to transfer and delete files when updating the content on the local file system.


### Logback Classic
Logback classic is the logging framework used by the Iris.

### Jackson Databind
Jackson databind is used to serialize the data from the API call to item viewer service into a token that can be sent to the Iris.

### Operating System Hardware Information
The Operating system Hardware Information (OSHI) library is used by the system diagnostic to get information on total memory, memory usage, and file system size, usage and type.

### SLF4J
SLF4J is the logging facade used by Iris. It can be bound to a number of different logging frameworks. In the case of Iris and the item viewer service the logback classic logging framework is used.

### Spring
Spring is the web application framework used in Iris and other Smarter Balanced applications. The Item Viewer Service uses version 3.2.1 because that is the same version Iris uses.

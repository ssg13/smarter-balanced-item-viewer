# Item Viewer Service

## Item Viewer Service Modules
The Item Viewer service is divided into three layers, the App, the Core and the Data Access layer or dal.
Each layer is a Maven module.

### App
The App module contains the web application controllers, JavaScript, page templates, and application configuration files.

### Core
The Core module contains business logic.
#### Diagnostic API

#### Item Request Models

#### S3 Content Package Update Checker

### Data Access Layer
The Data Access Layer contains logic to.

## Smarter Balanced Libraries
### Iris
The Item Viewer Service uses the Iris as a WAR overlay to extend the functionality that the Iris application provides. The Iris application renders items into an iFrame.

## Third Party Libraries

### Amazon Web Services Java SDK


### Apache
The Item Viewer Service uses the Apache Commons IO and HTTP Client libraries.

### Logback Classic
Logback classic is the logging framework used in

### OSHI

### SLF4J

### Spring
Spring is the web application framework used in Iris and other Smarter Balanced applications. The Item Renderer uses version 3.2.1 because that is the same Version that Iris uses.

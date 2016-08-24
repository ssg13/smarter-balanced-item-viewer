# Building the IRiS
At the center of this API is a previously built Java item renderer that takes a Smarter Balanced Assessment item as input (a folder containing XML and sometimes media files) and outputs an HTML file that can be presented to users. To build IRiS, student release needs to be built. 

The build process for the IRiS and dependencies isn't well documented on the Smarter Balanced repo, so we've listed the instructions here. Contact @rutgerfarry if you have any trouble.

Special thanks to @rutgerfarry and @Garrett-S for build steps

##Before you start
Easiest way to get all the depencies is to install chocolatey
* Get and set java 7, choco install jdk7
* Get hg, choco install hg
* Have test items
  * TODO: Inlcude url for tds package
* Have access to and cloned
  * [SharedMultiJarDev](https://bitbucket.org/sbacoss/sharedmultijar_release)
  * [student_release](https://bitbucket.org/sbacoss/student_release)
  * [tdsdll_release](https://bitbucket.org/sbacoss/tdsdll_release)
  * [itemscoring_release](https://bitbucket.org/sbacoss/itemscoring_release)
  * [itemselectionshell_release](https://bitbucket.org/sbacoss/itemselectionshell_release)
  * [testscoring_release](https://bitbucket.org/sbacoss/testscoring_release)
  * [sharedbuild_release](https://bitbucket.org/sbacoss/sharedbuild_release)
  * [sharedcode_release](https://bitbucket.org/sbacoss/sharedcode_release)
  * [programmanagementclient_release](https://bitbucket.org/sbacoss/programmanagementclient_release)
  * [sharedsecurity_release](https://bitbucket.org/sbacoss/sharedsecurity_release)
  * [restapigenerator](https://bitbucket.org/sbacoss/restapigenerator_release)
  * [programmanagement_release](https://bitbucket.org/sbacoss/programmanagement_release)
  * [monitoringandalertingclient_release](https://bitbucket.org/sbacoss/monitoringandalertingclient_release)
  * [itemrenderer_release](https://bitbucket.org/sbacoss/itemrenderer_release)

## Pre-build instructions
* Update multijar's pom to have a dependency for the shared-test module

 ```xml
 <dependency> 
  <groupId>xml-apis</groupId>
  <artifactId>xml-apis</artifactId>
  <version>1.4.01</version>
 </dependency>
```

## Build Instructions
Tests are skipped if known to fail

```bash
mvn clean install -f ./sharedmultijar_release/pom.xml
mvn clean install -f ./student_release/ResourceBundler/pom.xml
mvn clean install -Dmaven.test.skip=true -f ./tdsdll_release/pom.xml
mvn clean install -Dmaven.test.skip=true -f ./itemscoring_release/pom.xml
mvn clean install -Dmaven.test.skip=true -f ./itemselectionshell_release/pom.xml
mvn clean install -Dmaven.test.skip=true -f ./testscoring_release/testscoring/pom.xml
mvn clean install -f ./student_release/SpellCheck/pom.xml
mvn clean install -f ./sharedbuild_release/pom.xml
mvn clean install -f ./sharedcode_release/pom.xml
mvn clean install -f ./programmanagementclient_release/pom.xml
mvn clean install -f ./sharedsecurity_release/pom.xml
mvn clean install -f ./restapigenerator_release/pom.xml
mvn clean install -Dmaven.test.skip=true -f ./monitoringandalertingclient_release/pom.xml
mvn clean install -Dmaven.test.skip=true -f ./programmanagement_release/pom.xml
mvn clean install -Dmaven.test.skip=true -f ./itemrenderer_release/shared-blackbox/pom.xml
mvn clean install -Dmaven.test.skip=true -f ./itemrenderer_release/TDS.ItemRenderer/pom.xml
mvn clean install -Dmaven.test.skip=true -f ./student_release/student.library/pom.xml
mvn clean install -Dmaven.test.skip=true -f ./student_release/student/pom.xml
mvn clean install -Dmaven.test.skip=true -f ./student_release/iris/pom.xml
```

##Deploy War file
1. Build War File
  1. Update settings-mysql.xml in IRiS update content path to point to items
  2. Items content path should contain the zipped bank 
2. Get tomcat, choco install tomcat
3. Give permission to tomcat to read/write tomcat installation area
4. Update context, C:\Program Files\Apache Software Foundation\tomcat\apache-tomcat-8.0.32\conf

  ```xml
  <Parameter name="tds.iris.EncryptionKey" override="false" value="24 characters alphanumeric Encryption key" />
  ```
5. Put the war into the webapps directory
6. Verify the deployment
  1. Access management console for tomcat, localhost:8080
  2. Login and verify IRiS is running
  3. See log in tomcat logs

##How to use
* http://localhost:8080/iris/IrisPages/sample.xhtml

```
{"items":[{"response":"","id":"I-187-2576"}]}
```

* More to come...


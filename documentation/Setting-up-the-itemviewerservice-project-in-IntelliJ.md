## Step 1: Install IntelliJ
If you already have IntelliJ installed you can skip this step.
We are currently using the community version of IntelliJ.  
You can download IntelliJ [here](https://www.jetbrains.com/idea/download/#section=windows).  
Run the installer and choose the defaults.

## Step 2: Clone the project
Clone the project from Github into a local directory of your choice.  
If you need to create a new branch you can do so with the commands  
`git checkout -b branchName`  
`git push origin branchName`  
If you need to change which branch you are on use the command `git checkout -b branchName`  

## Step 3: Set up the project in IntelliJ
In IntelliJ choose open and select the folder you cloned the git repo into.  
#### 1. Download the project dependencies  
Find pom.xml in the source directory. Right click on pom.xml and choose Add as Maven Project.  
Right click on pom.xml and choose Maven ➡ Download Sources.  
#### 2. Select the JDK  
Go to File ➡ Project Structure. Click on the SDKs tab. Set the JDK home path to C:\Program Files\Java\jdk1.8.0_11
#### 3. Set the run configuration  
1. Go to Run ➡ Edit Configurations...  
2. Click the green plus in the top left of the screen. Choose the Maven option.  
3. Choose a name such as "Run IntemViewerService".  
4. Under the Parameters tab set the command line to `spring-boot:run` 
5. Choose OK  

### Step 4: Run the project  
Go to Run ➡ Run ItemViewerService. You should see output in the console as Maven builds the project an Tomcat starts. Once the console has stopped spewing startup info open a web browser and navigate to [http://localhost:8080/](http://localhost:8080/)  

Alternatively you can run the project from the command line with `spring-boot:run`
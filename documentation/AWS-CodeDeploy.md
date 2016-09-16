Amazon Code Deploy is used to redeploy the Item Viewer service when there is
a change in the files stored in the S3 bucket containing the content package.
Amazon Lambda is used to trigger the code deployment when there is an update to the content package stored
in an Amazon S3 bucket.

## EC2 Instance Setup
Create an EC2 instance following the deployment instructions in the Automated-AWS-Deployment documentation.
When you are creating the IAM role for the instance add the following:

- Click on "Create Role Policy" and add the following role policy.

```json
{
    "Statement": [
        {
            "Action": [
                "autoscaling:Describe*",
                "cloudformation:Describe*",
                "cloudformation:GetTemplate",
                "s3:Get*"
            ],
            "Resource": "*",
            "Effect": "Allow"
        }
    ]
}
```
- Attach the AWSCodeDeployFullAccess policy.

Install the Code Deploy agent on the instance following [Amazon's instructions](https://docs.aws.amazon.com/codedeploy/latest/userguide/how-to-run-agent-install.html).

## Code Deploy Setup
Create a zip or tar archive containing the `deploy.sh` script located in the root directory of the Item Viewer's
git repository, and an appspec.yml file that runs the script.  

Appspec:
 ```yml
version: 0.0
os: linux
hooks:
  ApplicationStart:
    - location: deploy.sh
      timeout: 3000
 ```

Upload the archive to an Amazon S3 bucket.

### Creating a Service Role
Code deploy requres a service role to interact with EC2 instances.
- Under IAM roles choose "Create New Role"
- Give the role a descriptive name such as "CodeDeployServiceRole", then choose next
- Under Select role Type choose "AWS Service Roles", then "AWS CodeDeploy", then choose next
- Select the "AWSCodeDeployRole" option, then choose next
- Choose "Create Role"

### Creating an Application and Deployment Group

Under the "Code Deploy" section of the Amazon Web Console choose "Create New Application"  

In the Create New Application Section:
- Give your application a descriptive name
- Set a descriptive deployment group name

In the Add Instances section find your EC2 instance:
- Select "Name" in the Key dropdown
- Select your EC2 instance's name in the Value dropdown

Deployment Configuration:
- Set Deployment Config to "CodeDeployDefault.OneAtATime"

Add any alarms you want. These are not required for the deployment to function.
Service Role
- Select the Role you created in the "Creating a Service Role" step.

Choose Create Application to finish creating the application.

## AWS Lambda Setup
An AWS Lambda function with an Amazon S3 ObjectCreated trigger is used to launch an AWS code deployment
when the objects in the S3 bucket containing the content package change.

AWS Lambda will need an IAM role in order to trigger the code deployment and interact with S3.
- Under IAM roles choose "Create Role"
- Set a descriptive name such as "LambdaExecution"
- Select AWS Service Roles, then AWS Lambda, then next
- Add the AmazonS3ReadOnly, and AWSCodeDeployFullAccess policies, then next
- Create Role

To set up the Lambda function go to the Lambda section of the AWS web console and choose "Create a Lambda function".
- Choose "Skip" in the templates section
- Under Configure Triggers add S3
- Choose the S3 bucket used to serve the content package
- Set the "Event type" to "Object Created(All)"
- Set the "Suffix" to zip
- Check the "Enable trigger box"
- Choose "Next"
- Give the function a descriptive name
- Set the runtime to "Node.js 4.3"
- Enter the following code

```javascript
var aws = require('aws-sdk');
var s3 = new aws.S3({apiVersion: '2006-03-01'});
var codedeploy = new aws.CodeDeploy();
 
exports.handler = function(event, context) {

    console.log("Started.");
        var params = {
            applicationName: 'your-application-name',
            deploymentGroupName: 'yourdeploygroup',
            description: 'Lambda invoked codedeploy deployment',
            ignoreApplicationStopFailures: false,
            revision: {
                revisionType: 'S3',
                s3Location: {
                    bucket: 'your-content-deployment-bucket',
                    bundleType: 'tar',
                    key: 'codedeploy.tar'
                }
            }
        };
        console.log("Before create Deployment");
        codedeploy.createDeployment(params, function(err, data) {
                if (err) {
                    context.done(err);
                }
                else {
                    context.done();
                }
        });
        console.log("Finished");
};
```
In the above code:
- Replace `your-application-name` with the name of the application you created in the "Creating an Application and Deployment Group" step
- Replace `yourdeploygroup` with the name of the deployment group you created in the "Creating an Application and Deployment Group" step
- Replace `your-content-deployment-bucket` with the name of the content bucket holding the archive you created in the "Code Deploy Setup" step.
- Replace `codedeploy.tar` with the name of the archive file you created in the "Code Deploy Setup" step.
- Under role choose the Existing role, and the Lambda Execution role you set up above.
- Choose next
- Choose create

Clicking the "Test" button will run the lambda function and allow you to verify it is working correctly.
You should be able to go to the CodeDeploy Application you set up earlier and see the new deployment running.
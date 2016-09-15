Amazon Code Deploy is used to redeploy the Item Viewer service when there is
a change in the files stored in the S3 bucket containing the content package.
The deployment should use the AWS deploy scripts specified in the Automated AWS Deployment document
to deploy the application.

## The EC2 Instance Setup
The EC2 instances running the Item Viewer need permissions to Amazon S3, and CodeDeploy. 
The [Amazon Code Deploy agent](https://docs.aws.amazon.com/codedeploy/latest/userguide/how-to-run-agent-install.html) 
must be running or the deployment will fail.

## Code Deploy Setup
Set up an Amazon Code Deploy application and deployment group for the Item Viewer.
Add the EC2 instances running the Item Viewer to the deployment group.

## AWS Lambda Setup
An AWS Lambda function with an Amazon S3 ObjectCreated trigger is used to launch an AWS code deployment
when the contents of the S3 bucket changes. The lambda function will need access to CodeDeploy and S3.

```javascript
var aws = require('aws-sdk');
var s3 = new aws.S3({apiVersion: '2006-03-01'});
var codedeploy = new aws.CodeDeploy();
 
exports.handler = function(event, context) {

    console.log("Started.");
        var params = {
            applicationName: 'item-viewer-service-content-update-code-deploy',
            deploymentGroupName: 'testdeploygroup',
            description: 'Lambda invoked codedeploy deployment',
            ignoreApplicationStopFailures: false,
            revision: {
                revisionType: 'S3',
                s3Location: {
                    bucket: 'cass-sb-content-deployment',
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
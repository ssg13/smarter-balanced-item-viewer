## Overview
This project is hosted on Amazon Web Services EC2 using Ubuntu 14.04. 

Deployment is automated via a deploy script (```deploy.sh```).

### Prerequisites
The automated deployment of this application requires several prerequisites to run.
-  Two S3 buckets must be configured:
    - One must host the configuration files (```config.zip```)
    - Another must host the ```.war``` file (```itemviewerservice.war```)
    - **The URLs to these S3 buckets must be configured in the deployment script.**
- An IAM role must be added to allow access to the S3 buckets. The following script is the necessary IAM role policy:
```
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:Get*",
        "s3:List*"
      ],
      "Resource": "*"
    }
  ]
}
```
- A security group must be configured to allow access to certain ports.
    - Create a security group with the following configurations:

#### Inbound

| Type | Protocol | Port Range | Source |
| --- | --- | --- | --- |
|HTTP | TCP | 80 | 0.0.0.0/0 |
| SSH | TCP | 22 | 0.0.0.0/0 | 


#### Outbound
| Type | Protocol | Port Range | Source |
| --- | --- | --- | --- |    
| All Traffic | All | All | 0.0.0.0/0 | 

### Deployment instructions
To deploy the application, launch an Amazon Web Services instance:

1. Use AMI: Ubuntu Server 14.04 LTS (HVM), SSD Volume Type (ami-d732f0b7).
2. Select a suitable instance type.
3. In ```Configure Instance Details```, add the IAM role allowing access to the S3 servers. If one has not been created yet, see the __Prerequisites__ section.
4. Still in ```Configure Instance Details```, expand the ```Advanced Details``` section. 
5. Select ```As file``` for ```User data``` and upload the deploy script (```deploy.sh```).
6. Select ```Review and Launch```.
7. Next to ```Security Groups```, select ```Edit Security Groups``` and add the security group created in the __Prerequisites__ section.
8. Launch your instance.

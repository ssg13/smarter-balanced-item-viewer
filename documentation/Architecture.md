# Smarter Balanced Item Viewer Service – Architecture Proposal

## Abstract 
Use Amazon EC2 instances for running webservers and Redis nodes. Since Redis doesn’t use the hard drive, storage-less EC2 instances could be used to save money.

## Background
CASS has been contracted to build a replacement item viewer service for Smarter Balanced. The service renders a Smarter Balanced item into HTML. An item is a folder of files containing XML and possibly media files that represent a question on an assessment test. The renderer is already built and is a Java .jar accessible via a command-line interface. 
CASS’s job is to build an API based on the item renderer that builds and serves rendered items efficiently.

## Objectives
### Speed + Scalability
The Item Viewer service is expected to be used by over 5,000+ students at a time. Since it will be running on a cloud provider, likely AWS, the service should be able to automatically scale up and down based on demand. The service should not be dependent on any one instance and the application should be built with the assumption that instances can go offline at any time.
#### Maintainability
The application should support changes to the Java HTML renderer without updates to the API code. Deploying changes to the renderer should be as simple as updating the application version and performing a rolling update. If Smarter Balance wishes to update the API, the language and code structure should be easily understood by the average developer. Overuse of frameworks should be avoided and only mature frameworks we are confident will be supported in the future should be used.
#### FOSS
Smarter Balanced has requested that the application run on CoreOS and that free and open source software be used as much as possible.
#### Simplicity
The architecture should be simple and use as few components as possible. The code backing each API instance should be the same.

## Architecture Proposal
![hardware_arch](https://cloud.githubusercontent.com/assets/6743966/13267159/b11832bc-da30-11e5-9a3a-9ddd4c286ac0.png)
### Infrastructure
I propose a simple architecture that takes advantage of products that are standard across cloud-computing providers (e.g., [AWS](https://aws.amazon.com), [Google Cloud](https://cloud.google.com)) and not proprietary to one provider. The architecture has three components: API instances, object storage, and a load balancer.

Since items will be stored in memory and only rendered when a previously-unrendered item is requested, high-memory, low-CPU instances should such as [Amazon’s R3](https://aws.amazon.com/ec2/instance-types/) or [Google’s n1-highmem](https://cloud.google.com/compute/pricing#disk) should be used. Persistent or local disks aren’t necessary.

![instance_arch](https://cloud.githubusercontent.com/assets/6743966/13267160/b131504e-da30-11e5-8e55-6bb19ffb09f4.png)

#### API Instances
Amazon EC2 or Google Compute Engine. API instances will be VMs running CentOS that contain the functionality of the application. Each VM image will contain three applications: 
1. In-memory datastore: preferably Redis, will cache rendered HTML to increase speed and reduce CPU load. Since we just need an in-memory key-value store, we could also consider making this part of the API server.
2. Java item renderer: This is provided by Smarter Balanced as a .JAR file. Its purpose is to render Smarter Balanced items into HTML that are served by the API server.
3. API server: handles requests and oversees rendering items. When an item is requested, the server will first check the memory datastore to see if the item was previously rendered. If not, the Java renderer will be called and the output HTML will be served and stored in memory for quick future access. The API server will contain the most custom code, and is where most of our development will be focused.
#### Object Storage
Amazon S3 or Google Cloud Storage. This is where item folders will be stored. Both contain a simple web interface for uploading and viewing objects. This will be the source of truth for the API instances, though accessing object storage will be unnecessary for most requests as most items will be pre-rendered and stored in each API instance’s memory. Adding new assessment items will be as simple as uploading a new item via AWS or Google Cloud’s web interface.
#### Load balancer
The load balancer will provide a static IP that directs requests to API instances. Most cloud services provide this as standard.

### Language / Framework Suggestions
My three language / frameworks suggestions are C# + ASP.NET 5, Node.js using TypeScript + Express, and Java + Play framework. All are well-suited for developing APIs, have decent developer tools, are relatively readable, and are widely used in the industry. In addition the all have mature Redis cluster drivers, which is important if we choose to use Redis for caching.
#### C# & ASP.NET 5
##### Pros
- CASS is most familiar with this
- Readable
- Typed
- Good developer tools (Visual Studio)
- Very popular

##### Cons
- ASP.NET below v5 only runs on Windows
- Just moved out of beta, not yet version 1.0
- Most questionable cross-platform support

#### Typescript (Node.js) & Express
##### Pros
- Probably most performant
- Most readable / expressive
- Typed (with TypeScript)
- Good developer tools (Visual Studio)
- Largest package library
- Increasing popularity
- Just-in-time compilation means zero build time

##### Cons
- Javascript is divisive
- Least mature
- Single thread can lock under excessive load
- More “magic” going on
- Had a recent security flaw (fixed)
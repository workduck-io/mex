# Mex

### Getting Started

- Clone the repository on your local machine
```
git clone mex repo : https://github.com/workduck-io/mex
```


- Install [IntelliJ IDEA CE](https://www.jetbrains.com/idea/download/#section=mac)


- Run IntelliJ and open mex as **maven project**


- On the left viewer tab, select Project Files from dropdown


- Run `mvn install` in the terminal to check if your local machine has all the required dependencies . Alternatively, click on  Maven button on the right edge of the editor and follow these steps : `mex>Lifecycle>install`


- Install the following plugins which are required for local setup :
**serverless-offline** : `npm install serverless-offline --save-dev` <br>
**serverless-dynamodb-local** : `npm install --save serverless-dynamodb-local`

**NOTE:** Please use Node.js Version *12.13.0, 10.16.3, and 8.10* to install npm packages

- The `serverless.yml` file has `userDocker` flag set as `true` for serverless-offline, so you'd need to install and run **docker** on your local as well.


- If you want to connect to a local dynamodb instance rather than an actual table,
navigate to `DDBHelper.kt` in `com.workduck.utils`  and comment out the current `createDDBConenction()` function and uncomment the commented one. This helps you to connect to local endpoint rather than connecting you to aws. ( Going forward this should be handled automatically )


- Install dynamodb-admin for GUI
```
npm install -g dynamodb-admin
```
To spin up dynamodb-admin :
```
docker pull amazon/dynamodb-local
docker run -p 8000:8000 amazon/dynamodb-local
dynamodb-admin
```
**Note** : ( DDB listens at 8000, GUI can be accessed at 8001 )


- Access the GUI at : `localhost:8001` & create a new table named `local-mex`
```
Hash Attribute : PK, Type : String
Range Attribute : SK, Type : String
```

- Go to `serverless.yml` and uncomment AWS credentials ( Make sure to uncomment this when pushing any changes )

- Add a `.env` file and add your aws credentials to it ( since this file is present in .gitignore, it won't be pushed to repository )

```
AWS_ACCESS_KEY_ID=***************
AWS_SECRET_ACCESS_KEY=***************

```

- run `sls offline` in terminal . APIs should be available on port **4000**. 

Cheers!🍻

### Known errors :

**domainName Error**

```
TypeError: Cannot read properties of undefined (reading 'domainName')
at ServerlessCustomDomain.addOutputs (/Users/varungarg/WorkDuck/mex/node_modules/serverless-domain-manager/dist/src/index.js:559:38)
at ServerlessCustomDomain.<anonymous> (/Users/varungarg/WorkDuck/mex/node_modules/serverless-domain-manager/dist/src/index.js:246:22)
at Generator.next (<anonymous>)
at fulfilled (/Users/varungarg/WorkDuck/mex/node_modules/serverless-domain-manager/dist/src/index.js:5:58)
at processTicksAndRejections (node:internal/process/task_queues:96:5)

     For debugging logs, run again after setting the "SLS_DEBUG=*" environment variable.
```

_Fix_

`serverless create_domain --stage ${stageYouWantToDeploy}`
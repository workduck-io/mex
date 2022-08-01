'use strict';

/** Generated by Serverless WarmUp Plugin **/

const AWS = require('aws-sdk');
const lambda = new AWS.Lambda({
  apiVersion: '2015-03-31',
  region: 'us-east-1',
  httpOptions: {
    connectTimeout: 1000, // 1 second
  },
});
const functions = [
  {
    "name": "mex-backend-staging-Ping",
    "config": {
      "enabled": true,
      "payload": "{\"source\":\"serverless-plugin-warmup\"}",
      "concurrency": 5
    }
  },
  {
    "name": "mex-backend-staging-PublicNoteUpdate",
    "config": {
      "enabled": true,
      "payload": "{\"source\":\"serverless-plugin-warmup\"}",
      "concurrency": 5
    }
  },
  {
    "name": "mex-backend-staging-Node",
    "config": {
      "enabled": true,
      "payload": "{\"source\":\"serverless-plugin-warmup\"}",
      "concurrency": 5
    }
  },
  {
    "name": "mex-backend-staging-Snippet",
    "config": {
      "enabled": true,
      "payload": "{\"source\":\"serverless-plugin-warmup\"}",
      "concurrency": 5
    }
  },
  {
    "name": "mex-backend-staging-Namespace",
    "config": {
      "enabled": true,
      "payload": "{\"source\":\"serverless-plugin-warmup\"}",
      "concurrency": 5
    }
  },
  {
    "name": "mex-backend-staging-Workspace",
    "config": {
      "enabled": true,
      "payload": "{\"source\":\"serverless-plugin-warmup\"}",
      "concurrency": 5
    }
  },
  {
    "name": "mex-backend-staging-User",
    "config": {
      "enabled": true,
      "payload": "{\"source\":\"serverless-plugin-warmup\"}",
      "concurrency": 5
    }
  },
  {
    "name": "mex-backend-staging-UserPreference",
    "config": {
      "enabled": true,
      "payload": "{\"source\":\"serverless-plugin-warmup\"}",
      "concurrency": 5
    }
  },
  {
    "name": "mex-backend-staging-UserBookmark",
    "config": {
      "enabled": true,
      "payload": "{\"source\":\"serverless-plugin-warmup\"}",
      "concurrency": 5
    }
  },
  {
    "name": "mex-backend-staging-Comment",
    "config": {
      "enabled": true,
      "payload": "{\"source\":\"serverless-plugin-warmup\"}",
      "concurrency": 5
    }
  },
  {
    "name": "mex-backend-staging-WorkspaceUpdateTrigger",
    "config": {
      "enabled": true,
      "payload": "{\"source\":\"serverless-plugin-warmup\"}",
      "concurrency": 5
    }
  },
  {
    "name": "mex-backend-staging-NodeArchiveUnarchiveTrigger",
    "config": {
      "enabled": true,
      "payload": "{\"source\":\"serverless-plugin-warmup\"}",
      "concurrency": 5
    }
  },
  {
    "name": "mex-backend-staging-Tag",
    "config": {
      "enabled": true,
      "payload": "{\"source\":\"serverless-plugin-warmup\"}",
      "concurrency": 5
    }
  },
  {
    "name": "mex-backend-staging-InternalTag",
    "config": {
      "enabled": true,
      "payload": "{\"source\":\"serverless-plugin-warmup\"}",
      "concurrency": 5
    }
  }
];

function getConcurrency(func, envVars) {
  const functionConcurrency = envVars[`WARMUP_CONCURRENCY_${func.name.toUpperCase().replace(/-/g, '_')}`];

  if (functionConcurrency) {
    const concurrency = parseInt(functionConcurrency);
    console.log(`Warming up function: ${func.name} with concurrency: ${concurrency} (from function-specific environment variable)`);
    return concurrency;
  }

  if (envVars.WARMUP_CONCURRENCY) {
    const concurrency = parseInt(envVars.WARMUP_CONCURRENCY);
    console.log(`Warming up function: ${func.name} with concurrency: ${concurrency} (from global environment variable)`);
    return concurrency;
  }

  const concurrency = parseInt(func.config.concurrency);
  console.log(`Warming up function: ${func.name} with concurrency: ${concurrency}`);
  return concurrency;
}

module.exports.warmUp = async (event, context) => {
  console.log('Warm Up Start');

  const invokes = await Promise.all(functions.map(async (func) => {
    const concurrency = getConcurrency(func, process.env);

    const clientContext = func.config.clientContext !== undefined
      ? func.config.clientContext
      : func.config.payload;

    const params = {
      ClientContext: clientContext
        ? Buffer.from(`{"custom":${clientContext}}`).toString('base64')
        : undefined,
      FunctionName: func.name,
      InvocationType: 'RequestResponse',
      LogType: 'None',
      Qualifier: func.config.alias || process.env.SERVERLESS_ALIAS,
      Payload: func.config.payload
    };

    try {
      await Promise.all(Array(concurrency).fill(0).map(async () => await lambda.invoke(params).promise()));
      console.log(`Warm Up Invoke Success: ${func.name}`);
      return true;
    } catch (e) {
      console.log(`Warm Up Invoke Error: ${func.name}`, e);
      return false;
    }
  }));

  console.log(`Warm Up Finished with ${invokes.filter(r => !r).length} invoke errors`);
}
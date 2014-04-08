couchbase-stale-connection-test
===============================

The control connection gets recycled only if 

```
connectionFactoryBuilder.setFailureMode(CouchbaseConnectionFactory.DEFAULT_FAILURE_MODE);
```
or

```
connectionFactoryBuilder.setFailureMode(FailureMode.Redistribute);
```

couchbase-stale-connection-test
===============================

The control connection gets recycled only if 

```
connectionFactoryBuilder.setFailureMode(FailureMode.Redistribute);
```

How to run?
___________

```
mvn clean test
```

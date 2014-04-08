couchbase-stale-connection-test
===============================

The control connection gets recycled only if 

```
connectionFactoryBuilder.setFailureMode(FailureMode.Redistribute);
```


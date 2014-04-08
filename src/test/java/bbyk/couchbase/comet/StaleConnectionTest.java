package bbyk.couchbase.comet;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.CouchbaseConnectionFactory;
import com.couchbase.client.CouchbaseConnectionFactoryBuilder;
import com.couchbase.client.vbucket.BucketMonitor;
import net.spy.memcached.FailureMode;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Arrays;
import java.util.Random;

/**
 * Unit test for simple App.
 */
public class StaleConnectionTest {
    private final static Logger logger = LoggerFactory.getLogger(StaleConnectionTest.class);

    /**
     * The test helps reproducing a stale control connection issue.
     * <p/>
     * Steps to repro:
     * <ol>
     * <li>Bring up a cluster of couchbase nodes on virtual machines using https://github.com/daschl/vagrants</li>
     * <li>Run the test against the cluster</li>
     * <li>Put a break-point on the first line in {@link BucketMonitor#notifyDisconnected}</li>
     * <li>run "vagrant suspend node1". This just takes the machine out without gracefully closing tcp connections.</li>
     * <li>The break point will never be hit.</li>
     * </ol>
     */
    @Test
    public void connectAndRunGetSet() throws Exception {
        final CouchbaseConnectionFactoryBuilder connectionFactoryBuilder = new CouchbaseConnectionFactoryBuilder();
        // this line probably causes it to not redistribute because
        // of com.couchbase.client.CouchbaseConnection.addOperation() lines 220 - 223
        connectionFactoryBuilder.setFailureMode(FailureMode.Cancel);
        
        // the line below is what restarts the connection.
        // connectionFactoryBuilder.setFailureMode(CouchbaseConnectionFactory.DEFAULT_FAILURE_MODE);
        
        final CouchbaseConnectionFactory connectionFactory = connectionFactoryBuilder.buildCouchbaseConnection(
                Arrays.asList(new URI("http://192.168.56.101:8091/pools")),
                "default",
                "" /* user name */,
                "" /* passwd */);
        final CouchbaseClient client = new CouchbaseClient(connectionFactory);

        final Random random = new Random();

        //noinspection InfiniteLoopStatement
        while (true) {
            for (char c = 'a'; c < 'z'; c++) {
                final String keyToFetch = Character.toString(c);

                try {
                    Integer value = (Integer) client.get(keyToFetch);
                    value = (value == null ? 0 : value) + random.nextInt();
                    client.set(keyToFetch, 30 * 60, value);
                } catch (Exception e) {
                    logger.error("an error while processing a transaction", e);
                }
            }

            Thread.sleep(200);

            // stop the test when you're done debugging
        }
    }
}

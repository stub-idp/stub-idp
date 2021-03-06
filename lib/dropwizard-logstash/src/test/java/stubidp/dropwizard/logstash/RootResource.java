package stubidp.dropwizard.logstash;

import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class RootResource {
    private static final Logger LOG = Logger.getLogger(RootResource.class);
    public static final String TEST_LOG_LINE = "test log line";
    public static final String HELLO_MESSAGE = "hello!";

    @GET
    public String get() {
        return HELLO_MESSAGE;
    }

    @GET
    @Path("log")
    public void log() {
        LOG.info(TEST_LOG_LINE);
    }
}

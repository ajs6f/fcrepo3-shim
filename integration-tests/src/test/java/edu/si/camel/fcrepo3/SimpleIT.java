package edu.si.camel.fcrepo3;

import static org.slf4j.LoggerFactory.getLogger;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.jena.riot.web.HttpOp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.slf4j.Logger;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class SimpleIT extends ShimIT {

    Logger log = getLogger(SimpleIT.class);

    @Test
    public void testProvisioning() throws Exception {
        assertFeatureInstalled("camel-core", CAMEL_VERSION);
    }
    
    @Test
    public void test() {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        Credentials credentials = new UsernamePasswordCredentials("karaf", "karaf");
        credsProvider.setCredentials(AuthScope.ANY, credentials);
        HttpClient httpclient = HttpClientBuilder.create().setDefaultCredentialsProvider(credsProvider).build();
        HttpOp.setDefaultHttpClient(httpclient);
        String models = HttpOp.execHttpGetString("http://localhost:9091/shim/fedora-system:FedoraObject-3.0/models");
        log.info("Found models: \n{}", models);
        String fourofour = HttpOp.execHttpGetString("http://localhost:9091/dfkhgdsfgdfsh");
        log.info("Found 404: \n{}", fourofour);
    }
}

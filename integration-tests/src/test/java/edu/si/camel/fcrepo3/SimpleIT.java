package edu.si.camel.fcrepo3;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.slf4j.LoggerFactory.getLogger;

import org.apache.camel.test.karaf.CamelKarafTestSupport;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.riot.web.HttpOp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.slf4j.Logger;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class SimpleIT extends CamelKarafTestSupport {

    Logger log = getLogger(SimpleIT.class);

    private static final String TEST_FEDORA = System.getProperty("dynamic.test.port", "8080") + "/fedora";
    private static final String BUILD_DIRECTORY = System.getProperty("buildDirectory");

    private static final String CAMEL_VERSION = "2.18.2";

    @Test
    public void testProvisioning() throws Exception {
        assertFeatureInstalled("camel-core", CAMEL_VERSION);
    }

    @Configuration
    public Option[] config() {
        MavenUrlReference camelRepo = maven().groupId("org.apache.camel.karaf").artifactId("apache-camel")
                        .classifier("features").type("xml").version(CAMEL_VERSION);
        MavenUrlReference jenaRepo = maven().groupId("org.apache.jena").artifactId("jena-osgi-features")
                        .classifier("features").type("xml").version("3.2.0");
        MavenUrlReference enterpriseRepo = maven().groupId("org.apache.karaf.features").artifactId("enterprise")
                        .classifier("features").type("xml").version("4.0.7");

        MavenArtifactProvisionOption shim = mavenBundle().groupId("edu.si").artifactId("fcrepo3-shim-core")
                        .version("0.0.1-SNAPSHOT").start();
        
        Option[] options = new Option[] { features(enterpriseRepo, "jndi"), features(jenaRepo, "jena"),
                features(camelRepo, "camel-test"), shim };
        return ArrayUtils.addAll(configure(), options);
    }
}

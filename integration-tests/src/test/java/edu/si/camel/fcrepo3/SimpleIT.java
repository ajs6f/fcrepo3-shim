package edu.si.camel.fcrepo3;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;
import static org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel.DEBUG;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;

import org.apache.camel.test.junit4.CamelTestSupport;
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
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.slf4j.Logger;

@RunWith(PaxExam.class)
public class SimpleIT extends CamelTestSupport {

    Logger log = getLogger(SimpleIT.class);

    private static final String TEST_FEDORA = System.getProperty("dynamic.test.port", "8080") + "/fedora";
    private static final String BUILD_DIRECTORY = System.getProperty("buildDirectory");

    @Test
    public void test() {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        Credentials credentials = new UsernamePasswordCredentials("karaf", "karaf");
        credsProvider.setCredentials(AuthScope.ANY, credentials);
        HttpClient httpclient = HttpClients.custom()
            .setDefaultCredentialsProvider(credsProvider)
            .build();
        HttpOp.setDefaultHttpClient(httpclient);
        String models = HttpOp.execHttpGetString("http://localhost:8181/shim/fedora-system:FedoraObject-3.0/models");
        log.info("Found models: \n{}", models);
        String fourofour = HttpOp.execHttpGetString("http://localhost:8181/dfkhgdsfgdfsh");
        log.info("Found 404: \n{}", fourofour);
    }

    @Configuration
    public Option[] config() {
        MavenArtifactUrlReference karafUrl = maven().groupId("org.apache.karaf").artifactId("apache-karaf")
                        .version("4.1.0").type("tar.gz");
        MavenUrlReference karafStandardRepo = maven().groupId("org.apache.karaf.features").artifactId("standard")
                        .classifier("features").type("xml").version("4.1.0");
        MavenUrlReference camelRepo = maven().groupId("org.apache.camel.karaf").artifactId("apache-camel")
                        .classifier("features").type("xml").version("2.18.2");
        MavenUrlReference jenaRepo = maven().groupId("org.apache.jena").artifactId("jena-osgi-features")
                        .classifier("features").type("xml").version("3.2.0");
        return new Option[] { logLevel(DEBUG),
                
                karafDistributionConfiguration().frameworkUrl(karafUrl)
                                .unpackDirectory(new File(BUILD_DIRECTORY + "/exam")),
                keepRuntimeFolder(),
                configureConsole().ignoreRemoteShell().ignoreLocalConsole(), features(karafStandardRepo, "standard", "webconsole"), features(jenaRepo, "jena"),
                features(camelRepo, "camel-core", "camel-test-karaf"),
                mavenBundle().groupId("org.fcrepo.camel").artifactId("fcrepo3-shim-core").versionAsInProject().start() ,
                mavenBundle().groupId("uk.org.lidalia").artifactId("sysout-over-slf4j").versionAsInProject().start()
    //    editConfigurationFilePut(configurationPointer, value)        
        };
    }
}

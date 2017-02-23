package edu.si.camel.fcrepo3;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.vmOption;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;

import org.apache.camel.CamelContext;
import org.apache.camel.core.osgi.OsgiDefaultCamelContext;
import org.apache.camel.test.karaf.CamelKarafTestSupport;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
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

    private static String KARAF_VERSION = "4.0.7";

    @Test
    public void testProvisioning() throws Exception {
        assertFeatureInstalled("camel-core", CAMEL_VERSION);
    }

    @Override
    protected CamelContext createCamelContext() {
        return new OsgiDefaultCamelContext(bundleContext);
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
        MavenArtifactProvisionOption camelOsgi = mavenBundle().groupId("org.apache.camel").artifactId("camel-core-osgi")
                        .version(CAMEL_VERSION).start();

        Option[] options = new Option[] {
                // for remote debugging
                // org.ops4j.pax.exam.CoreOptions.vmOption("-Xdebug"),
                // org.ops4j.pax.exam.CoreOptions.vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5008"),

                KarafDistributionOption.karafDistributionConfiguration()
                                .frameworkUrl(maven().groupId("org.apache.karaf").artifactId("apache-karaf")
                                                .type("tar.gz").versionAsInProject())
                                .karafVersion(KARAF_VERSION).name("Apache Karaf").useDeployFolder(false)
                                .unpackDirectory(new File("target/paxexam/unpack/")),
                logLevel(LogLevelOption.LogLevel.INFO),

                // keep the folder so we can look inside when something fails
                keepRuntimeFolder(),

                // Disable the SSH port
                configureConsole().ignoreRemoteShell(),

                // need to modify the jre.properties to export some com.sun packages that some features rely on
                // KarafDistributionOption.replaceConfigurationFile("etc/jre.properties", new
                // File("src/test/resources/jre.properties")),

                vmOption("-Dfile.encoding=UTF-8"),

                // Disable the Karaf shutdown port
                editConfigurationFilePut("etc/custom.properties", "karaf.shutdown.port", "-1"),

                // Assign unique ports for Karaf
                // editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.port",
                // Integer.toString(AvailablePortFinder.getNextAvailable())),
                // editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiRegistryPort",
                // Integer.toString(AvailablePortFinder.getNextAvailable())),
                // editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiServerPort",
                // Integer.toString(AvailablePortFinder.getNextAvailable())),

                // install junit
                CoreOptions.junitBundles(),

                // install camel
                features(camelRepo, "camel"),

                // install camel-test-karaf as bundle (not feature as the feature causes a bundle refresh that
                // invalidates the @Inject bundleContext)
                mavenBundle().groupId("org.apache.camel").artifactId("camel-test-karaf").version(CAMEL_VERSION),
                features(enterpriseRepo, "jndi"), camelOsgi, features(jenaRepo, "jena"),
                features(camelRepo, "camel-test"), shim };

        return options;
    }
}

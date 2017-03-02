package edu.si.camel.fcrepo3;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.vmOption;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;
import javax.inject.Inject ;
import java.io.File;

import org.apache.camel.CamelContext;
import org.apache.camel.core.osgi.OsgiDefaultCamelContext;
import org.apache.camel.test.karaf.CamelKarafTestSupport;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.karaf.options.KarafFeaturesOption;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.osgi.framework.BundleContext;

public abstract class ShimIT extends CamelKarafTestSupport {
    
    @Inject
    protected BundleContext bundleContext;

    protected static final String TEST_FEDORA = System.getProperty("dynamic.test.port", "8080") + "/fedora";
    protected static final String BUILD_DIRECTORY = System.getProperty("buildDirectory");
    protected static final String CAMEL_VERSION = "2.18.2";
    private static final String KARAF_VERSION = "4.1.0";
    private static final String TOMCAT_URI = "http://localhost:" + System.getProperty("dynamic.test.port", "8080");
    protected static final String FEDORA_URI = TOMCAT_URI + "/trippi-sparql-fcrepo-webapp";
    protected static final String FUSEKI_URI = TOMCAT_URI + "/jena-fuseki-war/fedora3";

    @Override
    protected CamelContext createCamelContext() {
        return new OsgiDefaultCamelContext(bundleContext);
    }

    @Configuration
    public Option[] config() {
        MavenUrlReference camelRepo = maven().groupId("org.apache.camel.karaf").artifactId("apache-camel")
                        .classifier("features").type("xml").version(CAMEL_VERSION);

        MavenUrlReference fcrepoShim = maven().groupId("edu.si").artifactId("fcrepo3-shim-core").classifier("features")
                        .type("xml").version("0.0.1-SNAPSHOT");

        KarafFeaturesOption camelTest = features(camelRepo, "camel-test");
        KarafFeaturesOption shim = features(fcrepoShim, "fcrepo3-shim-core");

        // install camel-test-karaf as bundle (not feature as the feature causes a bundle refresh that
        // invalidates the @Inject bundleContext)
        MavenArtifactProvisionOption camelTestKaraf = mavenBundle().groupId("org.apache.camel")
                        .artifactId("camel-test-karaf").version(CAMEL_VERSION);

        MavenArtifactProvisionOption camelCoreOsgi = mavenBundle().groupId("org.apache.camel")
                        .artifactId("camel-core-osgi").version(CAMEL_VERSION);

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
                configureConsole().ignoreRemoteShell().ignoreLocalConsole(),

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

                CoreOptions.systemProperty("fcrepo3.uri").value(FEDORA_URI),

                // install camel
                camelTest, camelTestKaraf,
                // and the shim itself
                shim, camelCoreOsgi };

        return options;
    }

}
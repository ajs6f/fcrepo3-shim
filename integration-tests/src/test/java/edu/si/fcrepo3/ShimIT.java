package edu.si.fcrepo3;

import static edu.si.fcrepo3.ShimIT.UnsafeIO.unsafeIO;
import static edu.si.fcrepo3.TestResourceOption.testResources;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.System.getProperty;
import static java.nio.file.FileSystems.getDefault;
import static java.nio.file.Files.find;
import static java.util.Arrays.stream;
import static java.util.Collections.list;
import static java.util.stream.Collectors.toMap;
import static org.apache.jena.rdf.model.ModelFactory.createDefaultModel;
import static org.apache.jena.riot.Lang.NTRIPLES;
import static org.apache.jena.riot.RDFDataMgr.loadModel;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.streamBundle;
import static org.ops4j.pax.exam.CoreOptions.vmOption;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.replaceConfigurationFile;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;
import static org.osgi.framework.Constants.BUNDLE_MANIFESTVERSION;
import static org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME;
import static org.osgi.framework.Constants.EXPORT_PACKAGE;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.apache.camel.core.osgi.OsgiDefaultCamelContext;
import org.apache.camel.test.AvailablePortFinder;
import org.apache.camel.test.karaf.CamelKarafTestSupport;
import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.rdf.model.Model;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.karaf.options.KarafFeaturesOption;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.PathUtils;
import org.ops4j.pax.tinybundles.core.TinyBundle;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class ShimIT extends CamelKarafTestSupport {

    private static final Logger LOG = getLogger(ShimIT.class);

    static {
        System.setProperty("org.ops4j.pax.logging.DefaultServiceLog.level", "WARN");
    }

    private static final String TEST_RESOURCE_BUNDLE_NAME = "edu.si.fcrepo3.testResources";

    private static final Path ANSWERS_DIR = getDefault()
                    .getPath(getProperty("buildDirectory", "src/test/resources") + "/answers");

    private static BiPredicate<Path, BasicFileAttributes> isTestFile = (path, attr) -> attr.isRegularFile()
                    && !path.getFileName().toString().startsWith(".");

    private static Option testResourceOption() throws IOException {

        TinyBundle testResourceBundle = bundle().set(BUNDLE_SYMBOLICNAME, TEST_RESOURCE_BUNDLE_NAME)
                        .set(BUNDLE_MANIFESTVERSION, "2");

        LOG.debug("Searching for test resources in {}", ANSWERS_DIR);

        try {
            find(ANSWERS_DIR, MAX_VALUE, isTestFile).forEach(path -> {
                LOG.debug("Adding test resource from: {}", path);
                Path relativePath = ANSWERS_DIR.relativize(path);
                LOG.debug("With path: {}", relativePath);
                testResourceBundle.set(EXPORT_PACKAGE, relativePath.toString()).add(relativePath.toString(),
                                unsafeIO(() -> new FileInputStream(path.toFile())));
            });
        } catch (IOException e) {
            LOG.error("Failed to load test resources!", e);
            throw new RuntimeIOException(e);
        }
        return testResources().add(streamBundle(testResourceBundle.build()).start());
    }

    @Inject
    protected BundleContext bundleContext;

    protected static final String TEST_FEDORA = System.getProperty("dynamic.test.port", "8080") + "/fedora";
    protected static final String BUILD_DIRECTORY = System.getProperty("buildDirectory");
    protected static final String CAMEL_VERSION = "2.18.2";
    private static final String KARAF_VERSION = "4.0.7";
    private static final String TOMCAT_URI = "http://localhost:" + System.getProperty("dynamic.test.port", "8080");
    protected static final String FEDORA_URI = TOMCAT_URI + "/trippi-sparql-fcrepo-webapp";
    protected static final String FUSEKI_URI = TOMCAT_URI + "/jena-fuseki-war/fedora3";
    private static final String SLF4J_VERSION = "1.7.20";
    private static final String LOGBACK_VERSION = "1.1.7";

    @Test
    public void testProvisioning() throws Exception {
        assertFeatureInstalled("camel-core", CAMEL_VERSION);
    }

    /**
     * @param url an URL to a test resource
     * @return a path for the shim application that should return that resource
     */
    private static String trimURLtoPath(URL url) {
        String tmp = url.getPath().replaceAll("_", ":");
        return tmp.substring(0, tmp.length() - 3);
    }

    @Test
    public void testCorrectResponses() throws IOException {

        // find test rubric bundle
        Bundle testResources = stream(bundleContext.getBundles())
                        .filter(b -> b.getSymbolicName().equals(TEST_RESOURCE_BUNDLE_NAME)).findAny()
                        .orElseThrow(() -> new IllegalStateException("Missing test resources!"));

        // build rubric in-memory
        List<URL> testEntries = list(testResources.findEntries("/", "*.nt", true));
        Map<String, Model> answers = testEntries.stream().collect(toMap(ShimIT::trimURLtoPath, url -> {
            Model m = createDefaultModel();
            LOG.info("Loading test resource from: {}", url);
            m.read(unsafeIO(() -> url.openStream()), null, NTRIPLES.getContentType().getContentType());
            return m;
        }));

        // take the test
        answers.forEach((path, answer) -> {
            String url = "http://localhost:8181/shim" + path;
            LOG.info("Testing: {}", url);
            LOG.info("Against answer:\n{}", answer);
            try {
                Model result = loadModel(url, NTRIPLES);
                boolean pass = result.isIsomorphicWith(answer);
                if (!pass) {
                    LOG.error("Got result:\n{}", result);
                    LOG.error("when correct answer was:\n{}", answer);
                }
                assertTrue("Got wrong triples for " + path + "!", pass);
            } catch (HttpException e) {
                LOG.error("Failed to retrieve answer because:\n", e.getResponse());
                throw e;
            }
        });
    }

    @Override
    protected CamelContext createCamelContext() {
        return new OsgiDefaultCamelContext(bundleContext);
    }

    @Configuration
    public Option[] config() throws IOException {

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

                // keep the folder so we can look inside when something fails
                keepRuntimeFolder(),

                testResourceOption(),

                // Disable the SSH port
                configureConsole().ignoreRemoteShell().ignoreLocalConsole(),

                vmOption("-Dfile.encoding=UTF-8"),
                // Disable the Karaf shutdown port
                // editConfigurationFilePut("etc/custom.properties", "karaf.shutdown.port", "-1"),
                // Assign unique ports for Karaf
                /*
                 * editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.port",
                 * Integer.toString(AvailablePortFinder.getNextAvailable(8181))),
                 * editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiRegistryPort",
                 * Integer.toString(AvailablePortFinder.getNextAvailable(1099))),
                 * editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiServerPort",
                 * Integer.toString(AvailablePortFinder.getNextAvailable(44444))),
                 */
                // install junit
                CoreOptions.junitBundles(),

                CoreOptions.systemProperty("fcrepo3.uri").value(FEDORA_URI),
                CoreOptions.systemProperty("build.directory").value(BUILD_DIRECTORY),

                // install camel
                camelTest, camelTestKaraf,
                // proper logging
                CoreOptions.systemProperty("karaf.log.console").value("ALL"),

                replaceConfigurationFile("etc/org.ops4j.pax.logging.cfg",
                                new File(BUILD_DIRECTORY + "/org.ops4j.pax.logging.cfg")),

                /*
                 * replaceConfigurationFile("etc/startup.properties", new File(BUILD_DIRECTORY +
                 * "/startup.properties")), mavenBundle("org.ops4j.pax.logging", "pax-logging-logback", "1.9.1"),
                 * mavenBundle("ch.qos.logback", "logback-classic", "1.1.7"), mavenBundle("ch.qos.logback",
                 * "logback-core", "1.1.7"), mavenBundle("org.slf4j", "slf4j-api", "1.7.20"),
                 */
                // tinybundles for test resources
                mavenBundle("org.ops4j.pax.tinybundles", "tinybundles", "2.1.1"),
                mavenBundle("biz.aQute.bnd", "bndlib", "2.4.0"),
                // and the shim itself
                shim, camelCoreOsgi };

        return options;
    }

    /**
     * IOException => RuntimeIOException
     */
    @FunctionalInterface
    public interface UnsafeIO<T> {

        T call() throws IOException;

        static <U> U unsafeIO(UnsafeIO<U> u) {
            try {
                return u.call();
            } catch (IOException e) {
                throw new RuntimeIOException(e);
            }
        }
    }
}
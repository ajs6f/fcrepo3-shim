package org.fcrepo.camel.fcrepo3;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;
import static org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel.DEBUG;

import java.io.File;

import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;

@RunWith(PaxExam.class)
public class SimpleIT extends CamelTestSupport {

    private static final String TEST_FEDORA = System.getProperty("dynamic.test.port", "8080") + "/fedora";
    private static final String BUILD_DIRECTORY = System.getProperty("buildDirectory");

    @Test
    public void test() {}

    @Configuration
    public Option[] config() {
        MavenArtifactUrlReference karafUrl = maven().groupId("org.apache.karaf").artifactId("apache-karaf")
                        .version("4.1.0").type("tar.gz");
        MavenUrlReference karafStandardRepo = maven().groupId("org.apache.karaf.features").artifactId("standard")
                        .classifier("features").type("xml").version("4.1.0");
        MavenUrlReference camelRepo = maven().groupId("org.apache.camel.karaf").artifactId("apache-camel")
                        .classifier("features").type("xml").version("2.18.2");
        return new Option[] { logLevel(DEBUG),
                karafDistributionConfiguration().frameworkUrl(karafUrl)
                                .unpackDirectory(new File(BUILD_DIRECTORY + "/exam")),
                keepRuntimeFolder(), features(karafStandardRepo, "standard", "webconsole"),
                features(camelRepo, "camel-core", "camel-test-karaf"),
                mavenBundle().groupId("org.fcrepo.camel").artifactId("fcrepo3-shim").versionAsInProject().start() };
    }
}

package edu.si.camel.fcrepo3;

import static org.apache.jena.riot.Lang.NTRIPLES;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class ModelsIT extends ShimIT {

    Logger log = getLogger(ModelsIT.class);

    @Test
    public void testProvisioning() throws Exception {
        assertFeatureInstalled("camel-core", CAMEL_VERSION);
    }

    private static Model answer = ModelFactory.createDefaultModel();
    static String triples = "<info:fedora/fedora-system:FedoraObject-3.0> <info:fedora/fedora-system:def/model#hasModel> <info:fedora/fedora-system:FedoraObject-3.0> . "
                    + "<info:fedora/fedora-system:FedoraObject-3.0> <info:fedora/fedora-system:def/model#hasModel> <info:fedora/fedora-system:ContentModel-3.0> .";
    static {
        try (StringReader r = new StringReader(triples)) {
            answer.read(r, null, "N-TRIPLE");
        }
    }

    @Test
    public void testCorrectModels() throws IOException {
        Model models = RDFDataMgr.loadModel("http://localhost:9191/shim/fedora-system:FedoraObject-3.0/models",
                        NTRIPLES);

        if (!models.isIsomorphicWith(answer)) {
            log.error("Result:\n{}", models);
            log.error("Answer:\n{}", answer);
        }
        assertTrue("Got wrong triples!", models.isIsomorphicWith(answer));
    }

    @Configuration
    public Option[] config() {
        return super.config();
    }
}

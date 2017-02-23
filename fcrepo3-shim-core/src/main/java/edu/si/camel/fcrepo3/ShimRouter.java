package edu.si.camel.fcrepo3;

import static java.lang.String.join;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.apache.jena.riot.Lang.NTRIPLES;
import static org.apache.jena.riot.Lang.RDFXML;
import static org.apache.jena.riot.RDFDataMgr.parse;
import static org.apache.jena.riot.system.StreamRDFLib.writer;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.stream.Stream;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.jena.atlas.RuntimeIOException;
import org.slf4j.Logger;

public class ShimRouter extends RouteBuilder implements Vocabulary {

    private static final String NTRIPLES_MIMETYPE = NTRIPLES.getContentType().toString();

    private static final Logger log = getLogger(ShimRouter.class);

    private static final AggregationStrategy MERGE_TRIPLES = (Exchange oldRdf, Exchange newRdf) -> {
        if (newRdf == null) return oldRdf;
        if (oldRdf == null) return newRdf;
        String newBody = oldRdf.getIn().getBody(String.class) + "\n" + newRdf.getIn().getBody(String.class);
        oldRdf.getOut().setBody(newBody);
        return oldRdf;
    };

    private String fcrepo3;

    public String triplify(InputStream rdfXml) {
        try (StringWriter w = new StringWriter()) {
            parse(writer(w), rdfXml, RDFXML);
            return w.toString();
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    public String filterByDsId(String nTriples, Exchange exchange) {
        final String dsId = exchange.getIn().getHeader("Fcrepo3DatastreamId", String.class);
        final Stream<String[]> filteredTriples = stream(nTriples.split("\n")).map(t -> t.split(" "))
                        .filter(t -> t[0].equals(dsId));
        return filteredTriples.map(t -> join(" ", t)).collect(joining("\n"));

    }

    @Override
    public void configure() {
        getContext().setTracing(true);
        deadLetterChannel("log:shim.error");

        final Namespaces ns = new Namespaces("rdf", RDF);
        ns.add("ldp", LDP);
        ns.add("fedora-system", SYSTEM_NS);
        ns.add("fedora-view", VIEW_NS);
        ns.add("fedora-model", MODEL_NS);

        from("direct:sparql").streamCaching().to("log:shim.sparql").setHeader("Content-Type").constant("application/x-www-form-urlencoded")
                        .setHeader("CamelHttpChunked").constant(false).setHeader("CamelHttpMethod").constant("POST")
                        .to("velocity:sparql/package.vm").to("log:shim.sparql.packaged").to("http://{{fcrepo3}}/risearch?httpClient.soTimeout=6000")
                        .to("log:shim.sparql.result")
                        .removeHeader("Content-Type").removeHeader("CamelHttpChunked").removeHeader("CamelHttpMethod");

        rest("/shim").to("log:shim")

                        .get("/{Fcrepo3Pid}/models").to("log:shim.models").produces(NTRIPLES_MIMETYPE)
                        .to("velocity:sparql/getcontentmodels.vm").to("log:shim.models.getmodels").to("direct:sparql")

                        .get("/{Fcrepo3Pid}/{Fcrepo3DatastreamId}")
                        .to("http://{{fcrepo3}}/objects/{{Fcrepo3Pid}}/datastreams/{{Fcrepo3DatastreamId}}")

                        // object RDF
                        .get("/{Fcrepo3Pid}").produces(NTRIPLES_MIMETYPE).to("direct:rdf.object")

                        // datastream RDF
                        .get("/{Fcrepo3Pid}/datastreams/{{Fcrepo3DsId}}").produces(NTRIPLES_MIMETYPE)
                        .to("direct:rdf.datastream");

        from("direct:rdf.object").multicast().to("direct:foxml", "direct:rels-ext", "direct:dublin-core")
                        .aggregate(MERGE_TRIPLES);
        from("direct:rdf.datastream").multicast().to("direct:foxml", "direct:rels-int").aggregate(MERGE_TRIPLES);

        from("direct:foxml").to("http://{{fcrepo3}}/objects/{{Fcrepo3Pid}}/objectXML")
                        .to("xslt:org/fcrepo/camel/fcrepo3/foxml2rdf.xsl").to("direct:rdfxml2triples");

        from("direct:rdfxml2triples").transform(method(this, "triplify"));
    }

}

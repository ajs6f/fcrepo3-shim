package org.fcrepo.camel.fcrepo3;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.slf4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.JenaException;

public class ShimRouter extends RouteBuilder implements Vocabulary {

	private static final Logger log = getLogger(ShimRouter.class);

	private static final AggregationStrategy MERGE_TRIPLES = (Exchange oldRdf, Exchange newRdf) -> {
		if (newRdf == null) return oldRdf;
		if (oldRdf == null) return newRdf;
		String newBody = oldRdf.getIn().getBody(String.class) + "\n" + newRdf.getIn().getBody(String.class);
		oldRdf.getOut().setBody(newBody);
		return oldRdf;
	};
	
	public String triplify(InputStream rdfXml) {
		Model m = ModelFactory.createDefaultModel();
		m.read(rdfXml, null);
		try (StringWriter w = new StringWriter()) {
			m.write(w, "N-TRIPLE");
			return w.toString();
		} catch (IOException e) {
			throw new JenaException(e);
		}
	}
	
	@Override
	public void configure() {
		final Namespaces ns = new Namespaces("rdf", RDF);
		ns.add("ldp", LDP);
		ns.add("fedora-system", SYSTEM_NS);
		ns.add("fedora-view", VIEW_NS);
		ns.add("fedora-model", MODEL_NS);


		rest("/shim").get("/{Fcrepo3Pid}").to("direct:build.rdf");

		from("direct:build.rdf").multicast().shareUnitOfWork().to("direct:foxml").to("direct:rels-ext")
				.to("direct:rels-int").aggregationStrategy(MERGE_TRIPLES);

		from("direct:foxml").to("http://{{fcrepo3}}/objects/{{Fcrepo3Pid}}/relationships?format=n-triples").to("xslt:org/fcrepo/camel/fcrepo3/foxml2rdf.xsl")
				.to("direct:rdfxml2triples");

		from("direct:rels-ext").to("http://{{fcrepo3}}/").to("direct:rdfxml2triples");

		from("direct:rels-int").to("http://{{fcrepo3}}/").to("direct:rdfxml2triples");

		from("direct:rdfxml2triples").transform(method(this, "triplify"));
	}

}

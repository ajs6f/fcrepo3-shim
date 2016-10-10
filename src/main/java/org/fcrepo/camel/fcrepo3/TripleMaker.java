package org.fcrepo.camel.fcrepo3;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.camel.Handler;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.JenaException;

public class TripleMaker {

	@Handler
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

}

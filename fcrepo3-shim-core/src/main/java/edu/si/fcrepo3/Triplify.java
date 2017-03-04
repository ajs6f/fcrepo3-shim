package edu.si.fcrepo3;

import static org.apache.jena.riot.Lang.RDFXML;
import static org.apache.jena.riot.RDFDataMgr.parse;
import static org.apache.jena.riot.system.StreamRDFLib.writer;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.jena.atlas.RuntimeIOException;

public class Triplify {
    
    public String triplify(InputStream rdfXml) {
        try (StringWriter w = new StringWriter()) {
            parse(writer(w), rdfXml, RDFXML);
            return w.toString();
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

}

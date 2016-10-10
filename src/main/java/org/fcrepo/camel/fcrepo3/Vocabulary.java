package org.fcrepo.camel.fcrepo3;

import static com.hp.hpl.jena.graph.NodeFactory.createLiteral;
import static com.hp.hpl.jena.graph.NodeFactory.createURI;

import com.hp.hpl.jena.graph.Node;

public interface Vocabulary {

	public static final String REPOSITORY = "http://fedora.info/definitions/v4/repository#";
	public static final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String LDP = "http://www.w3.org/ns/ldp#";
	public static final String SYSTEM_NS = "info:fedora/fedora-system:";
	public static final String MODEL_NS = SYSTEM_NS + "def/model#";
	public static final String VIEW_NS = SYSTEM_NS + "def/view#";
	public static final Node CREATEDDATE = createURI(MODEL_NS + "createdDate");
	public static final Node LASTMODIFIEDDATE = createURI(VIEW_NS + "lastModifiedDate");
	public static final Node LABEL = createURI(MODEL_NS + "label");
	public static final Node OWNER = createURI(MODEL_NS + "ownerId");
	public static final Node STATE = createURI(MODEL_NS + "state");
	public static final Node ACTIVE = createURI(MODEL_NS + "Active");
	public static final Node INACTIVE = createURI(MODEL_NS + "Inactive");
	public static final Node DELETED = createURI(MODEL_NS + "Deleted");
	public static final Node MIME_TYPE = createURI(VIEW_NS + "mimeType");
	public static final Node IS_VOLATILE = createURI(VIEW_NS + "isVolatile");
	public static final Node DISSEMINATES = createURI(VIEW_NS + "disseminates");
	public static final Node DISSEMINATION_TYPE = createURI(VIEW_NS + "disseminationType");
	public static final Node FALSE = createLiteral("false");
	public static final Node TRUE = createLiteral("true");
	public static final Node HAS_MODEL = createURI(MODEL_NS + "hasModel");
	public static final Node FEDORA_OBJECT = createURI(SYSTEM_NS + "FedoraObject-3.0");


}
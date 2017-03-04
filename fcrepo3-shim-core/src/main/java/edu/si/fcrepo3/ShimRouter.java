package edu.si.fcrepo3;

import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShimRouter extends RouteBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(ShimRouter.class);

    @Override
    public void configure() throws Exception {
        LOG.info("Configuring ShimRouter");
        
        restConfiguration().component("servlet").contextPath("/shim").port("{{shim.port}}");
        rest()
            .get("/{pid}/triples/object").route()
                .setHeader("CamelHttpChunked").constant(false)
                .to("log:shim.triples.object")
                .setHeader("CamelHttpChunked").constant(false)
                .removeHeader("CamelHttpUrl")
                .removeHeader("CamelHttpPath")
                .setHeader("CamelHttpUri").simple("{{fcrepo3.uri}}/objects/${header.pid}/objectXML")
                .to("http4://dummy?authUsername=fedoraAdmin&authPassword=fedoraAdmin")
                .removeHeader("CamelHttpUri")
                .removeHeader("CamelHttpChunked")
                .to("xslt:xslt/foxml.xslt")
                .to("bean:triplify").endRest()
            .get("/{pid}/models").route()
                .to("log:shim.models")
                .to("velocity:sparql/getcontentmodels.vm")
                .to("log:shim.models.getmodels")
                .to("direct:sparql").endRest();
         from("direct:sparql")
             .to("log:shim.sparql")
             .setHeader("Content-Type").constant("application/x-www-form-urlencoded")
             .setHeader("CamelHttpChunked").constant(false)
             .setHeader("CamelHttpMethod").constant("POST")
             .removeHeader("CamelHttpUrl")
             .removeHeader("CamelHttpPath")
             .setHeader("CamelHttpUri").constant("{{fcrepo3.uri}}/risearch")
             .to("velocity:sparql/package.vm")
             .to("log:shim.sparql.packaged")
             .to("http4://dummy?authUsername=fedoraAdmin&authPassword=fedoraAdmin")
             .log("SPARQL result: \n${body}")
             .to("log:shim.sparql.result")
             .removeHeader("CamelHttpUri")
             .removeHeader("CamelHttpMethod")
             .removeHeader("CamelHttpChunked")
             .removeHeader("Content-Type");
    }

}

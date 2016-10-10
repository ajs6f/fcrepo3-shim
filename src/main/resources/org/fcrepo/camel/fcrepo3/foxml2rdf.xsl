<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:foxml="info:fedora/fedora-system:def/foxml#" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:fedora-view="info:fedora/fedora-system:def/view#" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:fedora="info:fedora/fedora-system:def/relations-external#"
    xmlns:fedora-model="info:fedora/fedora-system:def/model#" 
    xsi:schemaLocation="info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-1.xsd">
    <xsl:output indent="yes"/>
    
    <xsl:template match="foxml:digitalObject">
        <rdf:RDF>
            <rdf:Description rdf:about="info:fedora/{@PID}">
                <xsl:apply-templates/>
            </rdf:Description>
        </rdf:RDF>
    </xsl:template>

    <xsl:template match="foxml:datastream">
        <fedora-view:disseminates rdf:resource="info:fedora/{../@PID}/{@ID}"/>
    </xsl:template>

    <xsl:template match="foxml:property[@NAME ='info:fedora/fedora-system:def/model#state']">
        <fedora-model:state>
            <xsl:value-of select="@VALUE"/>
        </fedora-model:state>
    </xsl:template>

    <xsl:template match="foxml:property[@NAME ='info:fedora/fedora-system:def/model#createdDate']">
        <fedora-model:createdDate>
            <xsl:value-of select="@VALUE"/>
        </fedora-model:createdDate>
    </xsl:template>

</xsl:transform>

<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:foxml="info:fedora/fedora-system:def/foxml#" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:fedora-view="info:fedora/fedora-system:def/view#" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:fedora="info:fedora/fedora-system:def/relations-external#"
    xmlns:fedora-model="info:fedora/fedora-system:def/model#"
    xsi:schemaLocation="info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-1.xsd">

    <xsl:template match="foxml:digitalObject">
        <xsl:variable name="pid" select="@PID"/>
        <xsl:variable name="objUri" select="concat('info:fedora/',$pid)"/>
        <rdf:RDF>
            <rdf:Description rdf:about="info:fedora/$pid">
                <fedora-model:hasModel rdf:resource="info:fedora/si:resourceCModel"/>
                <fedora-model:hasModel rdf:resource="info:fedora/si:fieldbookCModel"/>
                <xsl:for-each select="foxml:datastream">
                    <fedora-view:disseminates rdf:resource="{$objUri}/{@ID})"/>
                </xsl:for-each>

            </rdf:Description>
        </rdf:RDF>

    </xsl:template>

</xsl:transform>

<?xml version="1.0" encoding="UTF-8"?>
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:foxml="info:fedora/fedora-system:def/foxml#" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:fedora-view="info:fedora/fedora-system:def/view#" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:fedora="info:fedora/fedora-system:def/relations-external#"
    xmlns:fedora-model="info:fedora/fedora-system:def/model#"
    exclude-result-prefixes="foxml">

    <xsl:output indent="yes"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="foxml:digitalObject">
        <rdf:RDF>
            <rdf:Description rdf:about="info:fedora/{@PID}">
                <xsl:apply-templates/>
            </rdf:Description>
        </rdf:RDF>
    </xsl:template>

    <xsl:template match="foxml:datastream">
        <fedora-view:disseminates>
            <rdf:Description rdf:about="info:fedora/{../@PID}/{@ID}">
                <fedora-view:disseminationType rdf:resource="info:fedora/*/{@ID}"/>
                <xsl:choose>
                    <xsl:when test="starts-with(@STATE, 'A')">
                        <fedora-model:state rdf:resource="info:fedora/fedora-system:def/model#ACTIVE"/>
                    </xsl:when>
                    <xsl:when test="starts-with(@STATE, 'I')">
                        <fedora-model:state rdf:resource="info:fedora/fedora-system:def/model#INACTIVE"/>
                    </xsl:when>
                    <xsl:when test="starts-with(@STATE, 'D')">
                        <fedora-model:state rdf:resource="info:fedora/fedora-system:def/model#DELETE"/>
                    </xsl:when>
                </xsl:choose>
                <xsl:choose>
                    <xsl:when test="starts-with(@CONTROL_GROUP, 'R') or starts-with(@CONTROL_GROUP, 'E')">
                        <fedora-view:isVolatile>true</fedora-view:isVolatile>
                    </xsl:when>
                    <xsl:when test="starts-with(@CONTROL_GROUP, 'M') or starts-with(@CONTROL_GROUP, 'X')">
                        <fedora-view:isVolatile>false</fedora-view:isVolatile>
                    </xsl:when>
                </xsl:choose>
                <xsl:apply-templates/>
            </rdf:Description>
        </fedora-view:disseminates>
    </xsl:template>
    
    <xsl:template match="foxml:datastreamVersion[1]">
        <fedora-view:mimeType>
            <xsl:value-of select="@MIMETYPE"/>
        </fedora-view:mimeType>
        <fedora-view:lastModifiedDate>
            <xsl:value-of select="@CREATED"/>
        </fedora-view:lastModifiedDate>
    </xsl:template>

    <xsl:template match="foxml:property[@NAME ='info:fedora/fedora-system:def/model#state']">
        <fedora-model:state rdf:resource="info:fedora/fedora-system:def/model#{@VALUE}"/>
    </xsl:template>

    <xsl:template match="foxml:property[@NAME ='info:fedora/fedora-system:def/model#createdDate']">
        <fedora-model:createdDate>
            <xsl:value-of select="@VALUE"/>
        </fedora-model:createdDate>
    </xsl:template>

    <xsl:template match="foxml:property[@NAME ='info:fedora/fedora-system:def/view#lastModifiedDate']">
        <fedora-view:lastModifiedDate>
            <xsl:value-of select="@VALUE"/>
        </fedora-view:lastModifiedDate>
    </xsl:template>

    <xsl:template match="foxml:property[@NAME ='info:fedora/fedora-system:def/model#ownerId']">
        <fedora-model:owner>
            <xsl:value-of select="@VALUE"/>
        </fedora-model:owner>
    </xsl:template>

    <xsl:template match="foxml:property[@NAME ='info:fedora/fedora-system:def/model#label']">
        <fedora-model:label>
            <xsl:value-of select="@VALUE"/>
        </fedora-model:label>
    </xsl:template>

</xsl:transform>

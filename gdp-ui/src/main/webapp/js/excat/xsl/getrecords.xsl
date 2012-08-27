<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
                xmlns:ogc="http://www.opengis.net/ogc"
                xmlns:ows="http://www.opengis.net/ows"
                xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
                xmlns:gml="http://www.opengis.net/gml"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xlink="http://www.w3.org/1999/xlink">

    <xsl:output method="xml" encoding="utf-8" omit-xml-declaration="yes"/>
    <xsl:strip-space elements="*"/>


    <!-- Match Root -->
    <xsl:template match="/defaults">

        <csw:GetRecords 
            xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
            service = "CSW">
            <xsl:attribute name="version">
                <xsl:value-of select="./version"/>
            </xsl:attribute>
            <xsl:attribute name="maxRecords">
                <xsl:value-of select="./maxrecords"/>
            </xsl:attribute>
            <xsl:attribute name="startPosition">
                <xsl:value-of select="./startposition"/>
            </xsl:attribute>
            <xsl:attribute name="outputFormat">
                <xsl:value-of select="./outputformat"/>
            </xsl:attribute>
            <xsl:attribute name="outputSchema">
                <xsl:value-of select="./outputschema"/>
            </xsl:attribute>
            <xsl:attribute name="resultType">
                <xsl:value-of select="./resulttype"/>
            </xsl:attribute>
            <csw:Query typeNames="csw:Record">
                <csw:ElementSetName>full</csw:ElementSetName>
                <!-- Don't add Constraint if  search term is empty; this keeps Geonetwork happy -->
                <csw:Constraint version="1.1.0">
                    <ogc:Filter xmlns:ogc="http://www.opengis.net/ogc" xmlns="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
                        <ogc:And>
                            <xsl:if test="./literal !=''">
                                <ogc:PropertyIsLike escape="\" singleChar="_" wildCard="%">
                                    <ogc:PropertyName>
                                        <xsl:value-of select="./propertyname"/>
                                    </ogc:PropertyName>
                                    <ogc:Literal>
                                        <xsl:value-of select="./literal"/>
                                    </ogc:Literal>
                                </ogc:PropertyIsLike>
                            </xsl:if>
                            <ogc:BBOX>
                                <ogc:PropertyName>ows:BoundingBox</ogc:PropertyName>
                                <gml:Envelope>
                                    <gml:lowerCorner>
                                        <xsl:value-of select="./bboxlc"/>
                                    </gml:lowerCorner>
                                    <gml:upperCorner>
                                        <xsl:value-of select="./bboxuc"/>
                                    </gml:upperCorner>
                                </gml:Envelope>
                            </ogc:BBOX>
                            <!-- ScienceBase integration: If we are in the context of searching for 
                                SB features, we need to query the server to only return features
                                and if we're searching for covrages, only return services with 
                                OGC-WCS in them -->
                            <xsl:if test="./scienceBaseFeature = 'true'">
                                <ogc:PropertyIsLike wildCard="*" singleChar="." escapeChar="!">
                                    <ogc:PropertyName>apiso:resource.serviceId</ogc:PropertyName>
                                    <ogc:Literal>*OGC-WFS*</ogc:Literal>
                                </ogc:PropertyIsLike>
                            </xsl:if>
                            <xsl:if test="./scienceBaseCoverage = 'true'">
                                <ogc:PropertyIsLike wildCard="*" singleChar="." escapeChar="!">
                                    <ogc:PropertyName>apiso:resource.serviceId</ogc:PropertyName>
                                    <ogc:Literal>*OGC-WCS*</ogc:Literal>
                                </ogc:PropertyIsLike>
                            </xsl:if>
                        </ogc:And>
                    </ogc:Filter>
                </csw:Constraint>
                <ogc:SortBy xmlns:ogc="http://www.opengis.net/ogc">
                    <xsl:if test="./sortby !=''">
                        <ogc:SortProperty>
                            <ogc:PropertyName>
                                <xsl:value-of select="./sortby"/>
                            </ogc:PropertyName>
                            <ogc:SortOrder>
                                <xsl:value-of select="./sortorder"/>
                            </ogc:SortOrder>
                        </ogc:SortProperty>
                    </xsl:if>
                </ogc:SortBy>
            </csw:Query>
        </csw:GetRecords>

    </xsl:template>
</xsl:stylesheet>

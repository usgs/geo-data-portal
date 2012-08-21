<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:csw="http://www.opengis.net/cat/csw/2.0.2">
    <xsl:output method="html" encoding="ISO-8859-1"/>
    
    <xsl:variable name="wmsEndpoint">
        <xsl:choose>
            <xsl:when test="*[local-name()='GetRecordByIdResponse']/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification[@id='OGC-WMS']">
                <xsl:value-of select="*[local-name()='GetRecordByIdResponse']/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification[@id='OGC-WMS']/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL" />
            </xsl:when>
            <xsl:otherwise>
                false
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>

    <!-- TODO: This is not yet tested -->    
    <xsl:variable name="useCache">
        <xsl:choose>
            <xsl:when test="*[local-name()='GetRecordByIdResponse']/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:status/gmd:MD_ProgressCode">
                true
            </xsl:when>
            <xsl:otherwise>
                false
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    
    <xsl:template match="/">
        <div class="captioneddiv">
            <h3 id="multiple_records_returned">Multiple Records Returned</h3>
            <br />
            Select sub data set
            <ol>
                <xsl:apply-templates select="/*[local-name()='GetRecordByIdResponse']/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification[not(@id='OGC-WMS')]"/>
            </ol>
        </div>
    </xsl:template>

    <xsl:template match="*[local-name()='GetRecordByIdResponse']/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification[not(@id='OGC-WMS')]">
        <li>
            <a>
                <xsl:attribute name="title">
                    <xsl:value-of select="./gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
                </xsl:attribute>
                
                <xsl:attribute name="href">
                    <xsl:text>javascript:CSWClient.selectSubdataset</xsl:text>
                    
                    <!-- dataset URL -->
                    <xsl:text>('</xsl:text>
                    <xsl:value-of select="./srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
                    
                    <!-- WMS endpoint -->
                    <xsl:text>','</xsl:text>
                    <xsl:value-of select="$wmsEndpoint"/>
                    
                    <xsl:text>','</xsl:text>
                    <!-- title -->
                    <xsl:choose>
                        <xsl:when test="./gmd:citation/gmd:CI_Citation/gmd:title[@gco:nilReason]">
                            <xsl:text></xsl:text>
                        </xsl:when>
                        <xsl:when test="./gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString">
                            <xsl:value-of select="./gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text></xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                    
                    <xsl:text>',</xsl:text>
                    <!-- useCache -->
                    <xsl:value-of select="$useCache"/>
                    
                    <xsl:text>);</xsl:text>
                </xsl:attribute>
                    
                <xsl:choose>
                    <xsl:when test="./gmd:citation/gmd:CI_Citation/gmd:title[@gco:nilReason]">
                        <xsl:text>[ Title Unavailable ]</xsl:text>
                    </xsl:when>
                    <xsl:when test="./gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString">
                        <xsl:value-of select="./gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text> ...</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </a>
            <hr/>
        </li>
    </xsl:template>
    
</xsl:stylesheet>

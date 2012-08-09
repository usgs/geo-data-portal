<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:csw="http://www.opengis.net/cat/csw/2.0.2">
    <xsl:output method="html" encoding="ISO-8859-1"/>


    <xsl:template match="/">
        
        <div class="captioneddiv">
            <h3 id="multiple_records_returned">Multiple Records Returned</h3>
            <br />
            Select sub data set
            <ol>
                <xsl:apply-templates select="/*[local-name()='GetRecordByIdResponse']/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification"/>
            </ol>
        </div>
    </xsl:template>

    <xsl:template match="*[local-name()='GetRecordByIdResponse']/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification">
        <li>
            <a>
                <xsl:attribute name="title">
                    <xsl:value-of select="./gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
                </xsl:attribute>
                    
                <xsl:attribute name="href">
                    <xsl:text>javascript:(CSWClient.setDatasetUrl</xsl:text>
                    <xsl:text>('</xsl:text>
                    <xsl:value-of select="./srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
                    <xsl:text>'))</xsl:text>
                </xsl:attribute>
                    
                <xsl:choose>
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

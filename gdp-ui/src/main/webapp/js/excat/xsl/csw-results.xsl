<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:srv="http://www.isotc211.org/2005/srv">
    <!--xsl:output method="html" encoding="ISO-8859-1"/-->


    <xsl:template match="/results/*[local-name()='GetRecordsResponse']">
        <xsl:apply-templates select="./*[local-name()='SearchResults']"/>
    </xsl:template>
    
    <xsl:variable name="fromScienceBase">
        <xsl:choose>
            <xsl:when test="/results/@fromScienceBase">
                <xsl:value-of select="/results/@fromScienceBase" />
            </xsl:when>
            <xsl:otherwise>
                false
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    
    <xsl:variable name="pageUrl">
        <xsl:choose>
            <xsl:when test="$fromScienceBase = 'true'">
                <xsl:text>javascript:(CSWClient.getRecordsFromScienceBase</xsl:text>
                <xsl:text>('</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>javascript:(CSWClient.getRecords</xsl:text>
                <xsl:text>('</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>

    <xsl:template match="*[local-name()='SearchResults']">
        

        
        <xsl:variable name="start">
            <xsl:value-of select="../../request/@start"/>
        </xsl:variable>

        <!-- because GeoNetwork does not return nextRecord we have to do some calculation -->
        <xsl:variable name="next">
            <xsl:choose>
                <xsl:when test="@nextRecord">
                    <xsl:value-of select="@nextRecord"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:choose>
                        <xsl:when test="number(@numberOfRecordsMatched) >= (number($start) + number(@numberOfRecordsReturned))">
                            <xsl:value-of select="number($start) + number(@numberOfRecordsReturned)"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="0"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <div class="captioneddiv">
            
            <!--xsl:if test="number(@numberOfRecordsMatched) > number(@numberOfRecordsReturned)"-->
            <!-- because ESRI GPT returns always numberOfRecordsMatched = 0 -->
            <xsl:if test="number(@numberOfRecordsReturned) > 0 and ($start > 1 or number($next) > 0)">
                <h3 id='prev_next' style="float:right;top: -2.5em;">
                    <xsl:if test="$start > 1">
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of select="$pageUrl"/>
                                <xsl:value-of select="number($start)-number(../../request/@maxrecords)"/>
                                <xsl:text>'))</xsl:text>
                            </xsl:attribute>
                            <xsl:text>&lt;&lt; previous</xsl:text>
                        </a>
                    </xsl:if>
                    <xsl:text>  || </xsl:text>
                    <xsl:if test="number($next) > 0 and (number(@nextRecord) &lt;= number(@numberOfRecordsMatched))">
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of select="$pageUrl"/>
                                <xsl:value-of select="$next"/>
                                <xsl:text>'))</xsl:text>
                            </xsl:attribute>
                            <xsl:text>next &gt;&gt;</xsl:text>
                        </a>
                    </xsl:if>
                </h3>
            </xsl:if>

            <h3 id="total_records_returned">
                <xsl:text>Total records returned: </xsl:text>
                <xsl:choose>
                    <xsl:when test="@nextRecord > 0">
                        <xsl:value-of select="@nextRecord - @numberOfRecordsReturned"/>
                        <xsl:text> - </xsl:text>
                        <xsl:value-of select="@nextRecord - 1"/>
                        <xsl:text> (of </xsl:text>
                        <xsl:value-of select="@numberOfRecordsMatched"/>
                        <xsl:text>)</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="@numberOfRecordsReturned"/>
                    </xsl:otherwise>
                </xsl:choose>
            </h3>
    
            <br/>
            <ol>
                <xsl:attribute name="start">
                    <xsl:value-of select="$start"/>
                </xsl:attribute>

                <xsl:apply-templates select="./*[local-name()='SummaryRecord']"/>
                <xsl:apply-templates select="./gmd:MD_Metadata"/>
            </ol>
        </div>
    </xsl:template>

    <xsl:template match="*[local-name()='SummaryRecord']">
        <xsl:for-each select=".">
            <li>
                <strong>
                    <xsl:text>Title: </xsl:text>
                </strong>
                <a>
                    <xsl:attribute name="href">
                        <xsl:text>javascript:(CSWClient.selectDatasetById</xsl:text>
                        <xsl:text>('</xsl:text>
                        <xsl:value-of select="./dc:identifier"/>
                        <xsl:text>','</xsl:text>
                        <xsl:value-of select="./dc:title"/>
                        <xsl:text>'))</xsl:text>
                    </xsl:attribute>
                    
                    <xsl:attribute name="class">
                        <xsl:text>li-dataset</xsl:text>
                    </xsl:attribute>
                    
                    <xsl:choose>
                        <xsl:when test="./dc:title">
                            <xsl:apply-templates select="./dc:title"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text> ...</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </a>
                <br/>
                <xsl:apply-templates select="./dct:abstract"/>
                <br/>
                <strong>
                    <xsl:text>Keywords: </xsl:text>
                </strong>
                <xsl:for-each select="./dc:subject">
                    <xsl:if test=".!=''">
                        <xsl:if test="position() &gt; 1">, </xsl:if>
                        <i>
                            <xsl:value-of select="."/>
                        </i>
                    </xsl:if>
                </xsl:for-each>
                <br/>
                <a>
                    <xsl:attribute name="href">
                        <xsl:text>javascript:(CSWClient.popupMetadataById</xsl:text>
                        <xsl:text>('</xsl:text>
                        <xsl:value-of select="./dc:identifier"/>
                        <xsl:text>'))</xsl:text>
                    </xsl:attribute>
                    <xsl:text>Full Record</xsl:text>
                </a>
                <hr/>
            </li>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="dc:title">
        <xsl:choose>
            <xsl:when test=".!=''">
                <xsl:value-of select="."/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text> ...</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="dct:abstract">
        <strong>
            <xsl:text>Abstract: </xsl:text>
        </strong>
        <xsl:value-of select="substring(.,1,250)"/>
        <xsl:if test="string-length(.) &gt; 250">
            <xsl:text> ...</xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template match="gmd:MD_Metadata">
        
        <xsl:for-each select=".">
            <xsl:variable name="opendapServicesCount" select="count(./gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceType/gco:LocalName[contains(.,'OPeNDAP')])" />
            <li>
                <strong>
                    <xsl:text>Title: </xsl:text>
                </strong>
                <a>
                    <xsl:attribute name="title">
                        <xsl:text>Select A Data Set</xsl:text>
                    </xsl:attribute>
                    
                    <xsl:attribute name="class">
                        <xsl:text>li-dataset</xsl:text>
                    </xsl:attribute>
                    
                    <xsl:choose>
                        <xsl:when test="$fromScienceBase = 'true'">
                            <xsl:attribute name="href">
                                <xsl:text>javascript:(CSWClient.selectFeatureById</xsl:text>
                                <xsl:text>('</xsl:text>
                                <xsl:value-of select="./gmd:fileIdentifier/gco:CharacterString"/>
                                <xsl:text>'))</xsl:text>
                            </xsl:attribute>
                        </xsl:when>
                        <xsl:when test="$opendapServicesCount &gt; 1">
                            <xsl:attribute name="href">
                                <xsl:text>javascript:(CSWClient.displayMultipleOpenDAPSelection</xsl:text>
                                <xsl:text>('</xsl:text>
                                <xsl:value-of select="./gmd:fileIdentifier/gco:CharacterString"/>
                                <xsl:text>'))</xsl:text>
                            </xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="href">
                                <xsl:text>javascript:(CSWClient.selectDatasetById</xsl:text>
                                <xsl:text>('</xsl:text>
                                <xsl:value-of select="./gmd:fileIdentifier/gco:CharacterString"/>
                                <xsl:text>','</xsl:text>
                                <xsl:value-of select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
                                <xsl:text>'))</xsl:text>
                            </xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:choose>
                        <xsl:when test="./gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString">
                            <xsl:apply-templates select="./gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
                        </xsl:when>
                        <xsl:when test="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString">
                            <xsl:apply-templates select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text> ...</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </a>
                <br />
                <xsl:choose>
                    <xsl:when test="./gmd:identificationInfo/srv:SV_ServiceIdentification[@id='OGC-WFS']/gmd:abstract/gco:CharacterString">
                        <xsl:apply-templates select="./gmd:identificationInfo/srv:SV_ServiceIdentification[@id='OGC-WFS']/gmd:abstract/gco:CharacterString" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString" />
                    </xsl:otherwise>
                </xsl:choose>
                <hr/>
            </li>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString">
        <xsl:choose>
            <xsl:when test=".!=''">
                <xsl:value-of select="."/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text> ...</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString">
        <strong>
            <xsl:text>Abstract: </xsl:text>
        </strong>
        <xsl:value-of select="substring(.,1,500)"/>
        <xsl:if test="string-length(.) &gt; 500">
            <xsl:text> ...</xsl:text>
        </xsl:if>
        <br/>
        <a>
            <xsl:attribute name="href">
                <xsl:text>javascript:(CSWClient.popupMetadataById</xsl:text>
                <xsl:text>('</xsl:text>
                <xsl:value-of select="../../../../gmd:fileIdentifier/gco:CharacterString"/>
                <xsl:text>'))</xsl:text>
            </xsl:attribute>
            <xsl:text>Full Record</xsl:text>
        </a>
    </xsl:template>
    
    <xsl:template match="gmd:identificationInfo/srv:SV_ServiceIdentification[@id='OGC-WFS']/gmd:abstract/gco:CharacterString">
        <strong>
            <xsl:text>Abstract: </xsl:text>
        </strong>
        <xsl:value-of select="substring(.,1,500)"/>
        <xsl:if test="string-length(.) &gt; 500">
            <xsl:text> ...</xsl:text>
        </xsl:if>
        <br/>
        <a>
            <xsl:attribute name="href">
                <xsl:text>javascript:(CSWClient.popupMetadataById</xsl:text>
                <xsl:text>('</xsl:text>
                <xsl:value-of select="../../../../gmd:fileIdentifier/gco:CharacterString"/>
                <xsl:text>'))</xsl:text>
            </xsl:attribute>
            <xsl:text>Full Record</xsl:text>
        </a>
    </xsl:template>

</xsl:stylesheet>

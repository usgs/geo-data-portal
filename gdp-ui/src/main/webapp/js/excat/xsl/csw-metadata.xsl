<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:xs="http://www.w3.org/2001/XMLSchema" 
     xmlns:gco="http://www.isotc211.org/2005/gco"
     xmlns:gmd="http://www.isotc211.org/2005/gmd"
     xmlns:dc="http://purl.org/dc/elements/1.1/"
     xmlns:dct="http://purl.org/dc/terms/"
     xmlns:ows="http://www.opengis.net/ows"
     xmlns:cat="http://www.esri.com/metadata/csw/"
     xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
     xmlns:gml="http://www.opengis.net/gml"
     xmlns:gmd2="http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2"
     xmlns:gts="http://www.isotc211.org/2005/gts"
     xmlns:gmx="http://www.isotc211.org/2005/gmx"
     xmlns:gss="http://www.isotc211.org/2005/gss"
     xmlns:srv="http://www.isotc211.org/2005/srv"
     xmlns:gsr="http://www.isotc211.org/2005/gsr"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:gmi="http://www.isotc211.org/2005/gmi"
     xmlns:nc="http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2"
     xmlns:xlink="http://www.w3.org/1999/xlink"
     xmlns:geonet="http://www.fao.org/geonetwork"
     version="2.0">

<!--    <xsl:output method="html" encoding="ISO-8859-1"/>-->

    <xsl:template match="/">
        <div>
            <xsl:apply-templates/>
        </div>
    </xsl:template>

    <xsl:template match="/*[local-name()='GetRecordByIdResponse']">
        <xsl:apply-templates select="cat:FullRecord"/>
        <xsl:apply-templates select="*[local-name()='Record']"/>
        <xsl:apply-templates select="*[local-name()='SummaryRecord']"/>
        <xsl:apply-templates select="*[local-name()='BriefRecord']"/>
        <xsl:apply-templates select="gmd:MD_Metadata"/>
        <xsl:apply-templates select="Metadata"/>
        <xsl:apply-templates select="metadata"/>
    </xsl:template>

    <xsl:template match="cat:FullRecord">
        <xsl:apply-templates select="metadata"/>
    </xsl:template>


<!-- Start Metadata ISO19139 -->
    <xsl:template match="gmd:MD_Metadata">
        
        <xsl:variable name="keywordsVocabulary">
            <xsl:for-each select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString">
                <xsl:choose>
                    <xsl:when test="position()!=last()">
                        <xsl:value-of select='concat(.,", ")'/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select='.'/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:variable>        
        
        <xsl:apply-templates select="./gmd:identificationInfo/gmd:MD_DataIdentification"/>
        <xsl:apply-templates select="./gmd:contentInfo/gmd:MD_CoverageDescription"/>
        <xsl:apply-templates select="./gmd:spatialRepresentationInfo/gmd:MD_GridSpatialRepresentation"/>

<!-- Metadata block -->
        <div class="captioneddiv">
            <h3>Metadata</h3>
            <table class="meta">
                <tr></tr>
                
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'File Identifier'"/>
                    <xsl:with-param name="cvalue" select="./gmd:fileIdentifier/gco:CharacterString"/>
                </xsl:call-template>
                
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Language'"/>
                    <xsl:with-param name="cvalue" select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:language/gco:CharacterString"/>
                </xsl:call-template>
                
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Character set'"/>
                    <xsl:with-param name="cvalue" select="./gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue"/>
                </xsl:call-template>
                
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Metadata standard name'"/>
                    <xsl:with-param name="cvalue" select="./gmd:metadataStandardName/gco:CharacterString"/>
                </xsl:call-template>
                
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Metadata standard version'"/>
                    <xsl:with-param name="cvalue" select="./gmd:metadataStandardVersion/gco:CharacterString"/>
                </xsl:call-template>
                
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Keywords Vocabulary'"/>
                    <xsl:with-param name="cvalue" select="$keywordsVocabulary"/>
                </xsl:call-template>
                
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'CDM Data Type'"/>
                    <xsl:with-param name="cvalue" select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:spatialRepresentationType/gmd:MD_SpatialRepresentationTypeCode"/>
                </xsl:call-template>
                
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'History'"/>
                    <xsl:with-param name="cvalue" select="./gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:statement/gco:CharacterString"/>
                </xsl:call-template>
                
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Comment'"/>
                    <xsl:with-param name="cvalue" select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:supplementalInformation"/>
                </xsl:call-template>
                
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Publisher Name'"/>
                    <xsl:with-param name="cvalue" select="./gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode[@codeListValue='publisher']/../../gmd:organisationName/gco:CharacterString"/>
                </xsl:call-template>
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Publisher Name'"/>
                    <xsl:with-param name="cvalue" select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/gmd:CI_ResponsibleParty/gmd:CI_RoleCode[@codeListValue='publisher']/../../gmd:individualName/gco:CharacterString"/>
                </xsl:call-template>
                
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Publisher URL'"/>
                    <xsl:with-param name="cvalue" select="./gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
                </xsl:call-template>
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Publisher URL'"/>
                    <xsl:with-param name="cvalue" select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/gmd:CI_ResponsibleParty/gmd:CI_RoleCode[@codeListValue='publisher']/../../gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
                </xsl:call-template>
                
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Publisher E-Mail'"/>
                    <xsl:with-param name="cvalue" select="./gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString"/>
                </xsl:call-template>                
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Publisher E-Mail'"/>
                    <xsl:with-param name="cvalue" select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:citedResponsibleParty/gmd:CI_ResponsibleParty/gmd:CI_RoleCode[@codeListValue='publisher']/../../gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString"/>
                </xsl:call-template>

                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Date Stamp'"/>
                    <xsl:with-param name="cvalue" select="./gmd:dateStamp/gco:Date"/>
                </xsl:call-template>
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Date Stamp'"/>
                    <xsl:with-param name="cvalue" select="./gmd:dateStamp/gco:DateTime"/>
                </xsl:call-template>

                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Date Modified'"/>
                    <xsl:with-param name="cvalue" select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode[@codeListValue='revision']/../../gmd:date/gco:Date"/>
                </xsl:call-template>
                
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Date Published'"/>
                    <xsl:with-param name="cvalue" select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode[@codeListValue='publication']/../../gmd:date/gco:Date"/>
                </xsl:call-template>
                
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Geospatial Lat Units'"/>
                    <xsl:with-param name="cvalue" select="./gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:axisDimensionProperties/gmd:MD_Dimension/gmd:resolution/gco:Measure/@uom"/>
                </xsl:call-template>
                
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Geospatial Lat Res'"/>
                    <xsl:with-param name="cvalue" select="./gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:axisDimensionProperties/gmd:MD_Dimension/gmd:resolution/gco:Measure"/>
                </xsl:call-template>

                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Geospatial Lon Units'"/>
                    <xsl:with-param name="cvalue" select="./gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:axisDimensionProperties/gmd:MD_Dimension/gmd:resolution/gco:Measure/@uom"/>
                </xsl:call-template>
                
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Geospatial Lon Res'"/>
                    <xsl:with-param name="cvalue" select="./gmd:spatialRepresentationInfo/gmd:MD_Georectified/gmd:axisDimensionProperties/gmd:MD_Dimension/gmd:resolution/gco:Measure"/>
                </xsl:call-template>

                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Geospatial Vertical Units'"/>
                    <xsl:with-param name="cvalue" select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:verticalCRS"/>
                </xsl:call-template>

            </table>
            
            <xsl:apply-templates select="./gmd:contact"/>
            
            <div class="captioneddiv">
                <h3>Distribution Information</h3>
                <xsl:for-each select="./gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor">
                    <xsl:variable name="distId" select="./gmd:distributorFormat/gmd:MD_Format/gmd:name/gco:CharacterString" />
                    <xsl:variable name="distURL" select="./gmd:distributorTransferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL" />
                    <xsl:if test="($distId = 'OPeNDAP' or $distId = 'NetCDF Subset' or $distId = 'OGC-WMS') and $distURL != ''">
                        <table class="meta">
                            <tr>
                                <td class="meta" valign="top">
                                    <xsl:call-template name="tableHtmlRow">
                                        <xsl:with-param name="cname" select="$distId"/>
                                        <xsl:with-param name="cvalue" select="$distURL"/>
                                    </xsl:call-template>
                                </td>
                            </tr>
                        </table>
                    </xsl:if>
                </xsl:for-each>
            </div> 
            
            <div class="captioneddiv">
                <h3>Service Identification</h3>
                <xsl:for-each select="./gmd:identificationInfo/srv:SV_ServiceIdentification">
                    <xsl:variable name="serviceId" select="./@id" />
                    <xsl:variable name="serviceURL" select="./srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL" />
                    <xsl:if test="($serviceId = 'OPeNDAP' or $serviceId = 'THREDDS_NetCDF_Subset' or $serviceId = 'OGC-WMS') and $serviceURL != ''">
                
                        <table class="meta">
                            <tr>
                                <td class="meta" valign="top">
                                    <xsl:call-template name="tableHtmlRow">
                                        <xsl:with-param name="cname" select="$serviceId"/>
                                        <xsl:with-param name="cvalue" select="$serviceURL"/>
                                    </xsl:call-template>
                                </td>
                            </tr>
                        </table>
                    </xsl:if>
                </xsl:for-each>
            </div>
        </div>
    </xsl:template>
    
    <xsl:template match="gmd:MD_DataIdentification">
        
        <div class="captioneddiv">
            <h3>Identification Information</h3>
            <table class="meta">
                <tr></tr>
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Title'"/>
                    <xsl:with-param name="cvalue" select="./gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
                </xsl:call-template>
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Alt Title'"/>
                    <xsl:with-param name="cvalue" select="./gmd:citation/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString"/>
                </xsl:call-template>
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Date'"/>
                    <xsl:with-param name="cvalue" select="./gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:data/gco:Date"/>
                </xsl:call-template>
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Individual name'"/>
                    <xsl:with-param name="cvalue" select="./gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:individualName/gco:CharacterString"/>
                </xsl:call-template>
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Organisation name'"/>
                    <xsl:with-param name="cvalue" select="./gmd:pointOfContact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString"/>
                </xsl:call-template>
                <tr>
                    <td class="meta-param">Abstract:</td>
                    <td class="meta-value">
                        <xsl:apply-templates select="./gmd:abstract"/>
                    </td>
                </tr>
                
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Project'"/>
                    <xsl:with-param name="cvalue" select="./gmd:aggregationInfo/gmd:MD_AggregateInformation/gmd:associationType/gmd:DS_AssociationTypeCode[@codeListValue='largerWorkCitation']/../../gmd:initiativeType/gmd:DS_InitiativeTypeCode[@codeListValue='project']/../../gmd:aggregateDataSetName/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
                </xsl:call-template>
                
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Aggregation DS Name'"/>
                    <xsl:with-param name="cvalue" select="./gmd:aggregationInfo/gmd:MD_AggregateInformation/gmd:aggregateDataSetName/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
                </xsl:call-template>
                
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Aggregation DS Identifier'"/>
                    <xsl:with-param name="cvalue" select="./gmd:aggregationInfo/gmd:MD_AggregateInformation/gmd:aggregateDataSetIdentifier/gmd:MD_Identifier/gmd:authority/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
                </xsl:call-template>
                                
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Aggregation Type'"/>
                    <xsl:with-param name="cvalue" select="./gmd:aggregationInfo/gmd:MD_AggregateInformation/gmd:aggregateDataSetIdentifier/gmd:MD_Identifier/gmd:code/gco:CharacterString"/>
                </xsl:call-template>
                                                                
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Purpose'"/>
                    <xsl:with-param name="cvalue" select="./gmd:purpose/gco:CharacterString"/>
                </xsl:call-template>
                
                <xsl:variable name="keywords">
                    <xsl:for-each select="./gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString">
                        <xsl:choose>
                            <xsl:when test="position()!=last()">
                                <xsl:value-of select='concat(.,", ")'/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select='.'/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                </xsl:variable>
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Keywords'"/>
                    <xsl:with-param name="cvalue" select="$keywords"/>
                </xsl:call-template>
                
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Date Created'"/>
                    <xsl:with-param name="cvalue" select="./gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode[@codeListValue='creation']/../../gmd:date/gco:Date"/>
                </xsl:call-template>
                
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Acknowledgment'"/>
                    <xsl:with-param name="cvalue" select="./gmd:credit/gco:CharacterString"/>
                </xsl:call-template>
                
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'License'"/>
                    <xsl:with-param name="cvalue" select="./gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:useLimitation/gco:CharacterString"/>
                </xsl:call-template>
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Status'"/>
                    <xsl:with-param name="cvalue" select="./gmd:status/gmd:MD_ProgressCode/@codeListValue"/>
                </xsl:call-template>
            </table>
            
            <xsl:apply-templates select="./gmd:citation"/>
            <xsl:apply-templates select="./gmd:extent"/>
            <xsl:apply-templates select="./gmd:extent/gmd:EX_Extent/gmd:temporalElement"/>
            <xsl:apply-templates select="./gmd:pointOfContact"/>
        </div>
    </xsl:template>

    <xsl:template match="gmd:MD_GridSpatialRepresentation">
        <div class="captioneddiv">
            <h3>Spatial Representation</h3>
            <table class="meta">
                <tr>
                    <td class="meta" valign="top">
                        <table class="meta">
                            <xsl:call-template name="tablerow">
                                <xsl:with-param name="cname" select="'Dimensions'"/>
                                <xsl:with-param name="cvalue" select="./gmd:numberOfDimensions"/>
                            </xsl:call-template>
                            <xsl:call-template name="tablerow">
                                <xsl:with-param name="cname" select="'Cell Geometry'"/>
                                <xsl:with-param name="cvalue" select="./gmd:cellGeometry/gmd:MD_CellGeometryCode/@codeListValue"/>
                            </xsl:call-template>
                            
                            <xsl:for-each select="./gmd:axisDimensionProperties">
                                <xsl:variable name="id" select="./gmd:MD_Dimension/@id" />
                                <xsl:variable name="type" select="./gmd:MD_Dimension/gmd:dimensionName/gmd:MD_DimensionNameTypeCode/@codeListValue" />
                                <xsl:variable name="size" select="./gmd:MD_Dimension/gmd:dimensionSize/gco:Integer" />
                                <xsl:variable name="resolution" select="./gmd:MD_Dimension/gmd:resolution/*[1]" />
                                <xsl:variable name="uom" select="./gmd:MD_Dimension/gmd:resolution/*[1]/@uom" />
                                
                                <xsl:if test="$id != ''">
                                    <xsl:call-template name="tablerow">
                                        <xsl:with-param name="cname" select="'ID'"/>
                                        <xsl:with-param name="cvalue" select="$id"/>
                                    </xsl:call-template>
                                    <xsl:call-template name="tablerow">
                                        <xsl:with-param name="cname" select="'Type'"/>
                                        <xsl:with-param name="cvalue" select="$type"/>
                                    </xsl:call-template>
                                    <xsl:call-template name="tablerow">
                                        <xsl:with-param name="cname" select="'Size'"/>
                                        <xsl:with-param name="cvalue" select="$size"/>
                                    </xsl:call-template>
                                    <xsl:call-template name="tablerow">
                                        <xsl:with-param name="cname" select="'Resolution'"/>
                                        <xsl:with-param name="cvalue" select="$resolution"/>
                                    </xsl:call-template>
                                    <xsl:call-template name="tablerow">
                                        <xsl:with-param name="cname" select="'UOM'"/>
                                        <xsl:with-param name="cvalue" select="$uom"/>
                                    </xsl:call-template>
                                </xsl:if>
                            </xsl:for-each>
                            
                        </table>
                    </td>
                </tr>
            </table>
        </div>
    </xsl:template>


    <xsl:template match="gmd:MD_CoverageDescription">
        <div class="captioneddiv">
            <h3>Content Info</h3>
            <table class="meta">
                <tr>
                    <td class="meta" valign="top">
                        <table class="meta">
                            
                            <xsl:if test="gmd:attributeDescription/@gco:nilReason = ''">
                                <xsl:call-template name="tablerow">
                                    <xsl:with-param name="cname" select="'Attribute Description'"/>
                                    <xsl:with-param name="cvalue" select="./gmd:attributeDescription"/>
                                </xsl:call-template>
                            </xsl:if>
                            
                            <xsl:if test="gmd:contentType/@gco:nilReason = ''">
                                <xsl:call-template name="tablerow">
                                    <xsl:with-param name="cname" select="'Content Type'"/>
                                    <xsl:with-param name="cvalue" select="./gmd:contentType"/>
                                </xsl:call-template>
                            </xsl:if>
                            
                            <xsl:for-each select="./gmd:dimension">
                                <xsl:variable name="attributeId" select="./gmd:MD_Band/gmd:sequenceIdentifier/gco:MemberName/gco:aName/gco:CharacterString" />
                                <xsl:variable name="attributeType" select="./gmd:MD_Band/gmd:sequenceIdentifier/gco:MemberName/gco:attributeType/gco:TypeName/gco:aName/gco:CharacterString" />
                                <xsl:variable name="descriptor" select="./gmd:MD_Band/gmd:descriptor/gco:CharacterString" />
                                
                                <tr>
                                    <td class="meta-param">
                                        <xsl:value-of select="$attributeId"/> 
                                        (
                                        <xsl:value-of select="$attributeType"/>)
                                        <xsl:text>: </xsl:text>
                                    </td>
                                    <td class="meta-value">
                                        <xsl:value-of select="$descriptor"/>
                                    </td>
                                </tr>
                            </xsl:for-each>
                            
                        </table>
                    </td>
                </tr>
            </table>
        </div>
    </xsl:template>

    <xsl:template match="gmd:temporalElement">
        <div class="captioneddiv">
            <h3>Time Coverage</h3>
            <table class="meta">
                <tr>
                    <td class="meta" valign="top">
                        <table class="meta">
                            <xsl:call-template name="tablerow">
                                <xsl:with-param name="cname" select="'Time Start'"/>
                                <xsl:with-param name="cvalue" select="./gmd:EX_TemporalExtent/gmd:extent/*[local-name()='TimePeriod']/*[local-name()='beginPosition']"/>
                            </xsl:call-template>
                            <xsl:call-template name="tablerow">
                                <xsl:with-param name="cname" select="'Time End'"/>
                                <xsl:with-param name="cvalue" select="./gmd:EX_TemporalExtent/gmd:extent/*[local-name()='TimePeriod']/*[local-name()='endPosition']"/>
                            </xsl:call-template>
                        </table>
                    </td>
                </tr>
            </table>
        </div>
    </xsl:template>

    <xsl:template match="gmd:contact">
        <xsl:choose>
            <xsl:when test="./@gco:nilReason">
            </xsl:when>
            <xsl:otherwise>
                <div class="captioneddiv">
                    <h3>Metadata author</h3>
                    <table class="meta">
                        <tr>
                            <td class="meta" valign="top">
                                <table class="meta">
                                    <tr></tr>
                                    <xsl:call-template name="tablerow">
                                        <xsl:with-param name="cname" select="'Individual name'"/>
                                        <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:individualName/gco:CharacterString"/>
                                    </xsl:call-template>
                                    <xsl:call-template name="tablerow">
                                        <xsl:with-param name="cname" select="'Organisation name'"/>
                                        <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString"/>
                                    </xsl:call-template>
                                    <xsl:call-template name="tablerow">
                                        <xsl:with-param name="cname" select="'Position'"/>
                                        <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:positionName/gco:CharacterString"/>
                                    </xsl:call-template>
                                    <xsl:call-template name="tablerow">
                                        <xsl:with-param name="cname" select="'Role'"/>
                                        <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue"/>
                                    </xsl:call-template>
                                </table>
                            </td>
                            <td class="meta" valign="top">
                                <table class="meta">
                                    <tr></tr>
                                    <xsl:call-template name="tablerow">
                                        <xsl:with-param name="cname" select="'Voice'"/>
                                        <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:voice/gco:CharacterString"/>
                                    </xsl:call-template>
                                    <xsl:call-template name="tablerow">
                                        <xsl:with-param name="cname" select="'Facsimile'"/>
                                        <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:facsimile/gco:CharacterString"/>
                                    </xsl:call-template>
                                    <xsl:call-template name="tablerow">
                                        <xsl:with-param name="cname" select="'Delivery Point'"/>
                                        <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:deliveryPoint/gco:CharacterString"/>
                                    </xsl:call-template>
                                    <xsl:call-template name="tablerow">
                                        <xsl:with-param name="cname" select="'City'"/>
                                        <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:city/gco:CharacterString"/>
                                    </xsl:call-template>
                                    <xsl:call-template name="tablerow">
                                        <xsl:with-param name="cname" select="'Postal code'"/>
                                        <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:postalCode/gco:CharacterString"/>
                                    </xsl:call-template>
                                    <xsl:call-template name="tablerow">
                                        <xsl:with-param name="cname" select="'Country'"/>
                                        <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:country/gco:CharacterString"/>
                                    </xsl:call-template>
                                    <xsl:call-template name="tablerow">
                                        <xsl:with-param name="cname" select="'Email'"/>
                                        <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString"/>
                                    </xsl:call-template>
                                </table>
                            </td>
                        </tr>
                    </table>
                </div>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="gmd:pointOfContact">
        <div class="captioneddiv">
            <h3>Point of Contact</h3>
            <table class="meta">
                <tr>
                    <td class="meta" valign="top">
                        <table class="meta">
                            <tr></tr>
                            <xsl:call-template name="tablerow">
                                <xsl:with-param name="cname" select="'Individual name'"/>
                                <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:individualName/gco:CharacterString"/>
                            </xsl:call-template>
                            <xsl:call-template name="tablerow">
                                <xsl:with-param name="cname" select="'Organisation name'"/>
                                <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString"/>
                            </xsl:call-template>
                            <xsl:call-template name="tablerow">
                                <xsl:with-param name="cname" select="'Position'"/>
                                <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:positionName/gco:CharacterString"/>
                            </xsl:call-template>
                            <xsl:call-template name="tablerow">
                                <xsl:with-param name="cname" select="'Role'"/>
                                <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue"/>
                            </xsl:call-template>
                        </table>
                    </td>
                    <td class="meta" valign="top">
                        <table class="meta">
                            <tr></tr>
                            <xsl:call-template name="tablerow">
                                <xsl:with-param name="cname" select="'Voice'"/>
                                <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:voice/gco:CharacterString"/>
                            </xsl:call-template>
                            <xsl:call-template name="tablerow">
                                <xsl:with-param name="cname" select="'Facsimile'"/>
                                <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:phone/gmd:CI_Telephone/gmd:facsimile/gco:CharacterString"/>
                            </xsl:call-template>
                            <xsl:call-template name="tablerow">
                                <xsl:with-param name="cname" select="'Delivery Point'"/>
                                <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:deliveryPoint/gco:CharacterString"/>
                            </xsl:call-template>
                            <xsl:call-template name="tablerow">
                                <xsl:with-param name="cname" select="'City'"/>
                                <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:city/gco:CharacterString"/>
                            </xsl:call-template>
                            <xsl:call-template name="tablerow">
                                <xsl:with-param name="cname" select="'Postal code'"/>
                                <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:postalCode/gco:CharacterString"/>
                            </xsl:call-template>
                            <xsl:call-template name="tablerow">
                                <xsl:with-param name="cname" select="'Country'"/>
                                <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:country/gco:CharacterString"/>
                            </xsl:call-template>
                            <xsl:call-template name="tablerow">
                                <xsl:with-param name="cname" select="'Email'"/>
                                <xsl:with-param name="cvalue" select="./gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString"/>
                            </xsl:call-template>
                        </table>
                    </td>
                </tr>
            </table>
        </div>
    </xsl:template>

<!-- 'Citation->Point of Contact' block -->
    <xsl:template match="gmd:citation">
<!--        /gmd:CI_Citation/gmd:citedResponsibleParty/gmd:CI_ResponsibleParty-->
        <div class="captioneddiv">
            <h3>Data Creator - Point of Contact</h3>
            <table class="meta">
                <tr>
                    <td class="meta" valign="top">
                        <table class="meta">
                            <xsl:call-template name="tablerow">
                                <xsl:with-param name="cname" select="'Creator name'"/>
                                <xsl:with-param name="cvalue" select="./gmd:CI_Citation/gmd:citedResponsibleParty/gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode[@codeListValue='originator']/../../gmd:individualName/gco:CharacterString"/>
                            </xsl:call-template>
                            
                            <xsl:call-template name="tablerow">
                                <xsl:with-param name="cname" select="'Creator URL'"/>
                                <xsl:with-param name="cvalue" select="./gmd:CI_Citation/gmd:citedResponsibleParty/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL"/>
                            </xsl:call-template>
                            
                            <xsl:call-template name="tablerow">
                                <xsl:with-param name="cname" select="'Creator E-Mail'"/>
                                <xsl:with-param name="cvalue" select="./gmd:CI_Citation/gmd:citedResponsibleParty/gmd:CI_ResponsibleParty/gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString"/>
                            </xsl:call-template>
                            
                            <xsl:call-template name="tablerow">
                                <xsl:with-param name="cname" select="'Institution'"/>
                                <xsl:with-param name="cvalue" select="./gmd:CI_Citation/gmd:citedResponsibleParty/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString"/>
                            </xsl:call-template>
                        </table>
                    </td>
                </tr>
            </table>
        </div>
    </xsl:template>

<!-- 'Identification->Geographic box' block -->
    <xsl:template match="gmd:extent">
        <xsl:if test="./gmd:EX_Extent/gmd:geographicElement">
            <div class="captioneddiv">
                <h3>Geographic box</h3>
                <br/>
                <table class="meta" width="100%" align="center">
                    <tr></tr>
                    <tr>
                        <td></td>
                        <td class="meta-param" align="center">North bound latitude
                            <br/>
                            <font color="#000000">
                                <xsl:value-of select="./gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal"/>
                            </font>
                        </td>
                        <td></td>
                    </tr>
                    <tr>
                        <td class="meta-param" align="center">West bound longitude
                            <br/>
                            <font color="#000000">
                                <xsl:value-of select="./gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal"/>
                            </font>
                        </td>
                        <td></td>
                        <td class="meta-param" align="center">East bound longitude
                            <br/>
                            <font color="#000000">
                                <xsl:value-of select="./gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal"/>
                            </font>
                        </td>
                    </tr>
                    <tr>
                        <td></td>
                        <td class="meta-param" align="center">South bound latitude
                            <br/>
                            <font color="#000000">
                                <xsl:value-of select="./gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal"/>
                            </font>
                        </td>
                        <td></td>
                    </tr>
                    <xsl:call-template name="tablerow">
                        <xsl:with-param name="cname" select="'Vertical Min'"/>
                        <xsl:with-param name="cvalue" select="./gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:minimumValue/gco:Real"/>
                    </xsl:call-template>
                    <xsl:call-template name="tablerow">
                        <xsl:with-param name="cname" select="'Vertical Max'"/>
                        <xsl:with-param name="cvalue" select="./gmd:EX_Extent/gmd:verticalElement/gmd:EX_VerticalExtent/gmd:maximumValue/gco:Real"/>
                    </xsl:call-template>
                </table>
            </div>
        </xsl:if>
        
    </xsl:template>

<!-- 'Distribution Info' block -->
    <xsl:template match="gmd:MD_Distribution">
        <div class="captioneddiv">
            <h3>Distribution info</h3>
            <table class="meta">
                <tr></tr>
                <xsl:for-each select="gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource">
                    <xsl:choose>
                        <xsl:when test="starts-with(./gmd:protocol/gco:CharacterString,'WWW:DOWNLOAD-') and contains(./gmd:protocol/gco:CharacterString,'http--download') and ./gmd:name/gco:CharacterString">
                            <tr>
                                <td class="meta-param">Download:</td>
                                <td class="meta-value">
                                    <a>
                                        <xsl:attribute name="href">
                                            <xsl:value-of select="gmd:linkage/gmd:URL"/>
                                        </xsl:attribute>
                                        <xsl:value-of select="gmd:name/gco:CharacterString"/>
                                    </a>
                                </td>
                            </tr>
                        </xsl:when>
                        <xsl:when test="starts-with(./gmd:protocol/gco:CharacterString,'ESRI:AIMS-') and contains(./gmd:protocol/gco:CharacterString,'-get-image') and ./gmd:name/gco:CharacterString">
                            <tr>
                                <td class="meta-param">Esri ArcIms:</td>
                                <td class="meta-value">
                                    <a>
                                        <xsl:attribute name="href">
                                            <xsl:value-of select="gmd:linkage/gmd:URL"/>
                                        </xsl:attribute>
                                        <xsl:value-of select="gmd:name/gco:CharacterString"/>
                                    </a>
                                </td>
                            </tr>
                        </xsl:when>
                        <xsl:when test="starts-with(./gmd:protocol/gco:CharacterString,'OGC:WMS-') and contains(./gmd:protocol/gco:CharacterString,'-get-map') and ./gmd:name/gco:CharacterString">
                            <tr>
                                <td class="meta-param">OGC-WMS:</td>
                                <td class="meta-value">
                                    <a>
                                        <xsl:attribute name="href">
                                            <xsl:text>javascript:void(window.open('</xsl:text>
                                            <xsl:value-of select="gmd:linkage/gmd:URL"/>
                                            <xsl:text>'))</xsl:text>
                                        </xsl:attribute>
                                        <xsl:value-of select="gmd:name/gco:CharacterString"/>
                                    </a>
                                </td>
                            </tr>
                        </xsl:when>
                        <xsl:when test="starts-with(./gmd:protocol/gco:CharacterString,'OGC:WMS-') and contains(./gmd:protocol/gco:CharacterString,'-get-capabilities') and ./gmd:name/gco:CharacterString">
                            <tr>
                                <td class="meta-param">OGC-WMS Capabilities:</td>
                                <td class="meta-value">
                                    <a>
                                        <xsl:attribute name="href">
                                            <xsl:value-of select="gmd:linkage/gmd:URL"/>
                                        </xsl:attribute>
                                        <xsl:value-of select="gmd:name/gco:CharacterString"/>
                                    </a>
                                </td>
                            </tr>
                        </xsl:when>
  		    <!--xsl:when test="linkage[text()]">
  			    <link type="url"><xsl:value-of select="linkage[text()]"/></link>
  		    </xsl:when-->
                    </xsl:choose>
                </xsl:for-each>
            </table>
        </div>
    </xsl:template>

<!-- 'Identification->Abstract -->
    <xsl:template match="gmd:abstract">
        <xsl:apply-templates select="./gco:CharacterString/text()"/>
    </xsl:template>
<!-- End Metadata ISO19139 -->


<!-- StartMetadata Dublin Core -->

<!-- 'Identification' block -->
    <xsl:template match="*[local-name()='Record']|*[local-name()='SummaryRecord']|*[local-name()='BriefRecord']">
        <div class="captioneddiv">
            <h3>Identification info</h3>
            <table class="meta">
                <tr></tr>
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Title'"/>
                    <xsl:with-param name="cvalue" select="./dc:title"/>
                </xsl:call-template>
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Date'"/>
                    <xsl:with-param name="cvalue" select="./dc:date"/>
                </xsl:call-template>
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Presentation form'"/>
                    <xsl:with-param name="cvalue" select="./dc:format"/>
                </xsl:call-template>
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Individual name'"/>
                    <xsl:with-param name="cvalue" select="./dc:publisher"/>
                </xsl:call-template>
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Identifier'"/>
                    <xsl:with-param name="cvalue" select="./dc:identifier"/>
                </xsl:call-template>
                <xsl:if test="./dct:abstract">
                    <tr><!-- this "tr" causes problems for new line replacement by "p" -->
                        <td class="meta-param">Abstract:</td>
                        <td class="meta-value">
                            <xsl:apply-templates select="./dct:abstract"/>
                        </td>
                    </tr>
                </xsl:if>
                <xsl:for-each select="./dc:subject">
                    <xsl:call-template name="tablerow">
                        <xsl:with-param name="cname" select="'Keyword'"/>
                        <xsl:with-param name="cvalue" select="."/>
                    </xsl:call-template>
                </xsl:for-each>
            </table>
            <xsl:apply-templates select="./ows:BoundingBox"/>
            <xsl:apply-templates select="./ows:WGS84BoundingBox"/>
            <xsl:for-each select="./dc:URI">
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'URI'"/>
                    <xsl:with-param name="cvalue" select="."/>
                </xsl:call-template>
            </xsl:for-each>
        </div>
    </xsl:template>


    <xsl:template match="dct:abstract">
<!--xsl:value-of select="."/-->
        <xsl:apply-templates select="text()"/>
    </xsl:template>

<!-- 'Identification->Geographic box' block -->
    <xsl:template match="ows:BoundingBox|ows:WGS84BoundingBox">
        <div class="captioneddiv">
            <h3>Geographic box</h3>
            <table class="meta">
                <tr></tr>
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Lower corner'"/>
                    <xsl:with-param name="cvalue" select="./ows:LowerCorner"/>
                </xsl:call-template>
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Upper corner'"/>
                    <xsl:with-param name="cvalue" select="./ows:UpperCorner"/>
                </xsl:call-template>
            </table>
        </div>
    </xsl:template>
<!-- End Metadata Dublin Core -->

<!-- Start Utills -->
    <xsl:template  match="text()">
        <xsl:call-template name="to-para">
            <xsl:with-param name="from" select="'&#10;&#10;'"/>
            <xsl:with-param name="string" select="."/>
        </xsl:call-template>
    </xsl:template> 

<!-- replace all occurences of the character(s) `from'
                   by  <p/> in the string `string'.-->
    <xsl:template name="to-para" >
        <xsl:param name="string"/>
        <xsl:param name="from"/>
        <xsl:choose>
            <xsl:when test="contains($string,$from)">
                <xsl:value-of select="substring-before($string,$from)"/>
      <!-- output a <p/> tag instead of `from' -->
                <p/>
                <xsl:call-template name="to-para">
                    <xsl:with-param name="string" select="substring-after($string,$from)"/>
                    <xsl:with-param name="from" select="$from"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$string"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template name="tablerow" >
        <xsl:param name="cname"/>
        <xsl:param name="cvalue"/>
        <xsl:choose>
            <xsl:when test="string($cvalue)">
                <tr>
                    <td class="meta-param">
                        <xsl:value-of select="$cname"/>
                        <xsl:text>: </xsl:text>
                    </td>
                    <td class="meta-value">
                        <xsl:value-of select="$cvalue"/>
                    </td>
                </tr>
            </xsl:when>
            <xsl:otherwise>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template name="tableHtmlRow" >
        <xsl:param name="cname"/>
        <xsl:param name="cvalue"/>
        <xsl:choose>
            <xsl:when test="string($cvalue)">
                <tr>
                    <td class="meta-param">
                        <xsl:value-of select="$cname"/>
                        <xsl:text>: </xsl:text>
                    </td>
                    <td class="meta-value">
                        <a>
                            <xsl:attribute name="href">
                                <xsl:value-of select="$cvalue"/>
                            </xsl:attribute>
                            <xsl:value-of select="$cvalue"/>
                        </a>
                    </td>
                </tr>
            </xsl:when>
            <xsl:otherwise>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
<!-- End Utills -->

</xsl:stylesheet>

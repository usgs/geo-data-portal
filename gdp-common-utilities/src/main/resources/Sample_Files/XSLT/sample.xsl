<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:ns="http://www.opengis.net/wps/1.0.0" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:ns1="http://www.opengis.net/ows/1.1"
    xmlns:wps="http://www.opengis.net/wps/1.0.0" 
    xmlns:ows="http://www.opengis.net/ows/1.1">
        
    <xsl:output method="text" />
    <xsl:template match="/">
Process Information:
    <xsl:apply-templates select="/ns:ExecuteResponse/ns:Process"/>
    <xsl:apply-templates select="/ns:ExecuteResponse/ns:Status"/>

    Inputs:
    <xsl:apply-templates select="/ns:ExecuteResponse/wps:DataInputs"/>
    Outputs:
    <xsl:apply-templates select="/ns:ExecuteResponse/ns:ProcessOutputs"/>
    </xsl:template>

    <xsl:template match="/ns:ExecuteResponse/ns:Process">
  Process Title:      <xsl:value-of select="./ns1:Title"/>
  Process Version:    <xsl:value-of select="./@ns:processVersion"/>
  Process Full Name:  <xsl:value-of select="./ns1:Identifier"/>
    </xsl:template>
    
    <xsl:template match="/ns:ExecuteResponse/ns:Status">
  Process Created:    <xsl:value-of select="@creationTime"/>
  Process Status:     <xsl:value-of select="./ns:ProcessSucceeded"/>
    </xsl:template>        
    
    <xsl:template match="/ns:ExecuteResponse/wps:DataInputs">
        <xsl:for-each select="./wps:Input">
        <xsl:variable name="nodeTest"> 
          <xsl:value-of select="./wps:Data"/>         
        </xsl:variable>
        Input: <xsl:value-of select="./ows:Identifier"/>
        Data: <xsl:choose>
	    <xsl:when test="$nodeTest != ''">
		<xsl:for-each select="./wps:Data/wps:LiteralData"><xsl:value-of select="."/></xsl:for-each>
	    </xsl:when>
	    <xsl:otherwise>
	    	<xsl:apply-templates select="wps:Reference" />
	    </xsl:otherwise>
	</xsl:choose>
        </xsl:for-each>
    </xsl:template>    
    
    <xsl:template match="/ns:ExecuteResponse/ns:ProcessOutputs">
        <xsl:for-each select="./ns:Output">
        Output Name: <xsl:value-of select="./ns1:Identifier"/>
            Data: <xsl:apply-templates select="/ns:ExecuteResponse/ns:ProcessOutputs/ns:Output/ns:Reference"/>
        </xsl:for-each>
    </xsl:template>    
    
    <xsl:template match="/ns:ExecuteResponse/ns:ProcessOutputs/ns:Output/ns:Reference">
            Encoding:  <xsl:value-of select="./@encoding"/>
            Mime Type: <xsl:value-of select="./@mimeType"/>
            HREF:      <xsl:value-of select="./@href"/>
    </xsl:template>
    
    <xsl:template match="wps:Reference">    
	 &lt; Complex Value Reference (not shown) &gt;
    </xsl:template>
    
</xsl:stylesheet>

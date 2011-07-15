<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    <xsl:output method="html"/>
    <xsl:template match="/">
        <div>
            <div style="text-align: center; width: 550px; margin-left: auto; margin-right: auto;">
                <h1><u>Property Settings For GDP</u></h1>
                
                <div style="text-align: left; margin-left: auto; margin-right: auto;">
                    <center><h3>Map Layer Configuration</h3></center>
                    <div>
                        To add a map layer: layer must be available in EPSG:4326;
                        add WMS layers as property map.layers.wms.layer_id; add
                        tile cache layers as property map.layers.tile.layer_id.
                        The format of each property's value should be:
                    </div>
                    <br />
                    <div style="text-align: center;">
                        Layer Name,layer-url?paramKey1=paramVal1&amp;paramKey2=paramVal2...
                    </div>
                    <br />
                    <div>
                        Make sure to include the WMS/tile cache layer name as a
                        parameter: layer-url?layers=bluemarble. If the layer is
                        to be overlaid on top of other layers, add the parameter
                        isBaseLayer=false. Set map.default_layer to the layer_id
                        you wish to be the default.
                    </div>
                </div>
                
                <br /><br />
            </div>
            <center>
                <table>
                    <thead>
                        <tr>
                            <th>Property</th>
                            <th></th>
                            <th>Value</th>
                            <th>Delete?</th>
                        </tr>
                    </thead>
                
                    <xsl:apply-templates/>
                </table>
            </center>
        </div>
    </xsl:template>

    <xsl:template match="properties">
        <xsl:for-each select="entry">
            <xsl:variable name="key" select="@key" />
            <xsl:variable name="propertyvalue" select="." />
            <xsl:sort select="@key" />
            <tr>
                <td>
                    <input type="text" value="{$key}" size="30" class="key" />
                </td>
                <td> = </td>
                <td>
                    <input type="text" value="{$propertyvalue}" size="80" class="value" />
                </td>
                <td>
                    <center>
                        <input type="checkbox" checked="" class="delete"/>
                    </center>
                </td>
            
            </tr>
        </xsl:for-each>
        <tr>
            <td colspan="4">
                <button id="addrow_button" style="margin-right: 20px;">Add Row</button>
                <button id="submit_button" style="margin-right: 20px;">Submit</button>
            </td>
        </tr>
    
    </xsl:template>

</xsl:stylesheet>

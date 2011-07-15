<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    <xsl:output method="html"/>
    <xsl:template match="/">
        <div>
            <div style="text-align: center; width: 550px; margin-left: auto; margin-right: auto;">
                <h1><u>Blacklist Management For GDP</u></h1>
                <div style="text-align: left; margin-left: auto; margin-right: auto;">
            
                </div>
                <br /><br />
            </div>
            <center>
                <table>
                    <thead>
                        <tr>
                            <th>IP Address</th>
                            <th></th>
                            <th>Date Entered</th>
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
                    <input type="text" value="{$key}" size="30" class="key" readonly="readonly" />
                </td>
                <td> = </td>
                <td>
                    <input type="text" value="{$propertyvalue}" size="80" class="value" readonly="readonly" />
                </td>
                <td>
                    <center>
                        <input type="checkbox" checked="" class="delete" />
                    </center>
                </td>
            
            </tr>
        </xsl:for-each>
        <tr>
            <td colspan="4">
                <button id="submit_button" style="margin-right: 20px;">Submit</button>
            </td>
        </tr>
    
    </xsl:template>

</xsl:stylesheet>

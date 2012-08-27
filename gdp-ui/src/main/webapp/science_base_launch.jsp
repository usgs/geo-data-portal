<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<!--
    What we currently see coming from ScienceBase
    https://my-beta.usgs.gov/catalog/gdp/callGdp?
    item_id=4f4e4a49e4b07f02db623cbe&
    owsurl=http%3A%2F%2Fmy-beta.usgs.gov%2FcatalogMaps%2Fmapping%2Fows%2F4f4e4a49e4b07f02db623cbe&
    wmsurl=http%3A%2F%2Fmy-beta.usgs.gov%2FcatalogMaps%2Fmapping%2Fows%2F4f4e4a49e4b07f02db623cbe&
    wfsurl=http%3A%2F%2Fmy-beta.usgs.gov%2FcatalogMaps%2Fmapping%2Fows%2F4f4e4a49e4b07f02db623cbe&
    wcsurl=
-->
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <form action="/GDP_WEB/index.jsp" method="POST">
            wfsurl: <input type="text" name="wfsurl" size="90" value="https://my-beta.usgs.gov/catalogMaps/mapping/ows/4f4e4a49e4b07f02db623cbe" /><br />
            wmsurl: <input type="text" name="wmsurl" size="90" value="https://my-beta.usgs.gov/catalogMaps/mapping/ows/4f4e4a49e4b07f02db623cbe" /><br />
            owsurl: <input type="text" name="owsurl" size="90" value="https://my-beta.usgs.gov/catalogMaps/mapping/ows/4f4e4a49e4b07f02db623cbe" /><br />
            item_id: <input type="text" name="item_id" size="90" value="4f4e4a49e4b07f02db623cbe" /><br />
            redirect_url: <input type="text" name="redirect_url" size="90" value="https://beta.sciencebase.gov/catalog/gdp/landing/4f4e4a49e4b07f02db623cbe" /><br />
            
            <!-- It doesn't look like these are being used (yet) -->
            coverage_wms: <input type="text" name="coverage_wms" size="90" value="https://my-beta.usgs.gov/catalogMaps/mapping/ows/4f4e4a49e4b07f02db623cbe" /><br />
            coverage_opendap: <input type="text" name="coverage_opendap" size="90" value="" /><br />
            coverage_wcs: <input type="text" name="coverage_wcs" size="90" value="http://my-beta.usgs.gov/catalogMaps/mapping/ows/4f4e4799e4b07f02db48f9dd" /><br />
            caller: <input type="text" name="caller" size="90" value="sciencebase" /><br />

            
            <input type="submit" name="submit" value="Go!">
        </form>
    </body>
</html>

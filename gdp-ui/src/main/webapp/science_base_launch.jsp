<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <form action="/GDP_WEB/index.jsp" method="POST">
            feature_wfs: <input type="text" name="feature_wfs" size="90" value="https://my-beta.usgs.gov/catalogMaps/mapping/ows/4f4e4a49e4b07f02db623cbe" /><br />
            feature_wms: <input type="text" name="feature_wms" size="90" value="https://my-beta.usgs.gov/catalogMaps/mapping/ows/4f4e4a49e4b07f02db623cbe" /><br />
            ows: <input type="text" name="feature_ows" size="90" value="https://my-beta.usgs.gov/catalogMaps/mapping/ows/4f4e4a49e4b07f02db623cbe" /><br />
            item_id: <input type="text" name="item_id" size="90" value="4f4e4a49e4b07f02db623cbe" /><br />
            
            <!-- It doesn't look like these are being used (yet) -->
            coverage_wms: <input type="text" name="coverage_wms" size="90" value="https://my-beta.usgs.gov/catalogMaps/mapping/ows/4f4e4a49e4b07f02db623cbe" /><br />
            coverage_opendap: <input type="text" name="coverage_opendap" size="90" value="" /><br />
            coverage_wcs: <input type="text" name="coverage_wcs" size="90" value="http://my-beta.usgs.gov/catalogMaps/mapping/ows/4f4e4783e4b07f02db48399e" /><br />

            redirect_url: <input type="text" name="redirect_url" size="90" value="https://beta.sciencebase.gov/catalog/gdp/landing/4f4e4a49e4b07f02db623cbe" /><br />

            caller: <input type="text" name="caller" size="90" value="sciencebase" /><br />
            
            <input type="submit" name="submit" value="Go!">
        </form>
    </body>
</html>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <form action="/GDP_WEB/index.jsp" method="POST">
            <input type="hidden" name="feature_wfs" value="http://my-beta.usgs.gov/catalogMaps/mapping/ows/502a77c2e4b01c6d34a57e81" />
            <input type="hidden" name="feature_wms" value="http://my-beta.usgs.gov/catalogMaps/mapping/ows/502a77c2e4b01c6d34a57e81" />
            <input type="hidden" name="coverage_wms" value="http://my-beta.usgs.gov/catalogMaps/mapping/ows/502a77c2e4b01c6d34a57e81" />
            <input type="hidden" name="coverage_opendap" value="http://my-beta.usgs.gov/catalogMaps/mapping/ows/502a77c2e4b01c6d34a57e81" />
            <input type="hidden" name="coverage_wcs" value="http://my-beta.usgs.gov/catalogMaps/mapping/ows/502a77c2e4b01c6d34a57e81" />
            <input type="hidden" name="item_id" value="http://my-beta.usgs.gov/catalogMaps/mapping/ows/502a77c2e4b01c6d34a57e81" />
            <input type="hidden" name="redirect_url" value="http://my-beta.usgs.gov/catalogMaps/mapping/ows/502a77c2e4b01c6d34a57e81" />
            <input type="hidden" name="caller" value="sciencebase" />
            <input type="submit" name="submit" value="Go!">
        </form>
    </body>
</html>

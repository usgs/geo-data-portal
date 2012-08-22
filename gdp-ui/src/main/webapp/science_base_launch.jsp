<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <form action="/GDP_WEB/index.jsp" method="POST">
            
            <input type="hidden" name="feature_wfs" value="http://my-beta.usgs.gov/catalogMaps/mapping/ows/4f4e4a49e4b07f02db623cbe" />
            <input type="hidden" name="feature_wms" value="https://my-beta.usgs.gov/catalogMaps/mapping/ows/4f4e4a49e4b07f02db623cbe" />
            
            <!-- feature_ows is not currently being used but can be to set feature_wfs and feature_wms --> 
            <input type="hidden" name="feature_ows" value="https://my-beta.usgs.gov/catalogMaps/mapping/ows/4f4e4a49e4b07f02db623cbe" />
            
            <input type="hidden" name="coverage_wms" value="https://my-beta.usgs.gov/catalogMaps/mapping/ows/4f4e4a49e4b07f02db623cbe" />
            <input type="hidden" name="coverage_opendap" value="https://my-beta.usgs.gov/catalogMaps/mapping/ows/4f4e4a49e4b07f02db623cbe" />
            <input type="hidden" name="coverage_wcs" value="https://my-beta.usgs.gov/catalogMaps/mapping/ows/4f4e4a49e4b07f02db623cbe" />
            
            <input type="hidden" name="item_id" value="https://my-beta.usgs.gov/catalogMaps/mapping/ows/4f4e4a49e4b07f02db623cbe" />
            
            <input type="hidden" name="redirect_url" value="https://my-beta.usgs.gov/catalogMaps/mapping/ows/4f4e4a49e4b07f02db623cbe" />
            
            <input type="hidden" name="caller" value="sciencebase" />
            
            <input type="submit" name="submit" value="Go!">
        </form>
    </body>
</html>

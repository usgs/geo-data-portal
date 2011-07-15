<% response.setHeader("Pragma", "no-cache");%>
<% response.setHeader("Cache-Control", "no-store");%>
<% response.setDateHeader("Expires", -1);%> 
<!DOCTYPE html>
<html>
    <head>
        <title>Geo Data Portal</title>
        <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
        <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
        <meta name="title" content="Geo Data Portal">
        <meta name="description" content="Geo Data Portal">
        <meta name="author" content="Ivan Suftin, Tom Kunicki, Ryan Zoerb, Jordan Walker">
        <meta name="keywords" content="Department of the Interior,hazards, hazard,earth, USGS,U.S. Geological Survey, water, earthquakes, volcanoes, volcanos,
              tsunami, tsunamis, flood, floods, wildfire, wildfires, natural hazards, environment, science, jobs, USGS Library, Ask USGS, maps, map">
        <meta name="date" content="20101228">
        <meta name="revised date" content="20101228">
        <meta name="reviewed date" content="20101228">
        <meta name="language" content="EN">
        <meta name="expiration date" content="Never">

        <link rel="shortcut icon" href="favicon.ico" type="image/x-icon">

        <link rel="stylesheet" href="css/usgs/common.css" type="text/css" />
        <link rel="stylesheet" href="css/usgs/usgs_style_main.css" type="text/css" />
        <link rel="stylesheet" href="css/colorbox/colorbox.css" type="text/css" />
        <link rel="stylesheet" href="css/fileuploader/fileuploader.css" type="text/css" />
        <link rel="stylesheet" href="css/jquery-ui/redmond/jquery-ui-1.8.12.custom.css" type="text/css" />
        <link rel="stylesheet" href="css/jquery-ui-override.css" type="text/css" />
        <link rel="stylesheet" href="css/jgrowl/jquery.jgrowl.css" type="text/css" />
        <link rel="stylesheet" href="css/jgrowl-override.css" type="text/css" />
        <link rel="stylesheet" href="css/tiptip/tipTip.css" type="text/css" />
        <link rel="stylesheet" href="css/excat/cswclient.css" type="text/css" />
        <link rel="stylesheet" href="css/common.css" type="text/css" />
        <link rel="stylesheet" href="css/area_of_interest.css" type="text/css" />
        <link rel="stylesheet" href="css/dataset.css" type="text/css" />
        <link rel="stylesheet" href="css/submit.css" type="text/css" />

        <script src="js/cookie/cookie.js" type="text/javascript"></script>
        <script src="js/log4javascript/log4javascript.js" type="text/javascript"></script>
        <script src="js/jquery/jquery-1.6.1.js" type="text/javascript"></script>
        <script src="js/xslt/jquery.xslt.js" type="text/javascript"></script>
        <script src="js/xmlns/jquery.xmlns.js" type="text/javascript"></script>
        <script src="js/objects/algorithm.js" type="text/javascript"></script>
        <script src="js/constants.js" type="text/javascript"></script>
        <script src="openlayers/OpenLayers.js" type="text/javascript"></script>
        <script src="js/jquery-ui/jquery-ui-1.8.12.custom.min.js" type="text/javascript"></script>
        <script src="js/jgrowl/jquery.jgrowl_google.js" type="text/javascript"></script>
        <script src="js/colorbox/jquery.colorbox-min.js" type="text/javascript"></script>
        <script src="js/parseUri/parseUri.js" type="text/javascript"></script>
        <script src="js/parsexml/jquery.xmldom-1.0.min.js" type="text/javascript"></script>
        <script src="js/fileuploader/fileuploader.js" type="text/javascript"></script>
        <script src="js/download/download.jQuery.js" type="text/javascript"></script>
        <script src="js/wps.js" type="text/javascript"></script>
        <script src="js/wfs.js" type="text/javascript"></script>
        <script src="js/root.js" type="text/javascript"></script>
        <script src="js/area_of_interest.js" type="text/javascript"></script>
        <script src="js/dataset.js" type="text/javascript"></script>
        <script src="js/map.js" type="text/javascript"></script>
        <script src="js/tiptip/jquery.tipTip.js" type="text/javascript"></script>
        <script src="js/excat/scripts/sarissa.js" type="text/javascript"></script>
        <script src="js/excat/scripts/sarissa_ieemu_xpath.js" type="text/javascript"></script>
        <script src="js/excat/scripts/cswclient.js" type="text/javascript"></script>
    </head>

    <body>
        <div id="overlay"><div id="overlay-content"></div></div>
        <div id="page-container-div">
            <jsp:include page="template/header.jsp" />
            <a name="map">
                <div id="map-div"></div>
            </a>

            <div id="bottom-div">
                <div id="steps-top">
                    <input id="prev-button" type="button" class="button nav-button" />
                    <input id="next-button" type="button" class="button nav-button" />

                    <div id="steps-header" class="center"></div>
                    <img id="throbber" class="hidden" alt="loading" src="images/ajax-loader.gif" />
                </div>
                <div id="steps-content">
                    <jsp:include page="jsp/area_of_interest.jsp" />
                    <jsp:include page="jsp/dataset.jsp" />
                </div>
            </div>
            <jsp:include page="template/footer.jsp">
                <jsp:param name="footer-class" value="x-hidden"/>
                <jsp:param name="serviceDir" value="service"/>
            </jsp:include>
            <jsp:include page="jsp/feedback/feedback.jsp">
                <jsp:param name="securityimageDir" value="securityimage"/>
                <jsp:param name="imageDir" value="images"/>
            </jsp:include>
        </div>
    </body>
</html>

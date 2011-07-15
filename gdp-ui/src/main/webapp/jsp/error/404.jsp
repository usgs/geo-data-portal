<% response.setHeader("Pragma","no-cache");%>
<% response.setHeader("Cache-Control","no-store");%>
<% response.setDateHeader("Expires",-1);%> 
<html>
    <head>
        <title>The Geo Data Portal - Page Not Found</title>
        <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
        <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
        <link rel="shortcut icon" href="favicon.ico" type="image/x-icon">
        <link rel="stylesheet" href="css/usgs/common.css" type="text/css" />
        <link rel="stylesheet" href="css/usgs/usgs_style_main.css" type="text/css" />
        <link rel="stylesheet" href="css/common.css" type="text/css" />
        
        <script src="js/log4javascript/log4javascript.js" type="text/javascript"></script>
        <script src="js/jquery/jquery-1.6.1.js" type="text/javascript"></script>
        <script src="js/constants.js" type="text/javascript"></script>
    </head>
    <body>
        <jsp:include page="../../template/header.jsp" />
        <div id="error_header_div"><h3>Page Not Found</h3></div>
        <div id="error_description">
            We are sorry but the page you are trying to access does not exist on the server.
            <br />
            You may try going back to the <a href=".">front page</a>.
        </div>
        <br />
        <jsp:include page="../../template/footer.jsp" />
    </body>
</html>
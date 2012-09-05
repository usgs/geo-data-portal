<% response.setHeader("Pragma", "no-cache");%>
<% response.setHeader("Cache-Control", "no-store");%>
<% response.setDateHeader("Expires", -1);%> 
<%-- 
    Document   : config
    Created on : Feb 3, 2011, 9:55:40 AM
    Author     : isuftin
--%>
<html>
    <head>
        <title>Blacklist Settings For GDP</title>
        <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
        <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
        <script src="js/log4javascript/log4javascript.js" type="text/javascript"></script>
        <jsp:include page="../js/jquery/jquery.jsp">
            <jsp:param name="debug-qualifier" value="false"/>
        </jsp:include>
        <script src="js/xmlns/jquery.xmlns.js" type="text/javascript"></script>
        <script src="js/constants.js" type="text/javascript"></script>
        <script src="js/xslt/jquery.xslt.js" type="text/javascript"></script>
        <script src="js/config/blacklist_config.js" type="text/javascript"></script>
    </head>
    <body>
        <div id="webform"></div>
    </body>
</html>
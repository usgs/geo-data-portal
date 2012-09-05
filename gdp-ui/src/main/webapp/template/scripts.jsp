<%-- 
    Document   : scripts
    Created on : Jul 21, 2011, 1:21:27 PM
    Author     : Ivan Suftin <isuftin@usgs.gov>
--%>

<%@page import="java.util.Enumeration"%>
<script type="text/javascript" src="js/cookie/cookie.js"></script>

<jsp:include page="../js/log4javascript/log4javascript.jsp">
    <jsp:param name="relPath" value=""/>
</jsp:include>

<jsp:include page="../js/jquery/jquery.jsp">
    <jsp:param name="debug-qualifier" value="false"/>
</jsp:include>

<script type="text/javascript" src="js/xslt/jquery.xslt.js"></script>
<script type="text/javascript" src="js/xmlns/jquery.xmlns.js"></script>
<script type="text/javascript" src="js/objects/algorithm.js"></script>
<script type="text/javascript" src="js/constants.js"></script>
<jsp:include page="../js/openlayers/openlayers.jsp">
    <jsp:param name="debug-qualifier" value="false"/>
    <jsp:param name="include-deprecated" value="true"/>
</jsp:include>
<script type="text/javascript" src="js/jquery-ui/jquery-ui-1.8.23.custom.min.js"></script>
<script type="text/javascript" src="js/jgrowl/jquery.jgrowl_compressed.js"></script> <%-- http://plugins.jquery.com/project/jGrowl --%>
<script type="text/javascript" src="js/colorbox/jquery.colorbox-min.js"></script>
<script type="text/javascript" src="js/parseUri/parseUri.js"></script>
<script type="text/javascript" src="js/parsexml/jquery.xmldom-1.0.min.js"></script>
<script type="text/javascript" src="js/fileuploader/fileuploader.js"></script>
<script type="text/javascript" src="js/download/download.jQuery.js"></script>
<script type="text/javascript" src="js/jquery-url-parser/jquery.url.js"></script>
<script type="text/javascript" src="js/wps.js"></script>
<script type="text/javascript" src="js/wfs.js"></script>
<script type="text/javascript" src="js/root.js"></script>
<script type="text/javascript" src="js/sciencebase.js"></script>
<script type="text/javascript" src="js/area_of_interest.js"></script>
<script type="text/javascript" src="js/dataset.js"></script>
<script type="text/javascript" src="js/map.js"></script>
<script type="text/javascript" src="js/tiptip/jquery.tipTip.js"></script>
<script type="text/javascript" src="js/excat/scripts/sarissa.js"></script>
<script type="text/javascript" src="js/excat/scripts/sarissa_ieemu_xpath.js"></script>
<script type="text/javascript" src="js/excat/scripts/cswclient.js"></script>
<script type="text/javascript">
    var incomingParams = {};
    <%
        Enumeration<String> paramNames = (Enumeration<String>) request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String key = paramNames.nextElement();
            String value = request.getParameter(key);
    %>
        incomingParams['<%=key%>'] = '<%=value%>'
    <%
        }
    %>
        
    <%-- Google Analytics --%> 
    <%-- http://internal.cida.usgs.gov/jira/browse/GDP-500 --%>
     var _gaq = _gaq || [];
     _gaq.push(['_setAccount', 'UA-34377683-1']);
     _gaq.push(['_gat._anonymizeIp']);
     _gaq.push(['_trackPageview']);

     (function() {
         var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
         ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
         var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
     })();

</script>
<%@page import="gov.usgs.cida.gdp.utilities.JNDISingleton"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="gov.usgs.cida.config.DynamicReadOnlyProperties"%>

<%-- Google Analytics --%> 
<%-- http://internal.cida.usgs.gov/jira/browse/GDP-500 --%>
<script type="text/javascript">

    var _gaq = _gaq || [];
    _gaq.push(['_setAccount', 'UA-34377468-1']);
    _gaq.push(['_gat._anonymizeIp']);
    _gaq.push(['_trackPageview']);

    (function() {
        var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
        ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
        var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
    })();

</script>

<%-- Base EXT modules & Extensions --%>
<script type="text/javascript" src="js/ext/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="js/ext/ext-all.js"></script>
<script type="text/javascript" src='${param["ComponentDir"]}/extension/notify.js'></script>

<%-- Other JavaScript modules (Mapping, logging and plotting) --%>
<jsp:include page="../js/log4javascript/log4javascript.jsp">
	<jsp:param name="relPath" value="" />
</jsp:include>
<jsp:include page="../js/openlayers/openlayers.jsp">
	<jsp:param name="relPath" value="" />
</jsp:include>
<jsp:include page="../js/geoext/geoext.jsp">
	<jsp:param name="relPath" value="" />
</jsp:include>
<!--<script type="text/javascript" src="js/geoext/GeoExt.js"></script>-->
<script type="text/javascript" src="js/dygraph/dygraph-combined.js"></script>
<script type="text/javascript" src="js/dygraph/dygraph-extra.js"></script>

<%-- Extended Openlayers/GeoExt readers/writers --%>
<script type="text/javascript" src='${param["ComponentDir"]}/CSW/Format/v2_0_2.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/SOS/Format/SOSGetObservation.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/CSW/CSWGetRecordsStore.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/CSW/CSWGetRecordsReader.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/SOS/SOSGetObservationStore.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/SOS/SOSGetObservationReader.js'></script>

<%-- Custom Application Modules Here --%>
<script type="text/javascript" src='${param["ComponentDir"]}/LayerController.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/panels/DatasetConfigPanel.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/panels/Plotter.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/panels/BaseMap.js'></script>

<%-- Root Module --%>
<script type="text/javascript" src='${param["UIScriptFile"]}'></script>

<%-- Read JNDI config from context.xml --%>
<% DynamicReadOnlyProperties props = JNDISingleton.getInstance();%>

<script type="text/javascript">
    GDP.LOG4JS_PATTERN_LAYOUT = '<%= props.getProperty("LOG4JS_PATTERN_LAYOUT", "%rms - %-5p - %m%n")%>';
    GDP.PROXY_PREFIX = '<%= props.getProperty("derivative/PROXY_PREFIX", "proxy/")%>';
    GDP.DEFAULT_LEGEND_NAME = '<%= props.getProperty("derivative/DEFAULT_LEGEND_NAME", "boxfill/occam")%>';
    GDP.CSW_QUERY_CONSTRAINT_FILTER_VALUE = '<%= props.getProperty("derivative/CSW_QUERY_CONSTRAINT_FILTER_VALUE",
            "a0a3c56c-2be5-4d45-9924-72b13e348919")%>';
                GDP.FOI_GETCAPS_URL = GDP.PROXY_PREFIX + '<%= props.getProperty("derivative/FOI_GETCAPS_URL",
            "http://cida-wiwsc-gdp2qa.er.usgs.gov:8082/geoserver/derivative/wms?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities")%>';
    
                Ext.override(Ext.data.Connection, { timeout: 60000 });
    
                // http://ejohn.org/blog/fast-javascript-maxmin/
                Array.max = function( array , maxVal ){
                    var arrMax = Math.max.apply( Math, array );
                    if (arguments.length == 1) {
                        return arrMax;
                    }
                    else {
                        return (arrMax > maxVal) ? arrMax : maxVal;
                    }
                };
                Array.min = function( array , minVal ){
                    var arrMin = Math.min.apply( Math, array );
                    if (arguments.length == 1) {
                        return arrMin;
                    }
                    else {
                        return (arrMin < minVal) ? arrMin : minVal;
                    }
                };
                Array.mean = function( array ) {
                    if (array.length == 0) return NaN;
                    var total = 0;
                    Ext.each(array, function(item) {
                        total += item; 
                    });
                    return total / array.length;
                }
    
                // http://jibbering.com/faq/#parseDate
                Date.parseISO8601 = function(dateStringInRange){
                    var isoExp = /^\s*(\d{4})-(\d\d)-(\d\d)\s*$/,
                    date = new Date(NaN), month,
                    parts = isoExp.exec(dateStringInRange);

                    if(parts) {
                        month = +parts[2];
                        date.setFullYear(parts[1], month - 1, parts[3]);
                        if(month != date.getMonth() + 1) {
                            date.setTime(NaN);
                        }
                    }
                    return date;
                };

    
                Ext.BLANK_IMAGE_URL = 'images/s.gif';

</script>


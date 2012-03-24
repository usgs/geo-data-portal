<%@page import="gov.usgs.cida.config.DynamicReadOnlyProperties"%>
<%@page language="java" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>

<html>
    <head>

        <jsp:include page="template/USGSHead.jsp">
            <jsp:param name="shortName" value="GDP PRMS Portal" />
            <jsp:param name="title" value="GDP PRMS Portal" />
            <jsp:param name="description" value="GDP PRMS Portal" />
            <jsp:param name="author" value="Ivan Suftin"/>
            <jsp:param name="publisher" value="USGS - U.S. Geological Survey, Water Resources; CIDA - Center for Integrated Data Analytics" />
            <jsp:param name="keywords" value="USGS, U.S. Geological Survey, water, earth science, hydrology, hydrologic, data, streamflow, stream, river, lake, flood, drought, quality, basin, watershed, environment, ground water, groundwater" />
            <jsp:param name="revisedDate" value="20120221" />
            <jsp:param name="nextReview" value="20130221" />
            <jsp:param name="expires" value="never" />
        </jsp:include>

        <script type="text/javascript">
            var CONFIG = {};
            
            <% 
                DynamicReadOnlyProperties props = new DynamicReadOnlyProperties();
                props.addJNDIContexts(new String[0]);
                boolean development = Boolean.parseBoolean(props.getProperty("watersmart.development"));
            %>
            
            CONFIG.LOG4JS_PATTERN_LAYOUT = '<%= props.getProperty("gdp.prms.ui.frontend.log4js.pattern.layout","%rms - %-5p - %m%n") %>';
            CONFIG.LOG4JS_LOG_THRESHOLD = '<%= props.getProperty("gdp.prms.ui.frontend.log4js.threshold", "info") %>';
            CONFIG.GEOSERVER_URL = '<%= props.getProperty("gdp.prms.ui.geoserver.url", "http://igsarm-cida-javadev1.er.usgs.gov:8081/geoserver") %>';
            CONFIG.WPS_PROCESS_URL = '<%= props.getProperty("gdp.prms.ui.wps.process.url", "http://igsarm-cida-javadev1.er.usgs.gov:8080/gdp-process-wps") %>';
            CONFIG.WPS_UTILITY_URL = '<%= props.getProperty("gdp.prms.ui.wps.utility.url", "http://igsarm-cida-javadev1.er.usgs.gov:8080/gdp-utility-wps") %>';
            CONFIG.DEVELOPMENT = <%= development %>;

            /**
             * Takes an element, checks the array for that element
             * and if found, returns the index of that element. 
             * Otherwise, returns -1
             */
            Array.prototype.contains = function(element) {
                for (var i = 0;i < this.length;i++) {
                    if (this[i] == element) {
                        return i;
                    }
                }
                return -1;
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
        </script>
        
        <jsp:include page="scripts.jsp" >
            <jsp:param name="debug-qualifier" value="<%= development %>" />
        </jsp:include>
        
    </head>
    <body>
        <jsp:include page="template/USGSHeader.jsp">
            <jsp:param name="header-class" value="x-hidden"/>
            <jsp:param name="site-title" value="GDP PRMS Portal"/>
        </jsp:include>
        
        <jsp:include page="template/USGSFooter.jsp">
            <jsp:param name="footer-class" value="x-hidden"/>
            <jsp:param name="site-url" value="http://cida.usgs.gov/watersmart"/>
            <jsp:param name="contact-info" value="dblodgett@usgs.gov"/>
        </jsp:include>
    </body>
</html>

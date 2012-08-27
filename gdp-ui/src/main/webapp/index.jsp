<%@ page contentType="text/html;charset=UTF-8"  %>
<% response.setHeader("Pragma", "no-cache");%>
<% response.setHeader("Cache-Control", "no-store");%>
<% response.setDateHeader("Expires", -1);%> 

<%--
    TODO:
    -- WebKit browsers are showing errors for AJAX requests:
        - Refused to set unsafe header "Content-length"
        - Refused to set unsafe header "Connection"
        - Look @ http://www.w3.org/TR/XMLHttpRequest/#the-setrequestheader-method
    
    -- ScienceBase now includes multiple features in their describefeature WFS call.
       This breaks our functionality to auto-select a feature. Figure out a way around 
       this.
--%>

<!DOCTYPE html>
<html>
    <jsp:include page="template/head.jsp" />
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
            <jsp:include page="template/footer.jsp" />
            <jsp:include page="jsp/feedback/feedback.jsp">
                <jsp:param name="securityimageDir" value="securityimage"/>
                <jsp:param name="imageDir" value="images"/>
                <jsp:param name="serviceDir" value="service"/>
            </jsp:include>
        </div>
    </body>
</html>

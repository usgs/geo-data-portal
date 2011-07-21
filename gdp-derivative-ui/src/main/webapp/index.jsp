<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <jsp:include page="template/head.jsp"></jsp:include>
        <jsp:include page="template/css.jsp"></jsp:include>
        <jsp:include page="template/scripts.jsp">
            <jsp:param name="UIScriptFile" value="js/derivative_portal/root.js" />
        </jsp:include>
        <title>USGS Derived Downscaled Climate Portal</title>
    </head>
    <body>
        <jsp:include page="template/header.jsp">
            <jsp:param name="imageDir" value="images"/>
        </jsp:include>
        <jsp:include page="template/footer.jsp">
            <jsp:param name="footer-class" value="x-hidden"/>
            <jsp:param name="imageDir" value="images"/>
        </jsp:include>
    </body>
</html>

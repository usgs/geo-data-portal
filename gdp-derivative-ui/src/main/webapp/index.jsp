<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<html>
    <head>
        <jsp:include page="template/head.jsp"></jsp:include>
        <jsp:include page="template/css.jsp"></jsp:include>
        <jsp:include page="template/scripts.jsp">
            <jsp:param name="ComponentDir" value="js/derivative_portal/components" />
            <jsp:param name="UIScriptFile" value="js/derivative_portal/root.js" />
        </jsp:include>
    </head>
    <body>
        <jsp:include page="template/header.jsp">
            <jsp:param name="imageDir" value="images"/>
        </jsp:include>
        <jsp:include page="template/main.jsp"></jsp:include>
        <jsp:include page="template/footer.jsp">
            <jsp:param name="footer-class" value="x-hidden"/>
            <jsp:param name="imageDir" value="images"/>
        </jsp:include>
    </body>
</html>

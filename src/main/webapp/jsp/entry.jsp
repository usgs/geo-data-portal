<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:import url="${cont}/jsp/header.jsp" var="head" />
<c:import url="${cont}/jsp/footer.jsp" var="foot" />
<c:set var="cont" value="<%=request.getContextPath()%>" />
<c:url var="upload" value="/jsp/fileUpload.jsp"/>
<c:url var="geotoolsProcessing" value="/Router?location=geotoolsProcessing&action=initial"/>
<c:url var="cdmProcessing" value="/jsp/cdmProcessing.jsp"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="${cont}/css/gdp_main.css"  title="USGS Full Width Nav" media="projection, screen, tv" type="text/css">
	<link rel="stylesheet" href="${cont}/css/gdp_main_print.css"  title="USGS Full Width Nav" media="print" type="text/css">
	<link rel="stylesheet" href="${cont}/css/full_width.css"  title="USGS Full Width Nav" media="projection, screen, tv" type="text/css">
	<title>Entry Point</title>
</head>
<body>
${head}
	<a href="${upload}">Upload Files For Current Session</a> | 
	<a href="${geotoolsProcessing}">Geotools Processing</a> | 
	<a href="${cdmProcessing}">CDM Processing</a>
	<br />
	<hr />
${foot}
</body>
</html>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%-- Set the application's context path --%>
<c:set var="cont" value="<%=request.getContextPath()%>" />

<c:import url="/jsp/header.jsp" var="head" />
<c:import url="/jsp/footer.jsp" var="foot" />

<c:url var="upload" value="/Router?location=uploadFiles" />
<c:url var="fileSelection" value="/Router?location=fileSelection" />
<c:url var="filesProcessing" value="/Router?location=filesProcessing"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="${cont}/css/gdp_main.css"  title="USGS Full Width Nav" media="projection, screen, tv" type="text/css">
	<link rel="stylesheet" href="${cont}/css/gdp_main_print.css"  title="USGS Full Width Nav" media="print" type="text/css">
	<link rel="stylesheet" href="${cont}/css/full_width.css"  title="USGS Full Width Nav" media="projection, screen, tv" type="text/css">
	<title>Welcome to the GeoData Portal</title>
</head>
<body>
${head}
	<a href="${upload}">Upload Files For Current Session</a> | 
	<a href="${filesProcessing}">Select Files For Processing</a>
	<br />
	<hr />
	Welcome to the GeoData Portal
${foot}
</body>
</html>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:import url="${cont}/jsp/header.jsp" var="head" />
<c:import url="${cont}/jsp/footer.jsp" var="foot" />
<c:set var="cont" value="<%=request.getContextPath()%>" />
<c:set var="processExampleShapeFiles"
	value="/ParseFile?action=processFiles&fileset=exampleShape&method=geoTools" />
<c:url var="upload" value="/jsp/fileUpload.jsp"/>
<c:url var="geotoolsProcessing" value="/jsp/geotoolsProcessing.jsp"/>
<c:url var="cdmProcessing" value="/jsp/cdmProcessing.jsp"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="${cont}/css/ippa_main.css"  title="USGS Full Width Nav" media="projection, screen, tv" type="text/css">
	<link rel="stylesheet" href="${cont}/css/ippa_main_print.css"  title="USGS Full Width Nav" media="print" type="text/css">
	<link rel="stylesheet" href="${cont}/css/full_width.css"  title="USGS Full Width Nav" media="projection, screen, tv" type="text/css">
	<title>Entry Point</title>
</head>
<body>
${head}
	<a href="${upload}">Upload Files For Current Session</a> | 
	<a href="${cdmProcessing}">CDM Processing</a>
	<br />
	<hr />
	<form id="processGeotoolsForm" method="post" name="processGeotoolsForm" action="${cont}">
		<input type="button" 
			value="Process Example Shape Files" 
			name="processExampleShape" 
			id="processExampleShapeFiles" 
			onclick="document.processGeotoolsForm.action='${cont}${processExampleShapeFiles}';document.processGeotoolsForm.submit()"></button>
	</form>
${foot}
</body>
</html>
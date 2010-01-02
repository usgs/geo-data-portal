<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:import url="/jsp/header.jsp" var="head" />
<c:import url="/jsp/footer.jsp" var="foot" />
<c:set var="cont" value="<%=request.getContextPath()%>" />
<c:url var="upload" value="/jsp/fileUpload.jsp"/>
<c:url var="geotoolsProcessing" value="/jsp/geotoolsProcessing.jsp"/>
<c:url var="cdmProcessing" value="/jsp/cdmProcessing.jsp"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="${cont}/css/gdp_main.css"  title="USGS Full Width Nav" media="projection, screen, tv" type="text/css">
	<link rel="stylesheet" href="${cont}/css/gdp_main_print.css"  title="USGS Full Width Nav" media="print" type="text/css">
	<link rel="stylesheet" href="${cont}/css/full_width.css"  title="USGS Full Width Nav" media="projection, screen, tv" type="text/css">
	<title>File Upload</title>
</head>
<body>
${head}
	<a href="${geotoolsProcessing}">Geotools Processing</a> | 
	<a href="${cdmProcessing}">CDM Processing</a>
	<br />
	<hr />
File Upload Area: <br />
<form method="post" enctype="multipart/form-data" action="${cont}/ParseFile">
	<label for="emailAddress">E-Mail Address</label>
	<input id="emailAddress" type="text" name="emailAddress" size="30" />
	<br />
	<label for="shpFile">SHP File</label>
	<input id="shpFile" type="file" name="shpFile" size="70" />
	<br />
	<label for="shxFile">SHX File</label>
	<input id="shxFile" type="file" name="shxFile" size="70" />
	<br />
	<label for="prjFile">PRJ File</label>
	<input id="prjFile" type="file" name="prjFile" size="70" />
	<br />
	<input type="submit" value="Submit Files" title="Submit Files" /><br />
	<input type="submit" value="Process Stored Files Using CDM" title="Process Stored Files Using CDM" /><br />
	<input type="submit" value="Process Stored Files Using Geotools" title="Process Stored Files Using Geotools" /><br />
</form>

${foot}
</body>
</html>
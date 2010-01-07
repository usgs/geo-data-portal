<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:import url="${cont}/jsp/header.jsp" var="head" />
<c:import url="${cont}/jsp/footer.jsp" var="foot" />
<jsp:useBean id="exampleFileList" scope="session" class="java.util.ArrayList"  />
<jsp:useBean id="uploadedFileList" scope="session" class="java.util.ArrayList"  />
<jsp:useBean id="summaryResults" scope="request" class="java.util.ArrayList"  />

<c:set var="cont" value="<%=request.getContextPath()%>" />
<c:set var="process" value="/Router?location=geotoolsProcessing&action=processFiles&method=geoTools&function=summarize" />
<c:url var="upload" value="/Router?location=uploadFiles" />
<c:url var="cdmProcessing" value="/jsp/cdmProcessing.jsp"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="${cont}/css/gdp_main.css"  title="USGS Full Width Nav" media="projection, screen, tv" type="text/css">
	<link rel="stylesheet" href="${cont}/css/gdp_main_print.css"  title="USGS Full Width Nav" media="print" type="text/css">
	<link rel="stylesheet" href="${cont}/css/full_width.css"  title="USGS Full Width Nav" media="projection, screen, tv" type="text/css">
	<title>Processing Using Geotools</title>
</head>
<body>
${head}
	<a href="${upload}">Upload Files For Current Session</a> | 
	<a href="${cdmProcessing}">CDM Processing</a>
	<br />
	<hr />
	
	<form id="processGeotoolsForm" method="post" name="processGeotoolsForm" action="${cont}">
	Files Available For Use:<br />
	<fieldset class="applicationFieldSet">
		<legend style="display: solid !important">
			Example File List
		</legend>
		<c:forEach var="fileName" items="${exampleFileList}">
			<input id="exampleFileCheckbox" type="checkbox" name="fileName" value="${fileName}">
			${fileName}
			<br />
		</c:forEach>
	</fieldset>
	<fieldset class="applicationFieldSet">
		<legend style="display: solid !important">
			Uploaded File List
		</legend>
		<c:forEach var="fileName" items="${uploadedFileList}">
			<input id="exampleFileCheckbox" type="checkbox" name="fileName" value="${fileName}">
			${fileName}
			<br />
		</c:forEach>
	</fieldset>
	
	
		<input type="button" 
			value="Process Shape Files" 
			name="processExampleShape" 
			id="processExampleShapeFiles" 
			onclick="document.processGeotoolsForm.action='${cont}${process}';document.processGeotoolsForm.submit()" />
	</form>
	<c:forEach var="summaryResultArraylist" items="${summaryResults}"> 
		<c:forEach var="summaryResultsArrayListItem" items="${summaryResultArraylist}">
			${summaryResultsArrayListItem} <br />
		</c:forEach>
		<br /><br />
	</c:forEach>
${foot}
</body>
</html>
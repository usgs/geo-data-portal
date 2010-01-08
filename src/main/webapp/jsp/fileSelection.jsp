<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="cont" value="<%=request.getContextPath()%>" />

<c:import url="/jsp/header.jsp" var="head" />
<c:import url="/jsp/footer.jsp" var="foot" />

<jsp:useBean id="exampleFileBean" scope="session" class="gov.usgs.gdp.bean.FilesBean"  />
<jsp:useBean id="uploadedFilesBean" scope="session" class="gov.usgs.gdp.bean.FilesBean"  />
<jsp:useBean id="summaryResults" scope="request" class="java.util.ArrayList"  />

<c:set var="process" value="/Router?location=geotoolsProcessing&action=processFiles&method=geoTools&function=summarize" />
<c:set var="summarize" value="/Router?location=summarize" />
<c:url var="upload" value="/Router?location=uploadFiles" />
<c:url var="cdmProcessing" value="/jsp/cdmProcessing.jsp"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="${cont}/css/gdp_main.css"  title="USGS Full Width Nav" media="projection, screen, tv" type="text/css">
	<link rel="stylesheet" href="${cont}/css/gdp_main_print.css"  title="USGS Full Width Nav" media="print" type="text/css">
	<link rel="stylesheet" href="${cont}/css/full_width.css"  title="USGS Full Width Nav" media="projection, screen, tv" type="text/css">
	<title>Select Files To Process</title>
</head>
<body>
${head}
	<a href="${upload}">Upload Files For Current Session</a>
	<br />
	<hr />
	
	<form id="processFiles" method="post" name="processFiles" action="${cont}">
	Files Available For Use:<br />
	
	<fieldset class="applicationFieldSet">
		<legend style="display: solid !important">
			Uploaded File List
		</legend>
		<c:forEach var="file" items="${uploadedFilesBean.files}">
			<input id="exampleFileCheckbox" type="checkbox" name="fileName" value="${file.name}">
			${file.name}
			<br />
		</c:forEach>
	</fieldset>
	
	<fieldset class="applicationFieldSet">
		<legend style="display: solid !important">
			Example File List
		</legend>
		<c:forEach var="file" items="${exampleFileBean.files}">
			<input id="exampleFileCheckbox" type="checkbox" name="fileName" value="${file.name}">
			${file.name}
			<br />
		</c:forEach>
	</fieldset>
	
	<input type="button" 
		value="Process Shape Files" 
		name="processExampleShape" 
		id="processExampleShapeFiles" 
		onclick="document.processFiles.action='${cont}${process}';document.processFiles.submit()" />
	
	<input type="button" 
		value="Summarize Shape Files" 
		name="summarizeShapeFiles" 
		id="summarizeShapeFiles" 
		onclick="document.processFiles.action='${cont}${summarize}';document.processFiles.submit()" />
	
	</form>
	
${foot}
</body>
</html>
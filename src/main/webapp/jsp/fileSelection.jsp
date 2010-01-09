<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*"  isELIgnored="false" %>
<%@ page import="gov.usgs.gdp.bean.FilesBean" isELIgnored="false" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="cont" value="<%=request.getContextPath()%>" />

<c:import url="/jsp/header.jsp" var="head" />
<c:import url="/jsp/footer.jsp" var="foot" />

<jsp:useBean id="exampleFileBeanList" scope="session" type="java.util.ArrayList<gov.usgs.gdp.bean.FilesBean>"  />
<jsp:useBean id="uploadedFilesBeanList" scope="session" type="java.util.ArrayList<gov.usgs.gdp.bean.FilesBean>"  />
<jsp:useBean id="shapeFileSetBeanList" scope="session" type="java.util.ArrayList<gov.usgs.gdp.bean.ShapeFileSetBean>"  />

<jsp:useBean id="errorBean" scope="request" class="gov.usgs.gdp.bean.ErrorBean"  />

<c:set var="process" value="/Router?location=processFiles&action=step1" />
<c:set var="summarize" value="/Router?location=summarize" />
<c:url var="upload" value="/Router?location=uploadFiles" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="${cont}/css/gdp_main.css"  title="USGS Full Width Nav" media="projection, screen, tv" type="text/css">
	<link rel="stylesheet" href="${cont}/css/gdp_main_print.css"  title="USGS Full Width Nav" media="print" type="text/css">
	<link rel="stylesheet" href="${cont}/css/full_width.css"  title="USGS Full Width Nav" media="projection, screen, tv" type="text/css">
	<title>Select Files To Process</title>
</head>
<script type="text/javascript">

</script>
<body>
${head}
	<a href="${upload}">Upload Files For Current Session</a>
	<br />
	<hr />
	<div id="shapeFileSetDiv">
		<form id="processFiles" method="post" name="processFiles" action="${cont}">		
		Files Available:<br />
		<fieldset class="applicationFieldSet">
			<legend style="display: solid !important">
				Available ShapeFile Sets
			</legend>
			<c:forEach var="fileBean" items="${shapeFileSetBeanList}">
					<input id="exampleFileCheckbox" type="checkbox" name="fileName" value="${fileBean.name}">
					${fileBean.name}
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
	</div>
	<div id="fileSetDiv">
		<fieldset class="applicationFieldSet">
			<legend style="display: solid !important">
				Uploaded File List
			</legend>
			<c:forEach var="fileBean" items="${uploadedFilesBeanList}">
				<c:forEach var="file" items="${fileBean.files}">
					${file.name}
					<br />
				</c:forEach>
			</c:forEach>
		</fieldset>
		
		<fieldset class="applicationFieldSet">
			<legend style="display: solid !important">
				Example File List
			</legend>
			<c:forEach var="fileBean" items="${exampleFileBeanList}">
				<c:forEach var="file" items="${fileBean.files}">
					${file.name}
					<br />
				</c:forEach>
				<br />
			</c:forEach>
		</fieldset>
		
		
	</div>
	<div id="errorText">
		<c:forEach var="error" items="${errorBean.errors}">				
			${error}
			<br />
		</c:forEach>
	</div>
${foot}
</body>
</html>
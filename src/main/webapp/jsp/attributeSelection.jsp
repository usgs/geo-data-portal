<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*"  isELIgnored="false" %>
<%@ page import="gov.usgs.gdp.bean.FilesBean" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="cont" value="<%=request.getContextPath()%>" />

<c:import url="/jsp/header.jsp" var="head" />
<c:import url="/jsp/footer.jsp" var="foot" />

<jsp:useBean id="shapeFileSetBeanSubsetList" scope="session" type="java.util.ArrayList<gov.usgs.gdp.bean.ShapeFileSetBean>"  />
<jsp:useBean id="errorBean" scope="request" class="gov.usgs.gdp.bean.MessageBean"  />

<c:set var="process" value="/Router?location=processFiles&action=step2" />
<c:set var="summarize" value="/Router?location=summarize" />
<c:url var="upload" value="/Router?location=uploadFiles" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="${cont}/css/gdp_main.css"  title="USGS Full Width Nav" media="projection, screen, tv" type="text/css">
	<link rel="stylesheet" href="${cont}/css/gdp_main_print.css"  title="USGS Full Width Nav" media="print" type="text/css">
	<link rel="stylesheet" href="${cont}/css/full_width.css"  title="USGS Full Width Nav" media="projection, screen, tv" type="text/css">
	<title>Select File Attributes To Work With</title>
</head>
<script type="text/javascript">

</script>
<body>
${head}
	<a href="${upload}">Upload Files For Current Session</a> |
	<a href="${filesProcessing}">Select Files For Processing</a>
	<br />
	<hr />
	Please select fileset attributes:<br />
	<div id="shapeFileSetDiv">
		<form id="processFiles" method="post" name="processFiles" action="${cont}">
		<c:forEach var="shapeFileSetBean" items="${shapeFileSetBeanSubsetList}">
			<fieldset class="applicationFieldSet">
			<legend style="display: solid !important">
					${shapeFileSetBean.name}
			</legend>
			<select id="attributeSelect" name="attributeSelection">
				<c:forEach var="listItem" items="${shapeFileSetBean.attributeList}">
						<option value="${shapeFileSetBean.name}::${listItem}" />${listItem}
				</c:forEach>
			</select>
		</fieldset>
		</c:forEach>		
		
		<input type="button" 
			value="Submit Attribute Selection(s)" 
			name="processExampleShape" 
			id="processExampleShapeFiles" 
			onclick="document.processFiles.action='${cont}${process}';document.processFiles.submit()" />		
		</form>
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
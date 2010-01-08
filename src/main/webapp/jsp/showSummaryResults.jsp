<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="cont" value="<%=request.getContextPath()%>" />

<c:import url="/jsp/header.jsp" var="head" />
<c:import url="/jsp/footer.jsp" var="foot" />

<jsp:useBean id="summaryBeanList" scope="session" class="gov.usgs.gdp.bean.SummaryBean"  />

<c:set var="process" value="/Router?location=geotoolsProcessing&action=processFiles&method=geoTools&function=summarize" />
<c:set var="summarize" value="/Router?location=summarize" />
<c:url var="upload" value="/Router?location=uploadFiles" />
<c:url var="fileSelection" value="/Router?location=fileSelection" />
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
	<a href="${upload}">Upload Files For Current Session</a> |
	<a href="${fileSelection}">Select Files For Processing</a>
	<br />
	<hr />
	
	<c:forEach var="summaryBean" items="${summaryBeanList}"> 
			File Name: ${summaryBean.fileName} <hr />
			<c:set var="summaryList" value="${summaryBean.fileSummary}" />
			<c:forEach var="summaryLine" items="${summaryList}">
				${summaryLine} <br />
			</c:forEach>
		<br /><br />
	</c:forEach>
	
${foot}
</body>
</html>
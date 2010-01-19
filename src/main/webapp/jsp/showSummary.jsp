<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*"  isELIgnored="false" %>
<%@ page import="gov.usgs.gdp.bean.FilesBean" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:set var="cont" value="<%=request.getContextPath()%>" />
<c:set var="process" value="/Router?location=processFiles&action=step8" />
<c:set var="summarize" value="/Router?location=summarize" />

<c:import url="/jsp/header.jsp" var="head" />
<c:import url="/jsp/footer.jsp" var="foot" />

<jsp:useBean id="shapeFileSetBean" scope="session" type="gov.usgs.gdp.bean.ShapeFileSetBean"  />
<jsp:useBean id="errorBean" scope="request" class="gov.usgs.gdp.bean.MessageBean"  />
<jsp:useBean id="messageBean" scope="request" class="gov.usgs.gdp.bean.MessageBean"  />
<jsp:useBean id="threddsInfoBean" scope="session" class="gov.usgs.gdp.bean.THREDDSInfoBean"  />

<c:url var="upload" value="/Router?location=uploadFiles" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<link rel="stylesheet" href="${cont}/css/gdp_main.css"  title="USGS Full Width Nav" media="projection, screen, tv" type="text/css">
		<link rel="stylesheet" href="${cont}/css/gdp_main_print.css"  title="USGS Full Width Nav" media="print" type="text/css">
		<link rel="stylesheet" href="${cont}/css/full_width.css"  title="USGS Full Width Nav" media="projection, screen, tv" type="text/css">
		<title>Select Time Periods To Work With</title>
	</head>

<body>
${head}
	<a href="${upload}">Upload Files For Current Session</a> |
	<a href="${filesProcessing}">Select Files For Processing</a>
	<br />
	<hr />
	
	<div id="fileSetDivCenter">
	Chosen FileSet(s) and options:<br />
			<fieldset class="applicationFieldSet">
				<legend>
						${shapeFileSetBean.name}
				</legend>
				Attribute: ${shapeFileSetBean.chosenAttribute} <br />
				Feature: ${shapeFileSetBean.chosenFeature} <br />
				THREDDS Server: ${threddsInfoBean.THREDDSServer} <br />
				DataSet: ${threddsInfoBean.dataSetNameSelection} <br />
				Grid: ${threddsInfoBean.gridItemSelection}
				Time Sets:  ${threddsInfoBean.fromTime} to ${threddsInfoBean.toTime}
				Stats: 
				<c:forEach var="statItem" items="${threddsInfoBean.statsSummary}">
					${statItem} <br />
				</c:forEach>			
			</fieldset>
	</div>
	
	<a href="${cont}${process}">Download File(s)</a>
	
<div id="errorText">
	<ul>
		<c:forEach var="error" items="${errorBean.messages}">				
			<li>${error}</li>
			<br />
		</c:forEach>
	</ul>
</div>
<div id="messageText">
	<ul>
		<c:forEach var="message" items="${messageBean.messages}">				
			<li>${message}</li>
		</c:forEach>
	</ul>
</div>
${foot}
</body>
</html>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:import url="/jsp/header.jsp" var="head" />
<c:import url="/jsp/footer.jsp" var="foot" />

<c:set var="cont" value="<%=request.getContextPath()%>" />
<jsp:useBean id="fileLink" scope="request" type="java.lang.String"  />
<jsp:useBean id="errorBean" scope="request" class="gov.usgs.gdp.bean.MessageBean"  />
<jsp:useBean id="messageBean" scope="request" class="gov.usgs.gdp.bean.MessageBean"  />

<c:url var="downloadFile" value="/Router?location=downloadFile&file="/>
<c:url var="filesProcessing" value="/Router?location=filesProcessing"/>
<c:url var="cdmProcessing" value="/jsp/cdmProcessing.jsp"/>
<c:url var="deleteFile" value="/Router?location=uploadFiles&action=delete&file="/>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="${cont}/css/gdp_main.css"  title="USGS Full Width Nav" media="projection, screen, tv" type="text/css">
	<link rel="stylesheet" href="${cont}/css/gdp_main_print.css"  title="USGS Full Width Nav" media="print" type="text/css">
	<link rel="stylesheet" href="${cont}/css/full_width.css"  title="USGS Full Width Nav" media="projection, screen, tv" type="text/css">
	<title>File Download</title>
</head>

<script type="text/javascript">
<!--
	function addUploadSlot() {
		var uploadInputList = document.getElementById("fileUploads");
		var addSlotText = "<label for='fileUploadInput'>File...</label> " +
			"<input class='fileUploadInput' type='file' name='fileUploadInput' size='30' />" +
			"<br />";
		uploadInputList.innerHTML = uploadInputList.innerHTML + addSlotText;
	}
-->
</script> 
<body>
${head}
	<a href="${filesProcessing}">Files Processing</a> 
	<br />
	<hr />
<a href="${downloadFile}${fileLink}">Download Your File</a>
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
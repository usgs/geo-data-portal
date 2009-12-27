<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:import url="/jsp/header.jsp" var="head" />
<c:import url="/jsp/footer.jsp" var="foot" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="../css/ippa_main.css"  title="USGS Full Width Nav" media="projection, screen, tv" type="text/css">
	<link rel="stylesheet" href="../css/ippa_main_print.css"  title="USGS Full Width Nav" media="print" type="text/css">
	<link rel="stylesheet" href="../css/full_width.css"  title="USGS Full Width Nav" media="projection, screen, tv" type="text/css">
	<title>Initial Entry</title>
</head>
<body>
${head}
File Upload Area: <br />
<form method="post" enctype="multipart/form-data" action="<%=request.getContextPath()%>/ParseFile">
	<label for="emailAddress">E-Mail Address</label>
	<input id="emailAddress" type="text" name="emailAddress" size="100" />
	<br />
	<label for="shpFile">SHP File</label>
	<input id="shpFile" type="file" name="shpFile" size="100" />
	<br />
	<label for="shxFile">SHX File</label>
	<input id="shxFile" type="file" name="shxFile" size="100" />
	<br />
	<input type="submit" />
	
</form>
${foot}
</body>
</html>
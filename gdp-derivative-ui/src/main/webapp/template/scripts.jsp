<%--<script src="js/jquery/jquery.js" type="text/javascript"></script>--%>

<script src="js/ext/adapter/ext/ext-base.js" type="text/javascript"></script>
<%--script src="js/ext/adapter/jquery/ext-jquery-adapter.js" type="text/javascript"></script --%>

<script type="text/javascript" src="js/ext/ext-all-debug.js"></script>
<script type="text/javascript">
Ext.BLANK_IMAGE_URL = 'images/s.gif';<%-- Path to the blank image should point to a valid location on your server --%>
</script>

<script src="js/openlayers/OpenLayers.js" type="text/javascript"></script>
<script src="js/geoext/GeoExt.js" type="text/javascript"></script>

<script type="text/javascript">
	Ext.override(GeoExt.WMSLegend,{
		showTitle : false
	});
</script>

<script src="js/log4javascript/log4javascript.js" type="text/javascript"></script>

<%-- Custom Application Modules Here --%>
<script type="text/javascript" src="js/derivative_portal/components/Animator.js"></script>
<script type="text/javascript" src="js/derivative_portal/components/LayerController.js"></script>
<script type="text/javascript" src="js/derivative_portal/components/LayerChooser.js"></script>
<script type="text/javascript" src="js/derivative_portal/components/TimestepChooser.js"></script>
<script type="text/javascript" src="js/derivative_portal/components/BaseMap.js"></script>


<script src='${param["UIScriptFile"]}' type="text/javascript"></script>

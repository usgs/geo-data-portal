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
		showTitle : false,
		getLegendUrl: function(layerName, layerNames) {
			var rec = this.layerRecord;
			var url;
			var styles = rec && rec.get("styles");
			var layer = rec.getLayer();
			layerNames = layerNames || [layer.params.LAYERS].join(",").split(",");

			var styleNames = layer.params.STYLES &&
				[layer.params.STYLES].join(",").split(",");
			var idx = layerNames.indexOf(layerName);
			var styleName = styleNames && styleNames[idx];
			// check if we have a legend URL in the record's
			// "styles" data field
			if(styles && styles.length > 0) {
				if(styleName) {
					Ext.each(styles, function(s) {
						url = (s.name == styleName && s.legend) && s.legend.href;
						return !url;
					});
				} else if(this.defaultStyleIsFirst === true && !styleNames &&
					!layer.params.SLD && !layer.params.SLD_BODY) {
					url = styles[0].legend && styles[0].legend.href;
				}
			}
			if(!url) {
				url = layer.getFullRequestString({
					REQUEST: "GetLegendGraphic",
					WIDTH: null,
					HEIGHT: null,
					EXCEPTIONS: "application/vnd.ogc.se_xml",
					LAYER: layerName,
					LAYERS: null,
					STYLE: (styleName !== '') ? styleName: null,
					STYLES: null,
					SRS: null,
					FORMAT: null
				});
			}
			// add scale parameter - also if we have the url from the record's
			// styles data field and it is actually a GetLegendGraphic request.
			if(this.useScaleParameter === true &&
                url.toLowerCase().indexOf("request=getlegendgraphic") != -1) {
				var scale = layer.map.getScale();
				url = Ext.urlAppend(url, "SCALE=" + scale);
			}
			var params = this.baseParams || {};
			Ext.applyIf(params, {FORMAT: 'image/gif'});
			if(url.indexOf('?') > 0) {
				url = Ext.urlEncode(params, url);
			}
        
			return 'proxy/' + url;
		}
	});
	
	Ext.override(Ext.Container, {
		doLayout : function(shallow){
			if(!this.isVisible() || this.collapsed){
				this.deferLayout = this.deferLayout || !shallow;
				return;
			}
			shallow = shallow && !this.deferLayout;
			delete this.deferLayout;
			if(this.rendered && this.layout){
				this.layout.layout();
			}
			if(shallow !== false && this.items){
				var cs = this.items.items;
				for(var i = 0, len = cs.length; i < len; i++) {
					var c  = cs[i];
					if(c.doLayout){
						c.doLayout();
					}
				}
			}
		},
		onShow : function(){
			Ext.Container.superclass.onShow.apply(this, arguments);
			if(this.deferLayout !== undefined){
				this.doLayout(true);
			}
		}
	});
	Ext.override(Ext.Panel, {
		afterExpand : function(){
			this.collapsed = false;
			this.afterEffect();
			if(this.deferLayout !== undefined){
				this.doLayout(true);
			}
			this.fireEvent('expand', this);
		}
	});
</script>

<script src="js/log4javascript/log4javascript.js" type="text/javascript"></script>

<%-- Custom Application Modules Here --%>
<script type="text/javascript" src="js/derivative_portal/components/notify.js"></script>
<script type="text/javascript" src="js/derivative_portal/components/Animator.js"></script>
<script type="text/javascript" src="js/derivative_portal/components/LayerController.js"></script>
<script type="text/javascript" src="js/derivative_portal/components/LayerChooser.js"></script>
<script type="text/javascript" src="js/derivative_portal/components/TimestepChooser.js"></script>
<script type="text/javascript" src="js/derivative_portal/components/BaseMap.js"></script>


<script src='${param["UIScriptFile"]}' type="text/javascript"></script>

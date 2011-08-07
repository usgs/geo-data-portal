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
<script type="text/javascript" src="js/derivative_portal/components/PolygonPOIPanel.js"></script>
<script type="text/javascript" src="js/derivative_portal/components/LayerChooser.js"></script>
<script type="text/javascript" src="js/derivative_portal/components/TimestepChooser.js"></script>
<script type="text/javascript" src="js/derivative_portal/components/BaseMap.js"></script>
<script type="text/javascript" src="js/derivative_portal/components/MapActivityBar.js"></script>



<script src='${param["UIScriptFile"]}' type="text/javascript"></script>

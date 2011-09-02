<%-- Base EXT modules & Extensions --%>
<script type="text/javascript" src="js/ext/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="js/ext/ext-all-debug.js"></script>
<script type="text/javascript" src='${param["ComponentDir"]}/extension/notify.js'></script>

<%-- Other JavaScript modules (Mapping, logging, etc) --%>
<script src="js/log4javascript/log4javascript.js" type="text/javascript"></script>
<script src="js/openlayers/lib/OpenLayers.js" type="text/javascript"></script>
<script src="js/geoext/GeoExt.js" type="text/javascript"></script>

<%-- Custom Application Modules Here --%>
<script type="text/javascript" src='${param["ComponentDir"]}/Animator.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/LayerController.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/BoundsPanelSubmitButton.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/BaseMap.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/MapActivityBar.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/WPS/processes/FeatureCoverageOPeNDAPIntersection.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/WPS/WPSCapabilitiesStore.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/WPS/WPSExecuteResponseStore.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/WPS/WPSCapabilitiesReader.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/WPS/WPSExecuteResponseReader.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/WPS/WPSDescribeProcessStore.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/WPS/WPSDescribeProcessReader.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/panels/WPSPanel.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/panels/WPSProcessPanel.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/panels/TimestepChooser.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/panels/EndpointPanel.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/panels/DatasetConfigPanel.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/panels/ConfigurationPanel.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/panels/MapConfigPanel.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/panels/ProcessingPanel.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/panels/PolygonPOIPanel.js'></script>


<%-- Root Module --%>
<script src='${param["UIScriptFile"]}' type="text/javascript"></script>


<script type="text/javascript">
    Ext.BLANK_IMAGE_URL = 'images/s.gif';
    
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


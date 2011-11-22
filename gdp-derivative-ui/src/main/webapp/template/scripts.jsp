<%-- Base EXT modules & Extensions --%>
<script type="text/javascript" src="js/ext/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="js/ext/ext-all.js"></script>
<script type="text/javascript" src='${param["ComponentDir"]}/extension/notify.js'></script>

<%-- Other JavaScript modules (Mapping, logging, etc) --%>
<script type="text/javascript" src="js/log4javascript/log4javascript.js" ></script>
<script type="text/javascript" src="js/openlayers/OpenLayers.js"></script>
<script type="text/javascript" src="js/geoext/GeoExt.js"></script>
<script type="text/javascript" src="js/dygraph/dygraph-combined.js"></script>

<%-- Extended Openlayers/GeoExt readers/writers --%>
<script type="text/javascript" src='${param["ComponentDir"]}/CSW/Format/v2_0_2.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/SOS/Format/SOSGetObservation.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/CSW/CSWGetRecordsStore.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/CSW/CSWGetRecordsReader.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/SOS/SOSGetObservationStore.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/SOS/SOSGetObservationReader.js'></script>

<%-- Custom Application Modules Here --%>
<script type="text/javascript" src='${param["ComponentDir"]}/LayerController.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/panels/DatasetConfigPanel.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/panels/Plotter.js'></script>
<script type="text/javascript" src='${param["ComponentDir"]}/panels/BaseMap.js'></script>

<%-- Root Module --%>
<script type="text/javascript" src='${param["UIScriptFile"]}'></script>


<script type="text/javascript">
    // http://ejohn.org/blog/fast-javascript-maxmin/
    Array.max = function( array ){
        return Math.max.apply( Math, array );
    };
    Array.min = function( array ){
        return Math.min.apply( Math, array );
    };


    
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


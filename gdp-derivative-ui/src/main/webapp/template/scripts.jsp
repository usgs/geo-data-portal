<%@page import="org.apache.commons.lang.StringUtils"%>

<%-- Base EXT modules & Extensions --%>
<script type="text/javascript" src="js/ext/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="js/ext/ext-all.js"></script>
<script type="text/javascript" src='${param["ComponentDir"]}/extension/notify.js'></script>

<%-- Other JavaScript modules (Mapping, logging and plotting) --%>
<script type="text/javascript" src="js/log4javascript/log4javascript.js" ></script>
<script type="text/javascript" src="js/openlayers/lib/OpenLayers.js"></script>
<script type="text/javascript" src="js/geoext/GeoExt.js"></script>
<script type="text/javascript" src="js/dygraph/dygraph-combined.js"></script>

<!--To be used if dygraphs is not working and needs to be debugged-->
<!--<script type="text/javascript" src="js/dygraph-unminified/dygraph-layout.js"></script>
<script type="text/javascript" src="js/dygraph-unminified/dygraph-canvas.js"></script>
<script type="text/javascript" src="js/dygraph-unminified/dygraph.js"></script>
<script type="text/javascript" src="js/dygraph-unminified/dygraph-utils.js"></script>
<script type="text/javascript" src="js/dygraph-unminified/dygraph-gviz.js"></script>
<script type="text/javascript" src="js/dygraph-unminified/dygraph-interaction-model.js"></script>
<script type="text/javascript" src="js/dygraph-unminified/dygraph-range-selector.js"></script>
<script type="text/javascript" src="js/dygraph-unminified/dygraph-tickers.js"></script>
<script type="text/javascript" src="js/dygraph-unminified/rgbcolor/rgbcolor.js"></script>
<script type="text/javascript" src="js/dygraph-unminified/strftime/strftime-min.js"></script>-->

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
    GDP.LOG4JS_PATTERN_LAYOUT = '<%= StringUtils.isBlank(System.getProperty("LOG4JS_PATTERN_LAYOUT")) ? "%rms - %-5p - %m%n" : System.getProperty("LOG4JS_PATTERN_LAYOUT") %>';
    GDP.PROXY_PREFIX = '<%= StringUtils.isBlank(System.getProperty("PROXY_PREFIX")) ? "proxy/" : System.getProperty("PROXY_PREFIX") %>';
    GDP.DEFAULT_LEGEND_NAME = '<%= StringUtils.isBlank(System.getProperty("DEFAULT_LEGEND_NAME")) ? "boxfill/occam" : System.getProperty("DEFAULT_LEGEND_NAME") %>';
    GDP.CSW_QUERY_CONSTRAINT_FILTER_VALUE = '<%= StringUtils.isBlank(System.getProperty("CSW_QUERY_CONSTRAINT_FILTER_VALUE")) ? "a0a3c56c-2be5-4d45-9924-72b13e348919" : System.getProperty("CSW_QUERY_CONSTRAINT_FILTER_VALUE") %>';
    GDP.FOI_GETCAPS_URL = GDP.PROXY_PREFIX + '<%= StringUtils.isBlank(System.getProperty("FOI_GETCAPS_URL")) ? "http://igsarm-cida-javadev1.er.usgs.gov:8081/geoserver/derivative/wms?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities" : System.getProperty("FOI_GETCAPS_URL") %>';
    
    // http://ejohn.org/blog/fast-javascript-maxmin/
    Array.max = function( array , maxVal ){
        var arrMax = Math.max.apply( Math, array );
        if (arguments.length == 1) {
            return arrMax;
        }
        else {
            return (arrMax > maxVal) ? arrMax : maxVal;
        }
    };
    Array.min = function( array , minVal ){
        var arrMin = Math.min.apply( Math, array );
        if (arguments.length == 1) {
            return arrMin;
        }
        else {
            return (arrMin < minVal) ? arrMin : minVal;
        }
    };
    Array.mean = function( array ) {
        if (array.length == 0) return NaN;
        var total = 0;
        Ext.each(array, function(item) {
           total += item; 
        });
        return total / array.length;
    }
    
    // http://jibbering.com/faq/#parseDate
    Date.parseISO8601 = function(dateStringInRange){
        var isoExp = /^\s*(\d{4})-(\d\d)-(\d\d)\s*$/,
        date = new Date(NaN), month,
        parts = isoExp.exec(dateStringInRange);

        if(parts) {
            month = +parts[2];
            date.setFullYear(parts[1], month - 1, parts[3]);
            if(month != date.getMonth() + 1) {
                date.setTime(NaN);
            }
        }
        return date;
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


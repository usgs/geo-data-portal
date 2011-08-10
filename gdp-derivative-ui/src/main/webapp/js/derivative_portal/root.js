//TODO- Way too much coniguration going on in this file. A lot of the elements can (should) be made into their own classes and self-configured in there
//TODO- Make sure that the element initializations are coming in at the right order -- With this much initialization from one place, lots of room for issues to come up
var LOG;
var NOTIFY;
var LOADMASK;

function test() {
    
    var inputData = '<?xml version="1.0" encoding="UTF-8"?>'
    inputData += '<wps:Execute service="WPS" version="1.0.0" xmlns:wps="http://www.opengis.net/wps/1.0.0" xmlns:ows="http://www.opengis.net/ows/1.1" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd">'
    inputData += '<ows:Identifier>gov.usgs.cida.gdp.wps.algorithm.FeatureCoverageOPeNDAPIntersectionAlgorithm</ows:Identifier>'
    inputData += '<wps:DataInputs>'
    inputData += '<wps:Input>'
    inputData += '<ows:Identifier>DATASET_URI</ows:Identifier>'
    inputData += '<wps:Data>'
    inputData += '<wps:LiteralData>dods://cida.usgs.gov/qa/thredds/dodsC/derivative/tmin-threshold-proto.ncml</wps:LiteralData>'
    inputData += '</wps:Data>'
    inputData += '</wps:Input>'
    inputData += '<wps:Input>'
    inputData += '<ows:Identifier>DATASET_ID</ows:Identifier>'
    inputData += '<wps:Data>'
    inputData += '<wps:LiteralData>days_below_threshold</wps:LiteralData>'
    inputData += '</wps:Data>'
    inputData += '</wps:Input>'
    inputData += '<wps:Input>'
    inputData += '<ows:Identifier>REQUIRE_FULL_COVERAGE</ows:Identifier>'
    inputData += '<wps:Data>'
    inputData += '<wps:LiteralData>false</wps:LiteralData>'
    inputData += '</wps:Data>'
    inputData += '</wps:Input>'
    inputData += '<wps:Input>'
    inputData += '<ows:Identifier>FEATURE_COLLECTION</ows:Identifier>'
    inputData += '<wps:Data>'
    inputData += '<wps:ComplexData schema="http://schemas.opengis.net/gml/3.1.1/base/feature.xsd" encoding="UTF-8" mimeType="text/xml">'
    inputData += '<gml:featureMembers xmlns:ogc="http://www.opengis.net/ogc" xmlns:draw="gov.usgs.cida.gdp.draw" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:ows="http://www.opengis.net/ows" xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink" xsi:schemaLocation="gov.usgs.cida.gdp.draw http://cida-wiwsc-gdp2qa.er.usgs.gov:8080/derivative-portal/xsd/draw.xsd">'
    inputData += '<gml:box gml:id="box.1">'
    inputData += '<gml:the_geom>'
    inputData += '<gml:MultiPolygon srsDimension="2" srsName="http://www.opengis.net/gml/srs/epsg.xml#4326">'
    inputData += '<gml:polygonMember>'
    inputData += '<gml:Polygon>'
    inputData += '<gml:exterior>'
    inputData += '<gml:LinearRing>'
    inputData += '<gml:posList>-116.76342791319 45.776855155826 -116.76342791319 31.055175468326 -94.790771663189 31.055175468326 -94.790771663189 45.776855155826 -116.76342791319 45.776855155826</gml:posList>'
    inputData += '</gml:LinearRing>'
    inputData += '</gml:exterior>'
    inputData += '</gml:Polygon>'
    inputData += '</gml:polygonMember>'
    inputData += '</gml:MultiPolygon>'
    inputData += '</gml:the_geom>'
    inputData += '<gml:ID>0</gml:ID>'
    inputData += '</gml:box>'
    inputData += '</gml:featureMembers>'
    inputData += '</wps:ComplexData>'
    inputData += '</wps:Data>'
    inputData += '</wps:Input>'
    inputData += '</wps:DataInputs>'
    inputData += '<wps:ResponseForm>'
    inputData += '<wps:ResponseDocument storeExecuteResponse="true" status="true">'
    inputData += '<wps:Output asReference="true">'
    inputData += '<ows:Identifier>OUTPUT</ows:Identifier>'
    inputData += '</wps:Output>'
    inputData += '</wps:ResponseDocument>'
    inputData += '</wps:ResponseForm>'
    inputData += '</wps:Execute>';
    
    var data = '<?xml version="1.0" encoding="UTF-8"?> \
<ns:ExecuteResponse xmlns:ns="http://www.opengis.net/wps/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_response.xsd" serviceInstance="http://cida-wiwsc-gdp1qa.er.usgs.gov:8080/gdp-process-wps/WebProcessingService?REQUEST=GetCapabilities&amp;SERVICE=WPS" xml:lang="en-US" service="WPS" version="1.0.0" statusLocation="http://cida-wiwsc-gdp1qa.er.usgs.gov:8080/gdp-process-wps/RetrieveResultServlet?id=1312919425852">\\n\
<ns:Process ns:processVersion="1.0.0">\
<ns1:Identifier xmlns:ns1="http://www.opengis.net/ows/1.1">gov.usgs.cida.gdp.wps.algorithm.FeatureCoverageOPeNDAPIntersectionAlgorithm</ns1:Identifier>\
<ns1:Title xmlns:ns1="http://www.opengis.net/ows/1.1">Feature Coverage OPeNDAP Intersection</ns1:Title>\
</ns:Process>\
<ns:Status creationTime="2011-08-09T14:50:25.835-05:00">\
<ns:ProcessStarted/>\
</ns:Status>\
</ns:ExecuteResponse>';
    
    var derpDa = '<?xml version="1.0" encoding="UTF-8"?><ns:ExecuteResponse xmlns:ns="http://www.opengis.net/wps/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_response.xsd" serviceInstance="http://localhost:8080/gdp-process-wps/WebProcessingService?REQUEST=GetCapabilities&amp;SERVICE=WPS" xml:lang="en-US" service="WPS" version="1.0.0" statusLocation="http://localhost:8080/gdp-process-wps/RetrieveResultServlet?id=1312944775238"><ns:Process ns:processVersion="1.0.0"><ns1:Identifier xmlns:ns1="http://www.opengis.net/ows/1.1">gov.usgs.cida.gdp.wps.algorithm.FeatureCoverageOPeNDAPIntersectionAlgorithm</ns1:Identifier><ns1:Title xmlns:ns1="http://www.opengis.net/ows/1.1">Feature Coverage OPeNDAP Intersection</ns1:Title></ns:Process><ns:Status creationTime="2011-08-09T21:52:55.184-05:00"><ns:ProcessFailed><ns1:ExceptionReport xmlns:ns1="http://www.opengis.net/ows/1.1"><ns1:Exception><ns1:ExceptionText>General Error: java.io.IOException: opendap.dap.DataReadException: Inconsistent array length read: 1165128303 != 1914731274</ns1:ExceptionText></ns1:Exception></ns1:ExceptionReport></ns:ProcessFailed></ns:Status></ns:ExecuteResponse>';
        
    var doc;
    if(window.ActiveXObject){
        doc = new ActiveXObject("Microsoft.XMLDOM");
        doc.async = "false";
        doc.loadXML(derpDa);
    }else{
        doc = new DOMParser().parseFromString(derpDa,"text/xml");
    }
    
    
    Ext.Ajax.request({
        url : 'proxy/' + 'http://cida-wiwsc-gdp1qa.er.usgs.gov:8080/gdp-process-wps/WebProcessingService',
        method: 'POST',
        xmlData : inputData,
        scope : this,
        success: function ( result, request ) {
            LOG.debug('BoundsPanelSubmitButton:onClick:Ajax:success.');
            var xml = result.responseXML;
            var procStarted = xml.getElementsByTagName('ns:ProcessStarted')
            var processLink;
            if (procStarted.length > 0) {
                // The process has started
                processLink = xml.getElementsByTagName('ns:ExecuteResponse')[0].getAttribute('statusLocation');
            }
        },
        failure: function ( result, request) {
            LOG.debug('BoundsPanelSubmitButton:onClick:Ajax:failure');
        // Notify the user that this call has failed.
        }
    });
    
    

//    

//    

    
    
//    var capabilitiesStore = new GDP.WPSExecuteResponseStore({
//        url : 'http://cida-wiwsc-gdp1qa.er.usgs.gov:8080/gdp-process-wps/WebProcessingService',
//        data : inputData,
//        storeId : 'wps-execresponse-store'
//    });
//    capabilitiesStore.load();
//    capabilitiesStore.on('load', function() {
//        
//        LOG.debug('test');
//    }, capabilitiesStore);
}

Ext.onReady(function () {
        GDP.PROXY_PREFIX = 'proxy/';
        GDP.DEFAULT_LEGEND_NAME = 'boxfill/greyscale';
	initializeLogging();
//        test();
//        return;
	initializeNotification();
	initializeMapping();
});

function initializeLogging() {
	LOG = log4javascript.getLogger();
	var layout = new log4javascript.PatternLayout("%rms - %d{HH:mm:ss.SSS} %-5p - %m%n");
	var appender = new log4javascript.BrowserConsoleAppender();
	appender.setLayout(layout);
	LOG.addAppender(appender);
	LOG.info('Derivative Portal: Logging initialized.');
}



function initializeNotification() {
    LOG.info('Derivative Portal: Initializing Notification.');
    LOG.debug('root:initializeNotification');
	var _notifyError = function(config) {
		if (!config) config = {};
		
		var moreInfo = new Ext.Button({text: 'More Info...'});
		var buttons = [];
		
		if (config.moreInfoAction) {
			buttons.push(moreInfo);
		}
		
		var notify = new Ext.ux.Notify({
			msgWidth: 200,
			hideDelay: 8000,
			title: 'Error!',
			titleIconCls: 'titleicon-error',
			msgIconCls: 'msgicon-error',
			msg: 'An error has occured.',
			buttons: buttons
		});
		
		if (config.moreInfoAction) {
			moreInfo.on('click', function() {
					notify.hide();
					config.moreInfoAction();
				});
		}
		
            notify.show(document);
	}
	
	var _notifySuccess = function() {
		new Ext.ux.Notify({
			msgWidth: 200,
			hideDelay: 1000,
			title: 'Success!',
			titleIconCls: 'titleicon-success',
			msg: 'Data saved successfully.'
		}).show(document);
	}
	
	var _notifyDebug = function(msg) {
		new Ext.ux.Notify({
			msgWidth: 200,
			hideDelay: 3000,
			title: 'DEBUG',
			titleIconCls: 'titleicon-debug',
			msg: msg
		}).show(document);
	}
        
        var _notifyWarning = function(msg) {
            new Ext.ux.Notify({
			msgWidth: 200,
			hideDelay: 3000,
			title: 'WARNING',
			titleIconCls: 'titleicon-warning',
			msg: msg
		}).show(document);
        }
	NOTIFY = {
		debug : _notifyDebug,
		success : _notifySuccess,
		error : _notifyError,
                warn : _notifyWarning
	};
        LOG.info('Derivative Portal: Notification Initialized.');
}

function initializeMapping() {
    LOG.info('Derivative Portal: Initializing Mapping.');
    LOG.debug('root:initializeMapping');
    LOADMASK = new Ext.LoadMask(Ext.getBody());
	
    var proxyUrl, urls, endpointStore, endpointCombo, endpointApplyButton, endpointContainer, endpointPanel;
    {
        proxyUrl = '';
        urls = [
                ['http://cida-wiwsc-gdp1qa.er.usgs.gov:8080/ncWMS/wms?SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.1.1'],
                ['http://igsarmewmaccave:8081/ncWMS/wms?SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.1.1'],
                ['http://igsarm-cida-thredds1.er.usgs.gov:8080/thredds/wms/gmo/GMO_w_meta.ncml?service=WMS&version=1.1.1&request=GetCapabilities']
        ];
        endpointStore = new Ext.data.ArrayStore({
                storeId : 'endpointStore',
                idIndex: 0,
                fields: ['url']
        });
        endpointStore.loadData(urls);
        endpointCombo = new Ext.form.ComboBox({
            mode : 'local'
            ,triggerAction: 'all'
            ,flex : 1
            ,store : endpointStore
            ,value : urls[0][0]
            ,lazyInit : false
            ,displayField : 'url'
            
        });
        endpointApplyButton = new Ext.Button({
            text : 'Go'
        });
        endpointCombo.on('select', function(){
            endpointApplyButton.fireEvent('click');
        });
        endpointContainer = new Ext.form.CompositeField({
            region : 'center'
            ,fieldLabel : 'Endpoint'
            ,items : [
                endpointCombo,
                endpointApplyButton
            ]
        });
        if (endpointCombo.getRawValue()) {
            proxyUrl = GDP.PROXY_PREFIX + endpointCombo.getRawValue();
        }
        endpointPanel = new Ext.Panel({
            region : 'north',
            border : false,
            layout : 'border',
            collapsed : true,
            collapsible : true,
            floatable : false,
            hideCollapseTool : true,
            collapseMode : 'mini',
            split : true,
            height : 25,
            minSize : 25,
            maxSize : 25,
            items : [endpointContainer]
        });
    }

    var capabilitiesStore = new GeoExt.data.WMSCapabilitiesStore({
        url : proxyUrl,
        storeId : 'capabilitiesStore'
    });
    
    var legendStore = new Ext.data.JsonStore({
        idProperty: 'name'
        ,root: 'styles'
        ,fields: [
            {name: 'name', mapping: 'name'}
            ,{name: 'title', mapping: 'title'}
            ,{name: 'abstrakt', mapping: 'abstract'}
            ,{name: 'width', mapping: 'legend.width'}
            ,{name: 'height', mapping: 'legend.height'}
            ,{name: 'format', mapping: 'legend.format'}
            ,{name: 'href', mapping: 'legend.href'}
        ]
    });
    var baseLayerStore = new GeoExt.data.LayerStore({
        layers : [
        new OpenLayers.Layer.WMS(
            "Blue Marble",
            "http://maps.opengeo.org/geowebcache/service/wms",
            {
                layers: "bluemarble"
            }
            ),
        new OpenLayers.Layer.WMS(
            "NAIP",
            "http://isse.cr.usgs.gov/ArcGIS/services/Combined/SDDS_Imagery/MapServer/WMSServer",
            {
                layers: "0"
            }
            ),
        new OpenLayers.Layer.XYZ(
            "Shaded Relief",
            "http://server.arcgisonline.com/ArcGIS/rest/services/ESRI_ShadedRelief_World_2D/MapServer/tile/${z}/${y}/${x}",
            {
                layers : "0"
            }
            ),
        new OpenLayers.Layer.XYZ(
            "Street Map",
            "http://server.arcgisonline.com/ArcGIS/rest/services/ESRI_StreetMap_World_2D/MapServer/tile/${z}/${y}/${x}",
            {
                layers : "0"
            }
            )
        ]
    });
    
    // Central controller for the application.
    var layerController = new GDP.LayerController({
        baseLayer : baseLayerStore.getAt(2) 
        ,legendStore : legendStore
        ,dimensions : ['time', 'elevation']
    });

    //UI Components
    var configPanel, mapPanel, timestepPanel, centerPanel;
    
    mapPanel = new GDP.BaseMap({
            id : 'mapPanel',
            region: 'center',
            layout : 'fit',
            border: false,
            layerController : layerController,
            title: 'USGS Derived Downscaled Climate Portal'
    });
    
    configPanel = new GDP.LayerChooser({
        id: 'control-panel',
        title : 'Controls',
        region: 'west',
        labelWidth: 80,
        border : false,
        collapsible : true,
        floatable : false,
        hideCollapseTool : true,
        collapseMode : 'mini',
        split : true,
        width : 265,
        minSize : 265,
        maxSize : 265,
        map : mapPanel.map,
        capabilitiesStore : capabilitiesStore,
        baseLayerStore : baseLayerStore,
        controller : layerController,
        defaults: {
            width: 180
        }
    });
    
    timestepPanel = new GDP.TimestepChooser({
        region : 'south',
        border : false,
        height : 30,
        layerController : layerController
    });
    centerPanel = new Ext.Panel({
        id : 'center-panel',
        region : 'center',
        layout : 'border',
        items : [ endpointPanel, mapPanel, timestepPanel]
    });

    var headerPanel, footerPanel;
    headerPanel = new Ext.Panel({
        id: 'header-panel',
        region: 'north',
        height: 'auto',
        border : false,
        autoShow: true,
        contentEl: 'usgs-header-panel'
    });
    footerPanel = new Ext.Panel({
        id: 'footer-panel',
        region: 'south',
        height: 'auto',
        border : false,
        autoShow: true,
        contentEl: 'usgs-footer-panel'
    });

    LOG.debug('root:initializeMapping: Registering Event Handlers.');
    {
        capabilitiesStore.on('exception', function() {
            if (LOADMASK) LOADMASK.hide();
            NOTIFY.error();
        }, this);
        endpointApplyButton.on('click', function() {
            LOG.debug('EVENT: User has clicked on the endpoint apply button');
            var endpoint = endpointCombo.getRawValue();
            if (endpoint) {
                LOG.debug('EVENT: Adding ' + endpoint + ' to the capabilities store');
                if (LOADMASK) LOADMASK.show();
                var proxyUrl = '';
                proxyUrl = GDP.PROXY_PREFIX + endpoint;
                capabilitiesStore.proxy.setApi(Ext.data.Api.actions.read, proxyUrl);
                capabilitiesStore.load();
            }
        }, this);
    }

    LOG.info('Derivative Portal: Mapping initialized.');
    if (LOADMASK) LOADMASK.show();
    
    LOG.debug('root: Loading capabilities store.');
    capabilitiesStore.load();
    LOG.debug('root: Triggering LayerController:requestBaseLayer.');
    layerController.requestBaseLayer(layerController.getBaseLayer());
    
    new Ext.Viewport({
            renderTo : document.body,
            items : [headerPanel, centerPanel, configPanel,footerPanel], 
            layout: 'border'
    });
}
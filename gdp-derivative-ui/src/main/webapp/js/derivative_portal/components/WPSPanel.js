Ext.ns("GDP");

GDP.WPSPanel = Ext.extend(Ext.Panel, {
    controller : undefined,
    getController : function() {
        return this.controller;
    },
    bounds : undefined,
    getBounds : function() {
        return this.bounds;
    },
    capabilitiesStore : undefined,
    getCapabilitiesStore : function() {
        return this.capabilitiesStore;
    },
    constructor : function(config) {
        LOG.debug('WPSPanel:constructor: Constructing self.');
        
        var processEndpoint = 'proxy/http://cida-wiwsc-gdp1qa.er.usgs.gov:8080/gdp-process-wps';
        var processGetCaps = processEndpoint + '/WebProcessingService?Service=WPS&Request=GetCapabilities';
        var currentProcess = 'gov.usgs.cida.gdp.wps.algorithm.FeatureCoverageOPeNDAPIntersectionAlgorithm';
        
        var items = [];
        if (!config) config = {};
        this.controller = config.controller;
        this.bounds = config.bounds;
        
        LOG.debug('WPSPanel:constructor: Constructing capabilitiesStore.');
        var capabilitiesStore = new GDP.WPSCapabilitiesStore({
            url : processGetCaps,
            storeId : 'wps-capabilities-store'
        });
        capabilitiesStore.load();
        capabilitiesStore.on('load', function() {
            LOG.debug('WPSPanel: Capabilities store loaded. Firing event "capabilities-store-loaded"');
            this.capabilitiesStore = capabilitiesStore;
            this.fireEvent('capabilities-store-loaded');
        }, this);
        
        LOG.debug('WPSPanel:constructor: Constructing algorithm description panel.');
        var describeProcessPanel = new Ext.Panel({
            id : 'process-description-panel',
            region : 'center',
            border : false,
            html : 'This service returns the subset of data that intersects a set of vector polygon features and time range, if specified. A NetCDF file will be returned.'
        })
        items.push(describeProcessPanel);
        
        LOG.debug('WPSPanel:constructor: Constructing algorithm description panel.');
        var displayProcessingPanel = new Ext.Panel({
            id : 'display-processing-panel',
            region : 'center',
            border : false,
            html : 'Submitting process to server.',
            hidden : true
        })
        items.push(displayProcessingPanel);
        
        LOG.debug('WPSPanel:constructor: Constructing WPS algorithm dropdown list.');
        var layerCombo = new Ext.form.ComboBox({
            xtype : 'combo',
            mode : 'local',
            triggerAction: 'all',
            store : capabilitiesStore,
            fieldLabel : 'Process',
            forceSelection : true,
            lazyInit : false,
            displayField : 'processOfferings',
            emptyText : 'Loading Processes'
        });
        layerCombo.on('select', function(combo, record, index) {
            
        });
//        items.push(layerCombo);
        
        LOG.debug('WPSPanel:constructor: Constructing submit button.');
        var submitButton = new Ext.Button({
            id : 'wps-submit-button',
            region : 'south',
            text : 'ProcessBounds'
        });
        submitButton.on('click', function() {
            this.submitButtonClicked()
        }, this);
        items.push(submitButton);
        
        config = Ext.apply({
            id : 'wps-panel',
            items : items,
            layout : 'form',
            title : 'WPS Submit',
            border : true
        }, config);
        GDP.PolygonPOIPanel.superclass.constructor.call(this, config);
        LOG.debug('WPSPanel:constructor: Construction complete.');
        
        LOG.debug('WPSPanel:constructor: Registering Observables.');
        this.addEvents(
                "capabilities-store-loaded",
                "wps-submit-clicked"
            );
                
        LOG.debug('WPSPanel:constructor: Registering Listeners.');
        this.on('wps-submit-clicked', function(){
            LOG.debug('WPSPanel: Observed "wps-submit-clicked"');
            this.createProcess();
        }, this);
    },
    submitButtonClicked : function() {
        LOG.debug('WPSPanel:submitButtonClicked: Submit button clicked');
        LOG.debug('WPSPanel:submitButtonClicked: Firing Event "wps-submit-clicked"');
        this.fireEvent('wps-submit-clicked');
    },
    createProcess : function() {
        LOG.debug('WPSPanel:createProcess: Collecting process inputs.');
        
        var bounds = this.bounds;
        
//        http://cida.usgs.gov/thredds/dodsC/derivative/derivative-days_above_threshold.ncml
//        http://cida.usgs.gov/thredds/dodsC/derivative/derivative-days_below_threshold.ncml
//        http://cida.usgs.gov/thredds/dodsC/derivative/tmin_threshold.ncml
        
        var layer = this.controller.getLayer();
        var layerName = layer.data.name;
        var layerFullName = layer.data.layer.params.LAYERS;
        var datasetUriPre = 'http://cida.usgs.gov/qa/thredds/dodsC/derivative/';
        var datasetUriMid = layerFullName.split('/')[0];
        var datasetUriPost = '.ncml';
        var datasetUri = datasetUriPre + datasetUriMid + datasetUriPost;
        var datasetId = layerFullName.split('/')[1];
        var requireFullCoverage = false;
        var timeStart = undefined;
        var timeEnd = undefined;
        
        var wfsXML = this.createWfsFeatureXml(bounds);
        var wpsXML = this.createWpsExecuteRequest({
            wfsXML : wfsXML,
            fullCoverage : requireFullCoverage,
            datasetUri : datasetUri,
            datasetId : datasetId,
            timeStart : timeStart,
            timeEnd : timeEnd
        });
        
        // Create XML to send to WPS backing process
        var xmlData;


        return; // stop here for now
        // Send the AJAX request and do success/fail handling. (We probably want to pass the 'success' function in?)
        Ext.Ajax.request({
            url : 'proxy/' ,
            method: 'POST',
            xmlData : xmlData,
            success: function ( result, request ) {
                LOG.debug('BoundsPanelSubmitButton:onClick:Ajax:success: ' + result);
            },
            failure: function ( result, request) {
                LOG.debug('BoundsPanelSubmitButton:onClick:Ajax:failure: ' + result);
            }
        });
        
    },
    createWfsFeatureXml : function(bounds) {
        LOG.debug('WPSPanel:createWfsXml: Creating WFS XML.');
        
        var left = bounds.left; 
        var right = bounds.right; 
        var top = bounds.top; 
        var bottom = bounds.bottom; 
        var now = new Date();
        var timestamp = now.format('c'); 
        var externalPortalMapping = 'http://cida-wiwsc-gdp2qa.er.usgs.gov:8080/derivative-portal/xsd/draw.xsd';
        
        var result = '<wfs:FeatureCollection xmlns:ogc="http://www.opengis.net/ogc" xmlns:draw="gov.usgs.cida.gdp.draw" xmlns:wfs="http://www.opengis.net/wfs" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:sample="gov.usgs.cida.gdp.sample" xmlns:ows="http://www.opengis.net/ows" xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:upload="gov.usgs.cida.gdp.upload" numberOfFeatures="0" timeStamp="'+timestamp+'" xsi:schemaLocation="gov.usgs.cida.gdp.draw '+externalPortalMapping+' http://schemas.opengis.net/wfs/1.1.0/wfs.xsd">';
	result += '<gml:featureMembers>';
        result += '<gml:box gml:id="box.1">';
        result += '<gml:the_geom>';
        result += '<gml:MultiPolygon srsDimension="2" srsName="http://www.opengis.net/gml/srs/epsg.xml#4326">';
        result += '<gml:polygonMember>';
        result += '<gml:Polygon>';
        result += '<gml:exterior>';
        result += '<gml:LinearRing>';
        result += '<gml:posList>';
        result += left + " " + top + " ";
        result += left + " " + bottom + " ";
        result += right + " " + bottom + " ";
        result += right + " " + top + " ";
        result += left + " " + top;
        result += '</gml:posList>'
        result += '</gml:LinearRing>'
        result += '</gml:exterior>'
        result += '</gml:Polygon>'
        result += '</gml:polygonMember>'
        result += '</gml:MultiPolygon>'
        result += '</gml:the_geom>'
        result += '<gml:ID>0</gml:ID>'
        result += '</gml:box>'
	result += '</gml:featureMembers>'
        result += '</wfs:FeatureCollection>'
        
        return result;
    },
    createWpsExecuteRequest : function(args) {
        var wfsXML =  args.wfsXML;
        var fullCoverage = args.fullCoverage;
        var datasetUri = args.datasetUri;
        var datasetId = args.datasetId;
        var timeStart = args.timeStart; // Currently using all times, so don't include
        var timeEnd = args.timeEnd; // Currently using all times, so don't include
        
        var result = '<?xml version="1.0" encoding="UTF-8"?>';
        result += '<wps:Execute service="WPS" version="1.0.0" xmlns:wps="http://www.opengis.net/wps/1.0.0" xmlns:ows="http://www.opengis.net/ows/1.1" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd">';
        result += '<ows:Identifier>gov.usgs.cida.gdp.wps.algorithm.FeatureCoverageOPeNDAPIntersectionAlgorithm</ows:Identifier>';
        result += '<wps:DataInputs>';
        result += '<wps:Input>';
        result += '<ows:Identifier>DATASET_URI</ows:Identifier>';
        result += '<wps:Data>';
        result += '<wps:LiteralData>'+datasetUri+'</wps:LiteralData>';
        result += '</wps:Data>';
        result += '</wps:Input>';
        result += '<wps:Input>';
        result += '<ows:Identifier>DATASET_ID</ows:Identifier>';
        result += '<wps:Data>';
        result += '<wps:LiteralData>'+datasetId+'</wps:LiteralData>';
        result += '</wps:Data>';
        result += '</wps:Input>';
        result += '<wps:Input>';
        result += '<ows:Identifier>REQUIRE_FULL_COVERAGE</ows:Identifier>';
        result += '<wps:Data>';
        result += '<wps:LiteralData>'+fullCoverage+'</wps:LiteralData>';
        result += '</wps:Data>';
        result += '</wps:Input>';
        result += '<wps:Input>';
        result += '<ows:Identifier>FEATURE_COLLECTION</ows:Identifier>';
        result += '<wps:Data>';
        result += '<wps:ComplexData>';
//        result += '<wps:Reference xlink:href="http://igsarm-cida-javadev1.er.usgs.gov:8081/geoserver/wfs">'
//        result += '<wps:Body>';
        result += wfsXML;
        result += '</wps:ComplexData>';
        result += '</wps:Data>';
//        result += '</wps:Body>';
//        result += '</wps:Reference>';
        result += '</wps:Input>';
        result += '</wps:DataInputs>';
        result += '<wps:ResponseForm>';
        result += '<wps:ResponseDocument storeExecuteResponse="true" status="true">';
        result += '<wps:Output asReference="true">';
        result += '<ows:Identifier>OUTPUT</ows:Identifier>';
        result += '</wps:Output>';
        result += '</wps:ResponseDocument>';
        result += '</wps:ResponseForm>';
        result += '</wps:Execute>';
        
        return result;
    }
    
});
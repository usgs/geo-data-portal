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
    timerPanel : undefined,
    getTimerPanel : function() {
        return this.timerPanel;
    },
    processPanels : {},
    getProcessPanel : function(id) {
        if (id) {
            return this.processPanels[id];
        } else {
            return this.processPanels;
        }
    },
    constructor : function(config) {
        LOG.debug('WPSPanel:constructor: Constructing self.');
//        var processEndpoint = 'proxy/http://127.0.0.1:8080/gdp-process-wps';
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
        
//        LOG.debug('WPSPanel:constructor: Constructing WPS algorithm dropdown list.');
//        var layerCombo = new Ext.form.ComboBox({
//            xtype : 'combo',
//            mode : 'local',
//            triggerAction: 'all',
//            store : capabilitiesStore,
//            fieldLabel : 'Process',
//            forceSelection : true,
//            lazyInit : false,
//            displayField : 'processOfferings',
//            emptyText : 'Loading Processes'
//        });
//        layerCombo.on('select', function(combo, record, index) {
//            
//        });
//        items.push(layerCombo);
        
        LOG.debug('WPSPanel:constructor: Creating panel to hold timer.');
        var timerPanel = new Ext.Panel({
            id : 'display-processing-panel',
            border : true,
            region : 'center'
        })
        timerPanel.on('added', function() {
            LOG.debug('Timer added to application');
            this.doLayout();
        }, timerPanel);
        timerPanel.on('removed', function() {
            LOG.debug('WPSPanel: Timer removed from application');
        }, this);
        items.push(timerPanel);
        this.timerPanel = timerPanel;
        
        LOG.debug('WPSPanel:constructor: Constructing submit button.');
        var submitButton = new Ext.Button({
            id : 'wps-submit-button',
            text : 'Bind Process',
            region : 'south'
        });
        submitButton.on('click', function() {
            this.submitButtonClicked();
        }, this);
        items.push(submitButton);
        
        config = Ext.apply({
            id : 'wps-panel',
            items : items,
            layout : 'form',
            title : 'WPS Submit',
            border : true
        }, config);
        GDP.WPSPanel.superclass.constructor.call(this, config);
        LOG.debug('WPSPanel:constructor: Construction complete.');
        
        LOG.debug('WPSPanel:constructor: Registering Observables.');
        this.addEvents(
                "capabilities-store-loaded",
                "wps-submit-clicked",
                "process-started"
            );
                
        LOG.debug('WPSPanel:constructor: Registering Listeners.');
        this.on('wps-submit-clicked', function(){
            LOG.debug('WPSPanel: Observed "wps-submit-clicked"');
            this.createProcess();
        }, this);
        this.on('process-started', function(args) {
            LOG.debug('WPSPanel: Observed "process-started"');
            this.addProcessChecker(args);
        }, this);
        this.on('added', function() {
            this.doLayout();
        }, this);
        this.on('removed', function() {
            this.doLayout();
        }, this);
        
    },
    submitButtonClicked : function() {
        LOG.debug('WPSPanel:submitButtonClicked: Submit button clicked');
        LOG.debug('WPSPanel:submitButtonClicked: Firing Event "wps-submit-clicked"');
        this.fireEvent('wps-submit-clicked');
    },
    addProcessChecker : function(args) {
        LOG.debug('WPSPanel:addProcessChecker: Kicking off process');
        LOG.debug('WPSPanel:addProcessChecker: Constructing process process checker panel.');
        
        var processLink = args.processLink;
        var processId = processLink.split('=')[1];
        
        var wpsProcessPanel = new GDP.WPSProcessPanel({
            id : 'process-panel-' + processId,
            processLink : processLink,
            processId : processId,
            controller : this
        });
        this.getProcessPanel()[processId] = wpsProcessPanel;
        this.getTimerPanel().add(wpsProcessPanel);
        this.getTimerPanel().doLayout();
    },
    processCancelled : function(processId) {
        LOG.debug('WPSPanel:processCancelled: Removing panel ID: ' + processId);
        this.getTimerPanel().remove(this.getProcessPanel(processId));
        this.getProcessPanel(processId).destroy();
        this.doLayout();
    },
    createProcess : function() {
        LOG.debug('WPSPanel:createProcess: Collecting process inputs.');
        
        var bounds = this.bounds;
        
        var layer = this.controller.getLayer();
        var layerName = layer.data.name;
        var layerFullName = layer.data.layer.params.LAYERS;
        var datasetUriPre = 'dods://cida.usgs.gov/qa/thredds/dodsC/derivative/';
        var datasetUriMid = layerFullName.split('/')[0];
        var datasetUriPost = '.ncml';
        var datasetUri = datasetUriPre + datasetUriMid + datasetUriPost;
        var datasetId = layerFullName.split('/')[1];
        var requireFullCoverage = true;
        var timeStart = undefined;
        var timeEnd = undefined;
        
        // Create XML to send to WPS backing process
        var wfsXML = this.createWfsFeatureXml(bounds);
        var wpsXML = this.createWpsExecuteRequest({
            wfsXML : wfsXML,
            fullCoverage : requireFullCoverage,
            datasetUri : datasetUri,
            datasetId : datasetId,
            timeStart : timeStart,
            timeEnd : timeEnd
        });
        
        // Begin the process
        Ext.Ajax.request({
            url : 'proxy/' + 'http://cida-wiwsc-gdp1qa.er.usgs.gov:8080/gdp-process-wps/WebProcessingService',
            method: 'POST',
            xmlData : wpsXML,
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
                this.fireEvent('process-started', {processLink : processLink});
            },
            failure: function ( result, request) {
                LOG.debug('BoundsPanelSubmitButton:onClick:Ajax:failure');
            // Notify the user that this call has failed.
            }
        });
        
        return; // stop here for now
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
        
	var result = '<gml:featureMembers xmlns:ogc="http://www.opengis.net/ogc" xmlns:draw="gov.usgs.cida.gdp.draw" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:ows="http://www.opengis.net/ows" xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink" xsi:schemaLocation="gov.usgs.cida.gdp.draw '+externalPortalMapping+'">';
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
        result += '<wps:ComplexData schema="http://schemas.opengis.net/gml/3.1.1/base/feature.xsd" encoding="UTF-8" mimeType="text/xml">';
        result += wfsXML;
        result += '</wps:ComplexData>';
        result += '</wps:Data>';
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
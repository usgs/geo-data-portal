Ext.ns("GDP");

GDP.WPSPanel = Ext.extend(Ext.Panel, {
    controller : undefined,
    getController : function() {
        return this.controller;
    },
    getBounds : function() {
        return this.bounds;
    },
    constructor : function(config) {
        LOG.debug('WPSPanel:constructor: Constructing self.');
        
        var processEndpoint = 'http://cida-wiwsc-gdp1qa.er.usgs.gov:8080/gdp-process-wps/WebProcessingService?Service=WPS&Request=GetCapabilities';
        var currentProcess = 'gov.usgs.cida.gdp.wps.algorithm.FeatureCoverageOPeNDAPIntersectionAlgorithm';
        
        
        if (!config) config = {};
        this.controller = config.controller;
        this.bounds = config.bounds;

        var items = [];
        LOG.debug('WPSPanel:constructor: Constructing capabilitiesStore.');
        var capabilitiesStore = new Ext.data.XmlStore({
            storeId : 'wps-capabilities-store',
            url : GDP.PROXY_PREFIX + processEndpoint,
            record : 'wps:Process',
            idPath : 'ows:Identifier',
            fields : [
                { name : 'id', mapping : 'ows:Identier'},
                { name : 'title', mapping : 'ows:Title' }
            ]
        });
        capabilitiesStore.load();
        
        LOG.debug('WPSPanel:constructor: Constructing WPS algorithm list.');
        
        
        LOG.debug('WPSPanel:constructor: Constructing submit button.');
        var submitButton = new Ext.Button({
            id : 'wps-submit-button',
            region : 'center',
            text : 'Submit WPS'
        });
        submitButton.on('click', function() { this.submitButtonClicked() });
        items.push(submitButton);
        
        config = Ext.apply({
            id : 'wps-panel',
            items : items,
            layout : 'form',
            title : 'WPS Submit',
            border : false
        }, config);
        GDP.PolygonPOIPanel.superclass.constructor.call(this, config);
        LOG.debug('WPSPanel:constructor: Construction complete.');
    }
});
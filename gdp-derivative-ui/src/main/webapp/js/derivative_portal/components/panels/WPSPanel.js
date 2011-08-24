Ext.ns("GDP");

GDP.WPSPanel = Ext.extend(Ext.Panel, {
    controller : undefined,
    bounds : undefined,
    capabilitiesStore : undefined,
    timerPanel : undefined,
    processPanel : undefined,
    processPanels : {},
    knownProcesses : {},
    threddsUrl : 'dods://cida.usgs.gov/qa/thredds/dodsC/derivative/',
    getProcessPanel : function(id) {
        if (id) {
            return this.processPanels[id];
        } else {
            return this.processPanels;
        }
    },
    constructor : function(config) {
        LOG.debug('WPSPanel:constructor: Constructing self.');
        
        var items = [];
        if (!config) config = {};
        this.controller = config.controller;
        this.bounds = config.bounds;
        
        // Create an array of known processes
        this.populateKnownProcesses();
        
        LOG.debug('WPSPanel:constructor: Constructing capabilitiesStore.');
        var capabilitiesStore = new GDP.WPSCapabilitiesStore({
            url : GDP.PROXY_PREFIX + GDP.PROCESS_ENDPOINT + '?Service=WPS&Request=GetCapabilities',
            storeId : 'wps-capabilities-store'
        });
        
        LOG.debug('WPSPanel:constructor: Creating panel to hold processes.');
        this.processPanel = new Ext.Panel({
            region : 'north',
            border : false
        })
        items.push(this.processPanel);
        
        LOG.debug('WPSPanel:constructor: Creating panel to hold timers.');
        this.timerPanel = new Ext.Panel({
            id : 'display-processing-panel',
            border : true,
            region : 'south',
            layout : 'accordion',
            animate : true,
            fill : true,
            title : 'Running Operations'
        })
        this.timerPanel.on('added', function() {
            LOG.debug('WPSPanel: Timer added to application');
            this.doLayout();
        }, this);
        this.timerPanel.on('removed', function() {
            LOG.debug('WPSPanel: Timer removed from application');
            this.doLayout();
        }, this);
        items.push(this.timerPanel);
        
        
        LOG.debug('WPSPanel:constructor: Constructing submit button.');
        
        config = Ext.apply({
            id : 'wps-panel',
            items : items,
            border : false
        }, config);
        GDP.WPSPanel.superclass.constructor.call(this, config);
        LOG.debug('WPSPanel:constructor: Construction complete.');
        capabilitiesStore.load();
        capabilitiesStore.on('load', function() {
            LOG.debug('WPSPanel: Capabilities store loaded. Firing event "capabilities-store-loaded"');
            this.capabilitiesStore = capabilitiesStore;
            this.constructProcessSelectionPanels({ areaType : config.areaType});
        }, this);
        
        LOG.debug('WPSPanel:constructor: Registering Observables.');
        this.addEvents(
            "request-attention"
        );
        LOG.debug('WPSPanel:constructor: Registering Listeners.');
        this.on('activate', function() {
            LOG.debug('WPSPanel: Activated. Removing attention icon.')
            this.setIconClass('');
        }, this)
    },
    populateKnownProcesses : function() {
        var opendapProc = new GDP.FeatureCoverageOPeNDAPIntersection({});
        this.knownProcesses[opendapProc.identifier] = opendapProc;
    },
    constructProcessSelectionPanels : function(args) {
        LOG.debug('WPSPanel:constructProcessSelectionPanels');
        var processes = this.capabilitiesStore.data.items[0].data.processOfferings;
        
        // Here we will go through every process available on the WPS server.
        // We will check each process against the array of known processes that we handle.
        // If we have a match, we will create a configuration panel for that process.
        // TODO- Though it should never happen, there is a possibility of no matches.  Handle that.
        Ext.iterate(processes, function(k ,v, o) {
            var identifier = v.identifier;
            var process = this.knownProcesses[identifier];
            if (process && process.areaTypes.indexOf(args.areaType) != -1) {
                LOG.debug('WPSPanel:constructProcessSelectionPanels: Process "'+ v.title +'" is known and handles this area type ('+args.areaType+'). Creating process panel for this process.');
                var describeProcessstore = new GDP.WPSDescribeProcessStore({
                    url : GDP.PROXY_PREFIX + GDP.PROCESS_ENDPOINT + '?Service=WPS&Request=DescribeProcess&Identifier=' + identifier
                });
                describeProcessstore.load();
                describeProcessstore.on('load', function(store, records, options){
                    LOG.debug('WPSPanel:constructProcessSelectionPanels: Loaded ' + records[0].data.title + ' process description from server.');
                    
                    var layer = this.controller.getLayer();
                    var layerFullName = layer.data.layer.params.LAYERS;
                    
                    // This right here is pretty weak. 
                    // Right now we're pulling WMS stuffs from ncWMS
                    // and are depending on a mirror naming scheme on THREDDS.
                    // TODO - Jordan's working on setting up a WCS solution to this
                    var datasetUriPre = this.threddsUrl;
                    var datasetUriMid = layerFullName.split('/')[0];
                    var datasetUriPost = '.ncml';
                    var datasetUri = datasetUriPre + datasetUriMid + datasetUriPost;
                    
                    // Hopefully this will come from ncWMS and we won't have to parse 
                    // ncWMS variables to pull critical bits of info from
                    var datasetId = layerFullName.split('/')[1];
                    
                    // We are only interested in the first record since the 
                    // describeProcess function only requests one process
                    var record = records[0].data;
                    var process = this.knownProcesses[identifier];
                    
                    // We have a process in our known processes list.  
                    // Let's (re)initialize the process with the describe
                    // process response and config infos from the portal (bounds, etc)
                    process.init({
                        title : record.title,
                        abstrakt : record.abstrakt,
                        statusSupported : record.statusSupported,
                        storeSupported : record.storeSupported,
                        dataInputs : record.dataInputs,
                        processOutputs : record.processOutputs,
                        processVersion : record.processVersion,
                        bounds : this.bounds,
                        fullCoverage : true,
                        datasetId : datasetId,
                        datasetUri : datasetUri
                    });
                    
                    var items = new Array();
                    var describeProcessPanel = new Ext.Panel({
                        id : 'process-description-panel-' + process.identifier,
                        border : false,
                        html : process.abstrakt,
                        region : 'center'
                    });
                    items.push(describeProcessPanel);
                    
                    var processConfigurationPanel = process.getConfigurables({ region : 'south'});
                    if (processConfigurationPanel) items.push(processConfigurationPanel);
                    
                    var submitButton = new Ext.Button({
                        text : 'Begin',
                        region : 'south',
                        process : process
                    });
                    submitButton.on('click', function(button, event) {
                        var process = button.process;
                        LOG.debug('Clicked button for process: ' + process.title);
                        this.addProcessChecker({ 
                            process : process,
                            button : button
                        });
                    }, this);
                    
                    var wpsProcessPanel = new Ext.Panel({
                        items : items,
                        process : process,
                        title : process.title,
                        buttons : [submitButton]
                    });
                    this.processPanel.add(wpsProcessPanel);
                    LOG.debug('WPSPanel: New process panel added. Firing event "request-attention"');
                    if (!this.isVisible()) this.fireEvent("request-attention", { obj : this });
                    
                    // http://internal.cida.usgs.gov/jira/browse/GDP-395
                    // TODO - This is fine since we only have one process. Remove this when we have more
                    wpsProcessPanel.on('afterrender', function() {
                       this.buttons[0].fireEvent('click', this);
                       (function(){
                                this.getEl().unmask();
                        }).defer(1000, this);
                    }, wpsProcessPanel)
                    
                    
                },this)
            }
        }, this);
    },
    updateBounds : function(args) {
        LOG.debug('WPSPanel:updateBounds');
        if (this.bounds == args.bounds) {
            LOG.debug('WPSPanel:updateBounds: Bounds submitted are already active bounds. Not rebuilding processing panels.');
        } else { // Still buggy
            this.bounds = args.bounds;
            LOG.debug('WPSPanel:updateBounds: Removing all process panels and will replace with new ones with updated bounds'); 
            this.processPanel.removeAll();
            this.constructProcessSelectionPanels(args);
        }
    },
    addProcessChecker : function(args) {
        LOG.debug('WPSPanel:addProcessChecker: Kicking off process');
        LOG.debug('WPSPanel:addProcessChecker: Constructing process process checker panel.');
        
        var process = args.process;
        var button = args.button;
        
        button.disable();
        
        var wpsProcessPanel = new GDP.WPSProcessPanel({
            id : 'process-panel-' + process.identifier,
            process : process,
            controller : this,
            ownerButton : args.button
        });
        wpsProcessPanel.on('request-attention', function(args){
            LOG.debug('WPSPanel: WPS process requested attention.');
            if (this.timerPanel.activeItem != args.obj) {
                LOG.debug('WPSPanel: Changing icon class.');
                args.obj.setIconClass('titleicon-warning');
                this.fireEvent("request-attention", { obj : this });
            }
        }, this);
        this.processPanel[process.identifier] = wpsProcessPanel;
        this.timerPanel.add(wpsProcessPanel);
        this.doLayout();
    }
});
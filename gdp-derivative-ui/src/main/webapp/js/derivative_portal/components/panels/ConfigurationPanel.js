Ext.ns("GDP");

/**
 * This panel is the holder for all other panels in the "control panel" area on 
 * the left side of the map
 */
GDP.ConfigurationPanel = Ext.extend(Ext.Panel, {
    controller : undefined,
    datasetConfigPanel : undefined,
    mapConfigPanel : undefined,
    constructor : function(config) {
        LOG.debug('ConfigurationPanel:constructor: Constructing self.');
        
        if (!config) config = {};
        this.controller = config.controller;
        
        this.datasetConfigPanel = new GDP.DatasetConfigPanel({
            controller : this.controller,
            //url : config.url,
            capabilitiesStore : config.capabilitiesStore,
            getRecordsStore : config.getRecordsStore,
            width : config.width || undefined,
            iconCls : 'blank-icon'
        });
        
        this.mapConfigPanel = new GDP.MapConfigPanel({
            controller : this.controller,
            //url : config.url,
            map : config.map,
            baseLayerStore : config.baseLayerStore,
            width : config.width || undefined,
            iconCls : 'blank-icon'
        });
        
        this.processingPanel = new GDP.ProcessingPanel({
            controller : this.controller,
            width : config.width || undefined,
            iconCls : 'blank-icon'
        });
        
        config = Ext.apply({
            layout : 'accordion',
            animate : true,
            title : 'Control Panel',
            border : false,
            items : [
                this.datasetConfigPanel,
                this.mapConfigPanel,
                this.processingPanel
            ]
        }, config);
        GDP.ConfigurationPanel.superclass.constructor.call(this, config);
        LOG.debug('ConfigurationPanel:constructor: Construction complete.');
        
        this.processingPanel.on('request-attention', function(args){
            this.onRequestAttention(args);
            LOG.debug('ConfigurationPanel: Processing panel requested attention.');
        }, this);
    },
    onRequestAttention : function(args) {
        if (this.activeItem != args.obj) {
            LOG.debug('ConfigurationPanel: Changing icon class of processing panel.');
            args.obj.setIconClass('titleicon-warning');
        }
    }
});
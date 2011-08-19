Ext.ns("GDP");

/**
 * This panel is a holder for processing-related panels
 */
GDP.ProcessingPanel = Ext.extend(Ext.Panel, {
    controller : undefined,
    constructor : function(config) {
        LOG.debug('ProcessingPanel:constructor: Constructing self.');

        this.controller = config.controller;
        
        config = Ext.apply({
            id : 'data-processing-panel',
            layout : 'form',
            //            iconCls : 'titleicon-warning',
            title : 'Data Processing',
            border : false
        }, config);
        GDP.ProcessingPanel.superclass.constructor.call(this, config);
        LOG.debug('ProcessingPanel:constructor: Construction complete.');
        
        LOG.debug('ProcessingPanel:constructor: Registering listeners.');
        this.controller.on('submit-bounds',function(args){
            LOG.debug('ProcessingPanel: Observed "submit-bounds"');
            this.boundsSubmitted(args);
        },this);
    },
    boundsSubmitted : function(args){
        LOG.debug('ProcessingPanel:boundsSubmitted');
        if (!this.get('wps-panel')) {
            LOG.debug('ProcessingPanel:boundsSubmitted: Constructing new WPS Panel');
            var wpsPanel = new GDP.WPSPanel(args);
            this.add(wpsPanel);
        } else {
            LOG.debug('ProcessingPanel:boundsSubmitted: WPS Panel exists. Updating bounds for algorithms');
            this.get('wps-panel').updateBounds(args);
        }
    }
});
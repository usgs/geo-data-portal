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
//            items : [],
            layout : 'form',
//            iconCls : 'titleicon-warning',
            title : 'Data Processing',
            border : false
        }, config);
        GDP.ProcessingPanel.superclass.constructor.call(this, config);
        LOG.debug('ProcessingPanel:constructor: Construction complete.');
        
        LOG.debug('ProcessingPanel:constructor: Registering listeners.');
        this.controller.on('submit-bounds',function(args){
            LOG.debug('LayerChooser: Observed "submit-bounds"');

            var wpsPanel = new GDP.WPSPanel(args);
            this.add(wpsPanel);
            this.doLayout(true);
            wpsPanel.setWidth(this.getWidth());
        },this);
    }
});
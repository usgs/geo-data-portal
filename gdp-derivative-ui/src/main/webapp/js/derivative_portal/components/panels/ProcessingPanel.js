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
            //            iconCls : 'titleicon-warning',
            title : 'Data Access',
            border : false
        }, config);
        GDP.ProcessingPanel.superclass.constructor.call(this, config);
        LOG.debug('ProcessingPanel:constructor: Construction complete.');
        
        LOG.debug('ProcessingPanel:constructor: Registering Observables.');
        this.addEvents(
            "request-attention"
        );

        LOG.debug('ProcessingPanel:constructor: Registering listeners.');
        this.controller.on('submit-bounds',function(args){
            LOG.debug('ProcessingPanel: Observed "submit-bounds"');
            this.boundsSubmitted(args);
        },this);
        this.on('activate', function() {
            LOG.debug('ProcessingPanel: Activated. Removing attention icon.')
            this.setIconClass('');
        }, this)
    },
    boundsSubmitted : function(args){
        LOG.debug('ProcessingPanel:boundsSubmitted');
        if (!this.get('wps-panel')) {
            LOG.debug('ProcessingPanel:boundsSubmitted: Constructing new WPS Panel');
            var wpsPanel = new GDP.WPSPanel(args);
            wpsPanel.on('request-attention', function(args){
            LOG.debug('ProcessingPanel: WPS process panel requested attention.');
            if (this.activeItem != args.obj) {
                LOG.debug('ProcessingPanel: Changing icon class of WPS panel.');
                args.obj.setIconClass('titleicon-warning');
                this.fireEvent("request-attention", { obj : this });
            }
        }, this);
            this.add(wpsPanel);
            this.doLayout();
            LOG.debug('setted');
        } else {
            LOG.debug('ProcessingPanel:boundsSubmitted: WPS Panel exists. Updating bounds for algorithms');
            this.get('wps-panel').updateBounds(args);
        }
    }
});
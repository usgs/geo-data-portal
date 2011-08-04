Ext.ns("GDP");

GDP.MapActivityBar = Ext.extend(Ext.Toolbar, {
    layerController : undefined,
    constructor : function(config) {
        LOG.debug('MapActivityBar: Constructing self.');
        
        if (!config) config = {};
        
        this.layerController = config.layerController;
        
        var zoomToExtentAction = new GeoExt.Action({
            text : 'Max Extent'
        })
        
        GDP.MapActivityBar.superclass.constructor.call(this, config);
    }
});
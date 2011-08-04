Ext.ns("GDP");

GDP.MapActivityBar = Ext.extend(Ext.Toolbar, {
    layerController : undefined,
    constructor : function(config) {
        LOG.debug('MapActivityBar: Constructing self.');
        
        if (!config) config = {};
        
        this.layerController = config.layerController;
        
        var map = config.map;
        var toggleGroup = 'draw';
        var zoomToExtentAction, navigationAction, vector, drawPolygonAction;
        
        vector = new OpenLayers.Layer.Vector('vector');
        zoomToExtentAction = new GeoExt.Action({
            text : 'Max Extent'
            ,control: new OpenLayers.Control.ZoomToMaxExtent()
            ,tooltip: 'Zoom To Map Extent'
            ,map: map
        });
        
        navigationAction = new GeoExt.Action({
            text: 'Nav'
            ,control: new OpenLayers.Control.Navigation()
            ,toggleGroup: toggleGroup
            ,allowDepress: false
            ,pressed: true
            ,tooltip: 'Navigate The Map'
            ,group: toggleGroup
            ,checked: true
            ,map: map
        });
        
        drawPolygonAction = new GeoExt.Action({
            text: 'Draw Poly'
            ,control: new OpenLayers.Control.DrawFeature(
                vector, OpenLayers.Handler.Polygon
                )
            ,toggleGroup: toggleGroup
            ,allowDepress: false
            ,tooltip: 'Draw A Polygon On The Map'
            ,group: toggleGroup
            ,map: map
        });
        
        config = Ext.apply({
            items: [zoomToExtentAction, navigationAction, drawPolygonAction]
        }, config);
        GDP.MapActivityBar.superclass.constructor.call(this, config);
    }
});
Ext.ns("GDP");

GDP.MapActivityBar = Ext.extend(Ext.Toolbar, {
    layerController : undefined,
    constructor : function(config) {
        LOG.debug('MapActivityBar: Constructing self.');
        
        if (!config) config = {};
        
        this.layerController = config.layerController;
        
        var zoomToExtentAction = new GeoExt.Action({
            text : 'Max Extent',
            control: new OpenLayers.Control.ZoomToMaxExtent(),
            tooltip: "zoom to max extent"
        });
        
        var navigationAction = new GeoExt.Action({
            text: "Nav",
            control: new OpenLayers.Control.Navigation(),
            toggleGroup: "draw",
            allowDepress: false,
            pressed: true,
            tooltip: "navigate",
            group: "draw",
            checked: true
        });
        
        var vector = new OpenLayers.Layer.Vector("vector");
        var drawPolygonAction = new GeoExt.Action({
        text: "draw poly",
        control: new OpenLayers.Control.DrawFeature(
            vector, OpenLayers.Handler.Polygon
        ),
        toggleGroup: "draw",
        allowDepress: false,
        tooltip: "draw polygon",
        group: "draw"
    });
        
        config = Ext.apply({
            items: [zoomToExtentAction, navigationAction]
        }, config);
        GDP.MapActivityBar.superclass.constructor.call(this, config);
    }
});
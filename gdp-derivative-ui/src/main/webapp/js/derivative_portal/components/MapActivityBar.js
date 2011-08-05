Ext.ns("GDP");

GDP.MapActivityBar = Ext.extend(Ext.Toolbar, {
    layerController : undefined,
    constructor : function(config) {
        LOG.debug('MapActivityBar:constructor: Constructing self.');
        
        if (!config) config = {};
        
        this.layerController = config.layerController;
        
        var map = config.map;
        var toggleGroup = 'draw';
        var zoomToExtentAction, navigationAction, bboxVector, drawBboxAction;
        
        bboxVector = new OpenLayers.Layer.Vector('bboxvector');
        
        drawBboxAction = new GeoExt.Action({
            text: 'Draw Box'
            ,control: new OpenLayers.Control.DrawFeature(
                bboxVector, 
                OpenLayers.Handler.Box
            )
            ,toggleGroup: toggleGroup
            ,allowDepress: false
            ,tooltip: 'Draw A Bounding Box On The Map'
            ,group: toggleGroup
            ,map: map
        });
        map.addLayers([bboxVector]);
        
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
        
        var zoomAction = new GeoExt.Action({
            text: "Zoom In"
            ,control: new OpenLayers.Control.ZoomBox({alwaysZoom: true})
            ,map: map
            ,toggleGroup: toggleGroup
            ,allowDepress: false
            ,tooltip: "Zoom In"
            ,group: toggleGroup
        });
        
        config = Ext.apply({
            items: [zoomToExtentAction, navigationAction, drawBboxAction, zoomAction]
        }, config);
        GDP.MapActivityBar.superclass.constructor.call(this, config);
        LOG.debug('MapActivityBar:constructor: Construction complete.');
    }
});
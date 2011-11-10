Ext.ns("GDP");

// TODO- The functionality of this bar is pretty fragile.  Make some effort to 
// understand how we can make multiple activity bars (for multiple accordion panels
// for example) and not have the activities overlap one another 
GDP.MapActivityBar = Ext.extend(Ext.Toolbar, {
    layerController : undefined,
    constructor : function(config) {
        LOG.debug('MapActivityBar:constructor: Constructing self.');
        
        if (!config) config = {};
        
        this.layerController = config.layerController;
        var map = config.map;
        var toggleGroup = 'draw';
        var zoomToExtentAction, navigationAction, bboxVector, drawBboxAction;
        
        bboxVector = new OpenLayers.Layer.Vector('bboxvector', {displayInLayerSwitcher : false});
        map.addLayers([bboxVector]);
        
        var control = new OpenLayers.Control();
        OpenLayers.Util.extend(control, {
            controller : this.layerController,
            handler : undefined,
            activate : function() {
                this.controller.boundingBoxButtonActivated();
            },
            draw: function() {
                this.handler = new OpenLayers.Handler.Box(
                    control,
                        {
                            done : this.notice
                        },
                        {
                            alwaysZoom : true
                        }
                    )
                this.handler.activate();
            },
            notice : function(bounds) {
                LOG.debug('MapActivityBar:drawControl:notice: bbox drawn');
                var left = map.getLonLatFromPixel(new OpenLayers.Pixel(bounds.left, bounds.top)); 
                var bottom = map.getLonLatFromPixel(new OpenLayers.Pixel(bounds.left, bounds.bottom));
                var right = map.getLonLatFromPixel(new OpenLayers.Pixel(bounds.right, bounds.bottom));
                var top = map.getLonLatFromPixel(new OpenLayers.Pixel(bounds.right, bounds.top)); 
                    
                LOG.debug('MapActivityBar:OpenLayers.Handler.Box:notice: Use drew a point and not a box');
                if (bounds.left == undefined || bounds.right == undefined || bounds.top == undefined || bounds.bottom == undefined) return;
                    
                var lonLatBounds = new OpenLayers.Bounds();
                lonLatBounds.extend(bottom);
                lonLatBounds.extend(top);
                
                this.controller.drewBoundingBox({map : this.map, bounds : bounds});
                this.controller.createGeomOverlay({bounds : lonLatBounds});
            }
        });
        
        drawBboxAction = new GeoExt.Action({
            id : 'draw-bbox-action'
            ,control: control
            ,toggleGroup: toggleGroup
            ,allowDepress: false
            ,tooltip: 'Draw A Bounding Box On The Map'
            ,group: toggleGroup
            ,map: map
            ,iconCls : 'bounding-box'
        });
        
        zoomToExtentAction = new GeoExt.Action({
            id : 'zoom-to-extent-action',
            control: new OpenLayers.Control.ZoomToMaxExtent(),
            tooltip: 'Zoom To Map Extent',
            map: map,
            iconCls : 'zoom-out'
        });
        
        navigationAction = new GeoExt.Action({
            id : 'navigation-action'
            ,iconCls : 'pan-map'
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
            id : 'zoom-action'
            ,control: new OpenLayers.Control.ZoomBox({alwaysZoom: true})
            ,map: map
            ,toggleGroup: toggleGroup
            ,allowDepress: false
            ,tooltip: "Zoom In"
            ,group: toggleGroup
            ,iconCls : 'zoom-in'
        });
        
        config = Ext.apply({
            buttonAlign: 'center',
            items: [drawBboxAction]
        }, config);
        GDP.MapActivityBar.superclass.constructor.call(this, config);
        LOG.debug('MapActivityBar:constructor: Construction complete.');
    }
});
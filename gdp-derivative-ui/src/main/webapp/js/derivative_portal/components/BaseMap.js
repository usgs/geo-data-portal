Ext.ns("GDP");

// TODO- When a legend first loads, the legend window expands.  When a new legend is requested, the legend window 
// contracts. When a new legend is loaded, the legend window does not re-expand and must be expanded manually.
// Originally I had wanted to collapse the legend window when a user changes the legend, and have it re-expand
// when the new legend image has been loaded but I'm still not able to get the timing right.

GDP.BaseMap = Ext.extend(GeoExt.MapPanel, {
    layerController : undefined,
    currentLayer : undefined,
    legendWindow : undefined,
    legendImage : undefined, 
    notificationWindow : undefined,
    DEFAULT_LEGEND_X : 110,
    DEFAULT_LEGEND_Y : 293,
    constructor : function(config) {
        LOG.debug('BaseMap:constructor: Constructing self.');
        // From GDP (with Zoerb's comments)
        // Got this number from Hollister, and he's not sure where it came from.
        // Without this line, the esri road and relief layers will not display
        // outside of the upper western hemisphere.
        var MAX_RESOLUTION = 1.40625/2;
        
        this.layerController = config.layerController;
        
        if (!config) config = {};
		
        var map = new OpenLayers.Map({
            maxResolution: MAX_RESOLUTION,
            maxExtent: new OpenLayers.Bounds(-180, -90, 180, 90),
            controls: [
            new OpenLayers.Control.MousePosition()
            ,new OpenLayers.Control.ScaleLine()
            ,new OpenLayers.Control.ArgParser()
            ,new OpenLayers.Control.Attribution()
            ,new OpenLayers.Control.PanZoomBar()
            ]
        });
			
        config = Ext.apply({
            map : map,
            center : new OpenLayers.LonLat(-96, 38),
            zoom : 4
        }, config);
                
        GDP.BaseMap.superclass.constructor.call(this, config);
        LOG.debug('BaseMap:constructor: Construction complete.');
                
        var legendImage = Ext.extend(GeoExt.LegendImage, {
            setUrl: function(url) {
                this.url = url;
                var el = this.getEl();
                if (el) {
                    el.dom.src = '';
                    el.un("error", this.onImageLoadError, this);
                    el.on("error", this.onImageLoadError, this, {
                        single: true
                    });
                    el.dom.src = url;
                }
                LOG.debug('BaseMap: Expanding legend window');
                this.ownerCt.expand(true);
            }
        });
        this.legendImage = new legendImage();
                
        this.legendWindow = new Ext.Window({
            resizable: false,
            draggable: false,
            border: false,
            frame: true,
            shadow: false,
            layout: 'absolute',
            items: [this.legendImage],
            height: this.DEFAULT_LEGEND_Y,
            width: this.DEFAULT_LEGEND_X,
            closable : false,
            collapsible : true,
            collapsed : true,
            expandOnShow : false,
            headerCfg : {
                html : 'Legend',
                cls : 'x-panel-header'
            }
        });
        this.legendWindow.show();
                
        LOG.debug('BaseMap:constructor: Registering Listeners.');
        this.layerController.on('changebaselayer', function() {
            LOG.debug('BaseMap: Observed "changebaselayer".');
            this.onReplaceBaseLayer();
        },this);
        this.layerController.on('changelayer', function() {
            LOG.debug('BaseMap: Observed "changelayer".');
            this.onChangeLayer();
        }, this);
        this.layerController.on('changedimension', function() {
            LOG.debug('BaseMap: Observed "changedimension".');
            this.onChangeDimension();
        }, this);
        this.layerController.on('changeopacity', function() {
            LOG.debug('BaseMap: Observed "changeopacity".');
            this.onChangeOpacity();
        }, this);
        this.layerController.on('changelegend', function() {
            LOG.debug('BaseMap: Observed "changelegend".');
            this.onChangeLegend();
        }, this);
        this.layerController.on('submit-bounds', function(args) {
            LOG.debug('BaseMap: Observed "submit-bounds".');
            this.createGeomOverlay(args);
        }, this);
        this.layerController.on('creategeomoverlay', function(args) {
            LOG.debug('BaseMap: Observed "creategeomoverlay".');
            this.createGeomOverlay(args);
        }, this);
        this.layerController.on('requestfoi', function(args){
            LOG.debug('BaseMap: Observed "requestfoi".');
            var existingLayer = this.layerController.getCurrentFeatureLayer();
            if (existingLayer) Ext.each(existingLayer, function(item){item.destroy()});
            var foiLayer = args;
            
            foiLayer.setVisibility(true);
//            foivectorlayer.events.on({
//                'featureselected': function(feature) {
//                    LOG.debug(feature.feature.fid);
//                    var csvToPlot = function() {
//                        var csvs = [
//                        'resources/a1b-a2.csv',
//                        'resources/kansas.csv',
//                        'resources/wisconsin.csv'
//                        ]
//                        var currentCsv = Ext.ComponentMgr.get('plotterPanel').csv;
//                        Ext.each(csvs, function(csv, index) {
//                            if (csv === currentCsv) csvs.splice(index, 1);
//                        })
//                        var chosenIndex = Math.floor(Math.random()*(csvs.length));
//                        LOG.debug("chosenIndex=" + chosenIndex);
//                        return (csvs[chosenIndex])
//                    }();
//                    
//                    this.layerController.updatePlotter({
//                        csv : csvToPlot,
//                        featureTitle : this.layerController.getDerivative().data.derivative + " - " + feature.feature.attributes.STATE
//                    });
//                    
//                    // TODO - There should be a better way of getting at this. 
//                    // Look into OpenLayers scoping for layer.on events
//                    Ext.ComponentMgr.get('mapPanel').notificationWindow.close();
//                },
//                'featureunselected': function(feature) {
//                    LOG.debug(feature.feature.fid);
//                // TODO -Remove this fid from the plotter
//                },
//                scope : this
//            }
//            );
            foiLayer.events.on({
                'added' : function(features) {
                    // Pull defaults for Ext.ux.NotifyMgr settings 
                    // to be reset later
                    var alignment = Ext.ux.NotifyMgr.alignment;
//                    
//                    // Set up the notification
                    Ext.ux.NotifyMgr.alignment = 'top-center';
//                    // TODO - There should be a better way of getting at this. 
//                    // Look into OpenLayers scoping for layer.on events
                    Ext.ComponentMgr.get('mapPanel').notificationWindow = new Ext.ux.Notify({
                        title: 'Areas Of Interest',
                        titleIconCls: 'titleicon-info',
                        hideDelay: 30000,
                     // hideDelay: 0, // Do not close until we decide to close it
                        msg: 'Click the area of interest you would like to plot an annual time series for.',
                        isClosable : true
                    }).show(document);
//                    
//                    // Reset back to default settings
                    Ext.ux.NotifyMgr.alignment = alignment;
                    if (Ext.ComponentMgr.get('mapPanel').notificationWindow) {
                        Ext.ComponentMgr.get('mapPanel').notificationWindow.close();
                    }
                    features.object.map.zoomToExtent(features.layer.getExtent());
                }
            })
            
//            var defaultStyle = new OpenLayers.Style({
//                strokeColor: "#FFFF66",
//                strokeWidth: 2,
//                strokeOpacity: 0.5,
//                fillOpacity: 0.2
//            });
//            var selectedStyle = new OpenLayers.Style({
//                strokeColor: "#00CCFF",
//                strokeWidth: 2,
//                strokeOpacity: 0.5,
//                fillOpacity: 0.2,
//                fillColor: "#0000FF"
//            });
//            foiLayer.styleMap = new OpenLayers.StyleMap({ 
//                'default' : defaultStyle,
//                'select' : selectedStyle
//            });
//            var selectorControl = new OpenLayers.Control.SLDSelect(
//                OpenLayers.Class(OpenLayers.Control, {                
//                    defaultHandlerOptions: {
//                        'single': true,
//                        'double': false,
//                        'pixelTolerance': 0,
//                        'stopSingle': false,
//                        'stopDouble': false
//                    },
//                    initialize: function(options) {
//                        this.handlerOptions = OpenLayers.Util.extend(
//                            {}, this.defaultHandlerOptions
//                        );
//                        OpenLayers.Control.prototype.initialize.apply(
//                            this, arguments
//                        ); 
//                        this.handler = new OpenLayers.Handler.Click(
//                            this, {
//                                'click': this.onClick
//                            }, this.handlerOptions
//                        );
//                    }, 
//                    onClick: function(evt) {
//                        //var output = document.getElementById(this.key + "Output");
//                        var msg = "click " + evt.xy;
//                        LOG.debug(msg);
//                    }
//                }),
//                {
//                    displayClass: 'olControlSLDSelectPoint',
//                    layers: [foivectorlayer]
//                }
//            );
            var selectorControl = new OpenLayers.Control.WMSGetFeatureInfo({
                maxFeatures : 50,
                output : 'features',
                infoFormat : 'application/vnd.ogc.gml',
                clickCallback : 'click',
                layers : [foiLayer]
            });
            selectorControl.events.register("getfeatureinfo", this, function(evt) {
               LOG.debug(evt);
               if (Ext.ComponentMgr.get('mapPanel').notificationWindow) Ext.ComponentMgr.get('mapPanel').notificationWindow.close();
               
            });
            // http://osgeo-org.1803224.n2.nabble.com/Conflict-Select-feature-vs-pan-td5935040.html
            // fixes the annoyance of not being able to pan with features displayed
            //selectorControl.handlers.feature.stopDown = false; 
            this.map.addLayers([foiLayer]);
            this.map.addControl(selectorControl);
            selectorControl.activate();
        }, this);
        this.on('resize', function() {
            this.realignLegend();
        }, this);
    },
    realignLegend : function() {
        if (this.legendWindow) {
            this.legendWindow.alignTo(this.body, "tr-tr");
        }
    },
    zoomToExtent : function(record) {
        if (!record) return;
        this.map.zoomToExtent(
            OpenLayers.Bounds.fromArray(record.get("llbbox"))
            );
    },
    findCurrentLayer : function() {
        LOG.debug('BaseMap:findCurrentLayer().');
        var storeIndex = this.layers.findBy(function(record, id) {
            return (this.layerController.getLayerOpacity() === record.get('layer').opacity);
        }, this, 1);
        if (-1 < storeIndex) {
            LOG.debug('BaseMap:findCurrentLayer():Found layer at index' + storeIndex);
            return this.layers.getAt(storeIndex);
        } else {
            LOG.debug('BaseMap:findCurrentLayer():Current layer not found');
            return null;
        }
    },
    clearLayers : function() { 
        LOG.debug('BaseMap:clearLayers: Handling request.');
        Ext.each(this.layers.data.getRange(), function(item, index, allItems){
            var layer = item.data.layer;
            if (layer.isBaseLayer || layer.CLASS_NAME === 'OpenLayers.Layer.Vector') {
                LOG.debug('BaseMap:clearLayers: Layer '+layer.id+' is a base layer and will not be cleared.');
                return;
            }                
            //TODO- This remove function should just take the layer defined above but 
            // testing shows the layer store does not remove the layer using the 
            // one defined above but this does work.
            this.layers.remove(this.layers.getById(layer.id));
            LOG.debug('BaseMap:clearLayers: Cleared layer: ' + layer.id);
        },this);
        LOG.debug('BaseMap:clearLayers: Clearing layers complete');
    },
    onChangeLayer : function() {
        LOG.debug('BaseMap:onChangeLayer: Handling request.')
            
        var layer = this.layerController.getLayer();

        if (!this.currentLayer || this.currentLayer.getLayer() !== layer) {
            this.zoomToExtent(layer);
            this.clearLayers();

            var params = {};
            Ext.apply(params, this.layerController.getAllDimensions());
            this.replaceLayer(layer, params);
        }
        this.currentLayer = this.findCurrentLayer();
    },
    onChangeDimension : function() {
        LOG.debug('BaseMap:onChangeDimension: Handling request.');
        var existingLayerIndex = this.layers.findBy(function(record, id) {
            LOG.debug(' BaseMap:onChangeDimension: Checking existing layer index.');
            var result = true;
            var requestedDimensions = this.layerController.getAllDimensions();
            Ext.iterate(requestedDimensions, function(extentName, value) {
                var layer = record.getLayer();
                if (layer.CLASS_NAME === 'OpenLayers.Layer.Vector' || layer.isBaseLayer) {
                    result = false;
                } else {
                    var existingDimension = record.getLayer().params[extentName.toUpperCase()];
                    result = result && (existingDimension === value)
                }
            }, this);
            LOG.debug(' BaseMap:onChangeDimension: Found existing layer index ' + result);
            return result;
        }, this, 0);
		
        var params = {};
        Ext.apply(params, this.layerController.getAllDimensions());
		
        this.replaceLayer(
            this.layerController.getLayer(), 
            params,
            (-1 < existingLayerIndex) ? existingLayerIndex : undefined
            );
        this.currentLayer = this.findCurrentLayer();
    },
    onChangeLegend : function() {
        LOG.debug('BaseMap:onChangeLegend: Handling Request.');
        if (!this.layerController.getLayer()) return;
        
        var legendHref = this.layerController.getLegendRecord().data.href;
        if(this.legendImage.url && this.legendImage.url.contains(legendHref)) {
            LOG.debug('BaseMap: \'changelegend\' called but legend image is already the same as requested legend.');
            return;
        }
        
        LOG.debug('BaseMap: Removing current legend image and reapplying new legend image.');
        this.legendImage.setUrl(GDP.PROXY_PREFIX + legendHref);
        var record = this.layerController.getLegendRecord();
        this.clearLayers();
        this.replaceLayer(
            this.layerController.getLayer(),
            {
                styles: record.id
            }
            );
        this.currentLayer = this.findCurrentLayer();
    },
    onChangeOpacity : function() {
        LOG.debug('BaseMap:onChangeOpacity: Handling Request.');
        if (this.currentLayer) {
            LOG.debug('BaseMap:onChangeOpacity: Current layer opacity: ' + this.currentLayer.getLayer().opacity + '. Changing to: ' + this.layerController.getLayerOpacity());
            this.currentLayer.getLayer().setOpacity(this.layerController.getLayerOpacity());
        }
    },
    onReplaceBaseLayer : function(record) {
        LOG.debug('BaseMap:onReplaceBaseLayer: Handling Request.');
        if (!record) {
            LOG.debug('BaseMap:onReplaceBaseLayer: A record object was not passed in. Using map\'s baselayer.');
            record = this.layerController.getBaseLayer()
        }
            
        var baseLayerIndex = 0;
        if (this.layers.getCount() > 0) {
            LOG.debug('BaseMap:onReplaceBaseLayer: Trying to find current base layer to remove it.');
            baseLayerIndex = this.layers.findBy(function(r, id){
                return r.data.layer.isBaseLayer
            });
                
            if (baseLayerIndex > -1 ) {
                this.layers.removeAt(baseLayerIndex);
                LOG.debug('BaseMap:onReplaceBaseLayer: Removed base layer from this object\'s map.layers at index ' + baseLayerIndex);
            } else {
                // Not sure why this would happen
                LOG.debug('BaseMap:onReplaceBaseLayer: Base layer not found.');
            }
        }
            
        this.layers.add([record]);
        LOG.debug('BaseMap:onReplaceBaseLayer: Added base layer to this object\'s map.layers at index ' + baseLayerIndex);
    },
    replaceLayer : function(record, params, existingIndex) {
        LOG.debug('BaseMap:replaceLayer: Handling request.');
        if (!record) return;
        if (!params) {
            params = {};
        }
		
        if (this.currentLayer) {
            var layer = this.currentLayer.getLayer();
            layer.setOpacity(0.0); // This will effectively hide the current layer
            LOG.debug('BaseMap:replaceLayer: Hiding current layer');
        }
                
        if (existingIndex) {
            LOG.debug('BaseMap:replaceLayer: Replacing current layer with already-existing layer at index ' + existingIndex);
            var existingLayer = this.layers.getAt(existingIndex);
            this.currentLayer = existingLayer;
            this.onChangeOpacity();
        } else {
            LOG.debug('BaseMap:replaceLayer: Replacing current layer with a new layer.');
            var copy = record.clone();

            params = Ext.apply({
                format: "image/png",
                transparent : true,
                styles : (params.styles) ? params.styles : this.layerController.getLegendRecord().id
            }, params);

            copy.get('layer').mergeNewParams(params);
            copy.getLayer().setOpacity(this.layerController.getLayerOpacity());
            copy.get('layer')['url'] = GDP.PROXY_PREFIX + copy.get('layer')['url'];
            copy.getLayer().events.register('loadend', this, function() {
                if (LOADMASK) LOADMASK.hide();
            });
            this.layers.add(copy);
        }
        
    },
    createGeomOverlay : function(args) {
        LOG.debug('BaseMap:createGeometryOverlay: Drawing vector')
        var bounds = args.bounds;
        var geom = bounds.toGeometry();
        var feature = new OpenLayers.Feature.Vector(geom, {
            id : 'draw-vector'
        });
            
        this.map.getLayersByName('bboxvector')[0].removeAllFeatures(null,true);
        this.map.getLayersByName('bboxvector')[0].addFeatures([feature]);
        this.map.zoomToExtent(bounds,true);
    }
});

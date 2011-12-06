Ext.ns("GDP");

GDP.BaseMap = Ext.extend(GeoExt.MapPanel, {
    // From GDP (with Zoerb's comments):
    // Got this number from Hollister, and he's not sure where it came from.
    // Without this line, the esri road and relief layers will not display
    // outside of the upper western hemisphere.
    MAX_RESOLUTION : 1.40625/2, //0.703125
    
    DEFAULT_LEGEND_X : 110,
    DEFAULT_LEGEND_Y : 293,
    
    changeProdToggleButton : undefined, 
    currentLayer : undefined,
    baseLayerCombo : undefined,
    expandContractButton : undefined,
    infoButton : undefined,
    infoText : undefined,
    layerController : undefined,
    layerOpacitySlider : undefined,
    legendCombo : undefined,
    legendImage : undefined, 
    legendSwitch : undefined,
    legendWindow : undefined,
    notificationWindow : undefined,
    constructor : function(config) {
        LOG.debug('BaseMap:constructor: Constructing self.');
        
        this.layerController = config.layerController;
        
        if (!config) config = {};

        // Set up the map and controls on the map
        var map = new OpenLayers.Map({
            maxResolution: this.MAX_RESOLUTION,
            controls: [
            new OpenLayers.Control.MousePosition(),
            new OpenLayers.Control.ScaleLine(),
            new OpenLayers.Control.PanZoomBar({
                panIcons : false,
                position : new OpenLayers.Pixel(3,30)
            })
            ]
        });

        // Set up the map control panel
        var navigationCtrl = new OpenLayers.Control.Navigation({
            title:'You can use the default mouse configuration',
            autoActivate : true
        });
        var mapControlPanel = new OpenLayers.Control.Panel({
            defaultControl: navigationCtrl
        });
        mapControlPanel.addControls([
            navigationCtrl, 
            new OpenLayers.Control.ZoomBox({
                title:"Zoom box: Selecting it you can zoom on an area by clicking and dragging."
            })
        ]);                
        map.addControl(mapControlPanel);
        
        // Set up the toolbar above the map
        this.baseLayerCombo = new Ext.form.ComboBox({
            id : 'baseLayerCombo',
            xtype : 'combo',
            mode : 'local',
            triggerAction: 'all',
            fieldLabel : 'Base Layer',
            forceSelection : true,
            lazyInit : false,
            displayField : 'title',
            editable : false,
            emptyText : 'Loading...',
            autoSelect : false, // Value is programatically selected on store load
            store : config.baseLayerStore,
            listeners : {
                select : function(combo, record) {
                    LOG.debug('BaseMap: Base Layer Combo Box ' + combo.getEl().id + ' observed select.');
                    this.layerController.requestBaseLayer(record);
                },
                scope : this
            }
        });
        
        this.changeProdToggleButton = new Ext.Button({
            itemId : 'changeProdToggleButton',
            id : 'changeProdToggleButton',
            text : 'Show Change From Historical Period',
            ref : 'change-product-toggle-button',
            pressed : false,
            enableToggle: true,
            listeners : {
                click : function(button) {
                    this.layerController.onChangeProductToggled(button.pressed);
                },
                scope : this
            }
        })
        
        this.legendSwitch = new Ext.Button({
            text : 'Off',
            pressed: true,
            enableToggle: true,
            listeners : {
                click : function() {
                    if (this.legendWindow.hidden) {
                        this.legendWindow.show();
                        this.legendSwitch.setText('Off');
                        this.legendSwitch.pressed = true;
                    } else {
                        this.legendWindow.hide();
                        this.legendSwitch.setText('On');
                        this.legendSwitch.pressed = false;
                    }
                },
                scope : this
            }
        });
        this.legendCombo = new Ext.form.ComboBox({
            xtype : 'combo',
            mode : 'local',
            triggerAction: 'all',
            store : this.layerController.getLegendStore(),
            forceSelection : true,
            lazyInit : false,
            displayField : 'title',
            editable : false,
            emptyText : 'Loading...',
            listeners : {
                select : function(obj, rec) {
                    LOG.debug('BaseMap: A new legend style chosen: ' + rec.id + ' (' + rec.data.abstrakt + ')');
                    this.layerController.requestLegendRecord(rec);
                },
                scope : this
            }
        });
        this.legendCombo.store.on('load', function(store) {
            LOG.debug('BaseMap: Legend Combobox store Loaded.');
            //  http://internal.cida.usgs.gov/jira/browse/GDP-372
            var recordIndex = store.find('name', GDP.DEFAULT_LEGEND_NAME);
            recordIndex = (recordIndex < 0) ? 0 : recordIndex;
            this.legendCombo.setValue(store.getAt(recordIndex).get('name'));
        }, this);
        this.layerOpacitySlider = new Ext.slider.SingleSlider({
            value : this.layerController.getLayerOpacity() * 100,
            width: 100,
            plugins: new GeoExt.LayerOpacitySliderTip({
                template: '<div>Opacity: {opacity}%</div>'
            })
        });
        
        this.infoButton = new Ext.Button({
            itemId : 'infoButton',
            id : 'infoButton',
            text : 'INFO',
            ref : '../toolbar-info-button',
            hidden: true,
            listeners: {
                click : function() {
                    var win = new Ext.Window({
                        id : 'infowindow',
                        width:640,
                        shadow : true,
                        title:'USGS Derived Downscaled Climate Projection Portal Information',
                        autoScroll:true,
                        modal:true,
                        floating : true
                    });
            
                    var infoDivId = 'information_div';
                    var infoDiv = {
                        id: infoDivId,
                        tag:'div',
                        width:'100%',
                        height:'100%'
                    }
                    win.show();
                    Ext.DomHelper.insertFirst(win.body, infoDiv);
                    Ext.DomHelper.append(Ext.DomQuery.selectNode('div[id="'+infoDivId+'"]'), this.infoText);
                },
                scope : this
            }
        });
        this.expandContractButton = new Ext.Button({
           itemId : 'expandContractButtion',
           id : 'expandContractButton',
           text : 'Expand',
           ref : '../expandContractButton',
           enableToggle: true,
           listeners : {
               click : function(button) {
                   var expandContractText = button.pressed ? 'Contract' : 'Expand';
                   LOG.debug('BaseMap:expandContractButton:Clicked: User wants to '+expandContractText+' application view');
                   button.setText(expandContractText)
                   this.layerController.requestApplicationResize(button.pressed);
               },
               scope : this
           }
        });
        var toolbar = new Ext.Toolbar({
            items : [
            this.infoButton,
            this.expandContractButton,
            '->',
            this.changeProdToggleButton,
            ' ' ,
            '-',
            ' ',
            'Opacity: ',
            this.layerOpacitySlider,
            ' ',
            '-',
            ' ',
            'Base Layer: ',
            this.baseLayerCombo,
            ' ',
            '-',
            ' ',
            'Legend: ',
            this.legendSwitch,
            this.legendCombo
            ]
        })
        LOG.debug('DatasetConfigPanel:constructor: Registering listeners.');
        config = Ext.apply({
            map : map,
            center : new OpenLayers.LonLat(-96, 38),
            tbar : toolbar,
            bufferResize : true
        }, config);
                
        GDP.BaseMap.superclass.constructor.call(this, config);
        LOG.debug('BaseMap:constructor: Construction complete.');
        
        LOG.debug('BaseMap:constructor: Registering Listeners.');
        this.layerController.on('updateplotter', function(){
            this.infoButton.show();
        }, this)
        this.layerController.on('changebaselayer', function() {
            LOG.debug('BaseMap: Observed "changebaselayer".');
            this.baseLayerCombo.setValue(this.layerController.getBaseLayer().data.title);
        }, this);
        this.layerOpacitySlider.on('change', function() {
            LOG.debug('BaseMap:layerOpacitySlider: Observed \'change\'.');
            this.layerController.requestOpacity(this.layerOpacitySlider.getValue() / 100);
        }, this, {
            buffer: 5
        });
        this.layerController.on('loaded-catstore', function(args) {
            LOG.debug('BaseMap:onLoadedCatstore');
            this.onLoadedCatstore(args);
        }, this);
        this.layerController.on('changebaselayer', function() {
            LOG.debug('BaseMap: Observed "changebaselayer".');
            this.onReplaceBaseLayer();
        },this);
        this.layerController.on('changelayer', function() {
            LOG.debug('BaseMap: Observed "changelayer".');
            this.onChangeLayer();
        }, this);
        this.layerController.on('changedimension', function(extentName) {
            LOG.debug('BaseMap: Observed "changedimension".');
            this.onChangeDimension(extentName);
        }, this);
        this.layerController.on('changeopacity', function() {
            LOG.debug('BaseMap: Observed "changeopacity".');
            this.onChangeOpacity();
        }, this);
        this.layerController.on('changelegend', function() {
            LOG.debug('BaseMap: Observed "changelegend".');
            this.onChangeLegend();
        }, this);
        this.layerController.on('requestfoi', function(args){
            LOG.debug('BaseMap: Observed "requestfoi".');
            
            // Clean up befre re-adding layers and controls
            Ext.each(this.map.getLayersByName('foilayer'), function(item){
                item.destroy();
            });
            
            Ext.each(this.map.getLayersByName('selectedFeature'), function(item){
                item.destroy();
            });
            
            Ext.each(this.map.getControlsByClass('OpenLayers.Control.WMSGetFeatureInfo'), function(item){
                item.deactivate();
                this.removeControl(item);
            }, this.map);
            
            
            var foiLayer = args.clone().getLayer();
            var highestZ = this.getHighestZIndex();
            foiLayer.setZIndex(highestZ + 1);
            foiLayer.displayInLayerSwitcher = false;
            foiLayer.setVisibility(true);
            foiLayer.name = "foilayer";
            foiLayer.events.on({
                'added' : function(features) {
                    // Pull defaults for Ext.ux.NotifyMgr settings to be reset later
                    var alignment = Ext.ux.NotifyMgr.alignment;
                    var offsets = Ext.ux.NotifyMgr.offsets;
                    
                    // Set up the notification
                    // http://internal.cida.usgs.gov/jira/browse/GDP-425
                    Ext.ux.NotifyMgr.alignment = 'top-left';
                    var tooltipTopPosition = Ext.ComponentMgr.get('plotFieldSet').getPosition()[1] + Ext.ComponentMgr.get('plotFieldSet').getHeight() + 5;
                    Ext.ux.NotifyMgr.offsets = [tooltipTopPosition,10];
                    
                    // TODO - There should be a better way of getting at this. 
                    // Look into OpenLayers scoping for layer.on events
                    if (!Ext.ComponentMgr.get('mapPanel').notificationWindow) {
                        Ext.ComponentMgr.get('mapPanel').notificationWindow = new Ext.ux.Notify({
                            title: 'Areas Of Interest',
                            titleIconCls: 'titleicon-info',
                            hideDelay: 30000,
                            msg: 'Click the area of interest you would like to plot an annual time series for.',
                            isClosable : true
                        }).show(document);
                    }

                    // Reset back to default settings
                    Ext.ux.NotifyMgr.alignment = alignment;
                    Ext.ux.NotifyMgr.offsets = offsets;
                    features.object.map.zoomToExtent(features.layer.getExtent());
                }
            })
            
            var selectorControl = new OpenLayers.Control.WMSGetFeatureInfo({
                id : 'clickcontrol',
                maxFeatures : 1,
                output : 'features',
                infoFormat : 'application/vnd.ogc.gml',
                clickCallback : 'click',
                layers : [foiLayer],
                url : foiLayer.url
            });
            
            selectorControl.events.register("getfeatureinfo", this, function(evt) {
                if (!evt.features[0]) return; // User clicked somewhere they shouldn't have
                
                Ext.each(this.map.getLayersByName('selectedFeature'), function(item){
                    item.destroy();
                })
                
                // http://internal.cida.usgs.gov/jira/browse/GDP-423
                // http://internal.cida.usgs.gov/jira/browse/GDP-435
                evt.features[0].attributes.TITLE = function() {
                    var fid = evt.object.layers[0].params.LAYERS.toLowerCase();
                    if (fid == 'derivative:conus_states') return evt.features[0].attributes.STATE;
                    if (fid == 'derivative:us_counties') return evt.features[0].attributes.FIPS;
                    if (fid == 'derivative:level_iii_ecoregions') return evt.features[0].attributes.LEVEL3_NAM;
                    if (fid == 'derivative:wbdhu8_alb_simp') return evt.features[0].attributes.HUC_8;
                    if (fid == 'derivative:fws_lcc') return evt.features[0].attributes.area_names;
                    if (fid == 'derivative:nca_regions') return evt.features[0].attributes.NCA_Region;
                    return '';
                }();
                
                this.layerController.featureTitle = function() {
                    var fid = evt.object.layers[0].params.LAYERS.toLowerCase();
                    if (fid == 'derivative:wbdhu8_alb_simp') return evt.features[0].attributes.SUBBASIN;
                    if (fid == 'derivative:us_counties') return evt.features[0].attributes.COUNTY;
                    return evt.features[0].attributes.TITLE;
                }();
                this.layerController.featureAttribute = evt.features[0].attributes.TITLE;
                this.layerController.updatePlotter();
                
                var selectedLayer = new OpenLayers.Layer.Vector(
                    "selectedFeature",
                    {
                        styleMap : new OpenLayers.StyleMap({
                            'default' : new OpenLayers.Style({
                                strokeColor: "#15428b",
                                strokeWidth: 2,
                                strokeOpacity: 0.5,
                                fillOpacity: 0.2,
                                fillColor: "#98c0f4"
                            })
                        }),
                        displayInLayerSwitcher : false
                    }
                    );
                selectedLayer.addFeatures(evt.features);
                
                this.map.addLayers([selectedLayer]);
                
                // TODO - There should be a better way of getting at this. 
                // Look into OpenLayers scoping for layer.on events
                if (Ext.ComponentMgr.get('mapPanel').notificationWindow) {
                    Ext.ComponentMgr.get('mapPanel').notificationWindow.animHide();
                    Ext.ComponentMgr.get('mapPanel').notificationWindow = undefined;
                }
                var highestZ = this.getHighestZIndex();
                this.map.getLayersByName('foilayer')[0].setZIndex(highestZ + 1);
                selectedLayer.setZIndex(highestZ + 2);
            });
            
            this.map.addLayers([foiLayer]);
            this.map.addControl(selectorControl);
            selectorControl.activate();
        }, this);
        
        this.on('resize', function() {
            this.realignLegend();
        }, this);
        
        this.createLegendImage();
    },
    createLegendImage : function() {
        LOG.debug('BaseMap: Setting up legend window.');
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
        this.legendImage.on('afterrender', function() {
            (function() {
                if(LOADMASK) LOADMASK.hide();
            }).defer(2000);
        });
        this.legendWindow = new Ext.Window({
            resizable: false,
            draggable: false,
            border: false,
            frame: false,
            shadow: false,
            layout: 'absolute',
            items: [this.legendImage],
            height: this.DEFAULT_LEGEND_Y,
            width: this.DEFAULT_LEGEND_X,
            closable : false,
            collapsible : false,
            collapsed : true,
            expandOnShow : false
        });
        this.legendWindow.show();
    },
    onLoadedCatstore : function(args) {
        this.infoText = args.record.get('helptext')['plotWindowIntroText'];
        this.changeProdToggleButton.setTooltip(args.record.get('helptext')['changeProdText']);
    },
    getHighestZIndex : function() {
        var highestZIndex = 0;
        Ext.each(this.map.layers, function(item) {
            if (highestZIndex < item.getZIndex()) highestZIndex = item.getZIndex();
        }, highestZIndex)
        return highestZIndex;
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
        Ext.each(this.layers.data.getRange(), function(item){
            var layer = item.data.layer;
            if (layer.isBaseLayer || layer.CLASS_NAME === 'OpenLayers.Layer.Vector' || layer.name === 'foilayer') {
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
    onChangeDimension : function(extentName) {
        LOG.debug('BaseMap:onChangeDimension: Handling request.');
        var existingLayerIndex = this.layers.findBy(function(record, id) {
            LOG.debug(' BaseMap:onChangeDimension: Checking existing layer index.');
            var result = true;
            var requestedDimensions = this.layerController.getAllDimensions();
            Ext.iterate(requestedDimensions, function(extentName, value) {
                var layer = record.getLayer();
                if (layer.CLASS_NAME === 'OpenLayers.Layer.Vector' || layer.isBaseLayer) {
                    result = false;
                }
                else {
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
        //        this.redraw();
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
            

            copy.get('layer').displayInLayerSwitcher = false;
            copy.get('layer').mergeNewParams(params);
            copy.getLayer().setOpacity(this.layerController.getLayerOpacity());
            copy.get('layer')['url'] = GDP.PROXY_PREFIX + copy.get('layer')['url'];
            copy.getLayer().events.register('loadend', this, function() {
                //                if (LOADMASK) LOADMASK.hide();
                });
            this.layers.add(copy);
            
            var foilayer = this.map.getLayersByName('foilayer');
            var selectedLayer = this.map.getLayersByName('selectedlayer');
            var highestZ = this.getHighestZIndex();
            if (foilayer.length) {
                foilayer[0].setZIndex(highestZ + 1);
            }
            if (selectedLayer.length) {
                selectedLayer[0].setZIndex(highestZ + 2);
            }
            
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

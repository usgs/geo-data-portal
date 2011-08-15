Ext.ns("GDP");

GDP.LayerChooser = Ext.extend(Ext.form.FormPanel, {
    controller : undefined,
    legendCombo : undefined,
    constructor : function(config) {
        LOG.debug('LayerChooser:constructor: Constructing self.');
        
        if (!config) config = {};

        this.controller = config.controller || new GDP.LayerController({});

        LOG.trace('LayerChooser:constructor: Constructing base layer combo box.');
        var baseLayerStore = config.baseLayerStore;
        var baseLayerCombo = new Ext.form.ComboBox({
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
            store : baseLayerStore
        });
        
//        var capabilitiesStore = config.capabilitiesStore;
//        var layerCombo = new Ext.form.ComboBox({
//            xtype : 'combo',
//            mode : 'local',
//            triggerAction: 'all',
//            store : capabilitiesStore,
//            fieldLabel : 'Layer',
//            forceSelection : true,
//            lazyInit : false,
//            editable : false,
//            displayField : 'title',
//            emptyText : 'Loading...'
//        });

//        var zlayerCombo, zlayerName, zlayerStore, zlayerComboConfig;
//        zlayerName = 'elevation';
//        zlayerStore = new Ext.data.ArrayStore({
//            storeId : 'zlayerStore',
//            idIndex: 0,
//            fields: [zlayerName]
//        });
//        zlayerComboConfig = {
//            mode : 'local',
//            triggerAction: 'all',
//            store : zlayerStore,
//            forceSelection : true,
//            lazyInit : false,
//            displayField : zlayerName,
//            emptyText : 'Loading...',
//            autoWidth : true
//        };

//        this.legendCombo = new Ext.form.ComboBox({
//            xtype : 'combo'
//            ,mode : 'local'
//            ,triggerAction: 'all'
//            ,store : this.controller.getLegendStore()
//            ,fieldLabel : 'Legend'
//            ,forceSelection : true
//            ,lazyInit : false
//            ,displayField : 'title'
//            ,editable : false
//            ,emptyText : 'Loading...'
//        });
//        
//        var layerOpacitySlider = new Ext.Slider({
//            value : 40,
//            fieldLabel: "Opacity",
//            plugins: new GeoExt.LayerOpacitySliderTip({
//                template: '<div>Opacity: {opacity}%</div>'
//            })
//        });
//
//        Ext.iterate([baseLayerCombo, this.legendCombo, layerOpacitySlider], function(item) {
//            item.on('added', function(me, parent){ me.setWidth(parent.width - 5); })
//        })
//
//        var activityBar = new GDP.MapActivityBar({
//                id : 'activityBar'
//                ,map : config.map
//                ,layerController : this.controller
//        });

        config = Ext.apply({
            labelAlign : 'top',
            items : [
//            activityBar
            ,baseLayerCombo
//            ,layerCombo
            ,this.legendCombo
//            ,layerOpacitySlider
            ]
        }, config);
        
        GDP.LayerChooser.superclass.constructor.call(this, config);
        LOG.debug('LayerChooser:constructor: Construction complete.');
        this.doLayout();
        
        LOG.debug('LayerChooser:constructor: Registering Listeners.');
        {
//            baseLayerCombo.on('select', function(combo, record, index) {
//                LOG.info('EVENT: ' + combo.getEl().id + ' observed select.');
//                this.controller.requestBaseLayer(record);
//            }, this);
//            layerCombo.on('select', function(combo, record, index) {
//                this.controller.requestLayer(record);
//            }, this);
//            capabilitiesStore.on('load', function(capStore, records) {
//                LOG.debug('root: Capabilities store has finished loading.');
//                var firstRecord = capStore.getAt(0);
//                layerCombo.setValue(firstRecord.get("title"));
//                
//                // Let's use this event to load the legendstore/combobox
//                this.controller.modifyLegendStore(firstRecord.data);
//                
//                layerCombo.fireEvent('select', layerCombo, firstRecord, 0);
//                if (LOADMASK) LOADMASK.hide();
//            }, this);
//            this.legendCombo.on('select', function(obj, rec, ind) {
//                LOG.debug('LayerChooser: A new legend style chosen: ' + rec.id + ' (' + rec.data.abstrakt + ')');
//                this.controller.requestLegendRecord(rec);
//            },this);
//            this.legendCombo.store.on('load', function(store) {
//                LOG.debug('LayerChooser: Legend Combobox store Loaded.');
//                
//                //  http://internal.cida.usgs.gov/jira/browse/GDP-372
//                // TODO - This is duplicated in LayerChoose @ LayerController.modifyLegendStore() -- Fix that.
//                var recordIndex = store.find('name', GDP.DEFAULT_LEGEND_NAME);
//                recordIndex = (recordIndex < 0) ? 0 : recordIndex;
//                this.legendCombo.setValue(store.getAt(recordIndex).get('name'));
//            }, this);
//            layerOpacitySlider.on('change', function() {
//                LOG.debug('layerOpacitySlider: Observed \'change\'.');
//                this.controller.requestOpacity(layerOpacitySlider.getValue() / 100);
//            }, this, {
//                buffer: 5
//            });
//            this.controller.on('changebaselayer', function() {
//                LOG.debug('LayerChooser: Observed \'changebaselayer\'.');
//                baseLayerCombo.setValue(this.controller.getBaseLayer().data.title);
//            }, this);
//            this.controller.on('changelayer', function() {
//                LOG.debug('LayerChooser: Observed \'changelayer\'.');
//                
//                var layer = this.controller.getLayer();
//                if (layer) {
////                    layerCombo.setValue(layer.getLayer().name);
//                    this.controller.modifyLegendStore(layer.data);
//                }
//
//                if (zlayerCombo) {
//                    this.remove(zlayerCombo)
//                }
//                var loaded = this.controller.loadDimensionStore(layer, zlayerStore, zlayerName);
//
//                if (loaded) {
//                    var threshold = this.controller.getDimension(zlayerName);
//                    if (threshold) {
//                        zlayerCombo = new Ext.form.ComboBox(Ext.apply({
//                            fieldLabel : this.controller.getZAxisName(),
//                            editable : false
//                        }, zlayerComboConfig));
////                        zlayerCombo.on('added', function(me, parent, index){ me.setWidth(parent.width); })
//                        this.add(zlayerCombo);
//                        zlayerCombo.setValue(threshold);
//                        zlayerCombo.on('select', function(combo, record, index) {
//                            this.controller.requestDimension(zlayerName, record.get(zlayerName));
//                        }, this);
//                        this.doLayout();
//                    }
//                }
//
//            }, this);
//            this.controller.on('changedimension', function() {
//                LOG.debug('LayerChooser: Observed \'changedimension\'.');
//                var threshold = this.controller.getDimension(zlayerName);
//                if (threshold & zlayerCombo) {
//                    zlayerCombo.setValue(threshold);
//                }
//            }, this);
//            this.controller.on('bboxbuttonactivated', function(args){
//                LOG.debug('LayerChooser: Observed "bboxbuttonactivated"');
//                if (this.get('coord-panel')) {
//                    LOG.debug('LayerChooser: Coordinate panel found. Reusing.');
//                    var coords = this.get('coord-panel').getCoords();
//                    if (coords.west && coords.south && coords.east && coords.north) {
//                        LOG.debug('LayerChooser: Coordinate panel has all 4 coordinates populated. Drawing and snapping to polygon.');
//                        
//                        var lonLatBounds = new OpenLayers.Bounds();
//                        lonLatBounds.extend(new OpenLayers.LonLat(coords.west, coords.south));
//                        lonLatBounds.extend(new OpenLayers.LonLat(coords.east, coords.north));
//
//                        this.controller.createGeomOverlay({bounds : lonLatBounds});
//                    }
//                } else {
//                    LOG.debug('LayerChooser: Coordinate panel not found. Reconstructing panel.');
//                    var poiPanelConfig = {
//                        id : 'coord-panel',
//                        submitButton : new GDP.BoundsPanelSubmitButton({
//                            layerController : this.controller
//                        })
//                    }
//                    var coordPanel = new GDP.PolygonPOIPanel(poiPanelConfig);
//                    this.add(coordPanel);
//                    this.doLayout(true);
//                    coordPanel.setWidth(this.getWidth());
//                }
//            }, this);
//            this.controller.on('drewbbox', function(args){
//                LOG.debug('LayerChooser: Observed "drewbbox"');
//                var bounds = args.bounds;
//                var map = args.map;
//                var west = map.getLonLatFromPixel(new OpenLayers.Pixel(bounds.left, bounds.top)); 
//                var south = map.getLonLatFromPixel(new OpenLayers.Pixel(bounds.left, bounds.bottom));
//                var east = map.getLonLatFromPixel(new OpenLayers.Pixel(bounds.right, bounds.bottom));
//                var north = map.getLonLatFromPixel(new OpenLayers.Pixel(bounds.right, bounds.top)); 
//                
//                // ul, br
//                if (this.get('coord-panel')) {
//                    this.get('coord-panel').setCoords({
//                        west : west,
//                        south : south,
//                        east : east,
//                        north : north
//                    });
//                } else {
//                    var poiPanelConfig = {
//                        id : 'coord-panel',
//                        west : west,
//                        south : south,
//                        east : east,
//                        north : north,
//                        submitButton : new GDP.BoundsPanelSubmitButton({
//                            layerController : this.controller
//                        })
//                    }
//                    var coordPanel = new GDP.PolygonPOIPanel(poiPanelConfig);
//                    this.add(coordPanel);
//                    this.doLayout(true);
//                    coordPanel.setWidth(this.getWidth());
//                }
//            }, this);
//            this.controller.on('submit-bounds',function(args){
//                LOG.debug('LayerChooser: Observed "submit-bounds"');
//                
//                var wpsPanel = new GDP.WPSPanel(args);
//                this.add(wpsPanel);
//                this.doLayout(true);
//                wpsPanel.setWidth(this.getWidth());
//            },this);
        }
//        this.on('resize', function() {
//            this.get('activityBar').setWidth(this.getWidth());
//        }, this);
    }
});
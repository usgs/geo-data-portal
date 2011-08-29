Ext.ns("GDP");

/**
 * This panel is a holder for all of the controls related to the map (opacity, base layer, toolset, etc)
 */
GDP.MapConfigPanel = Ext.extend(Ext.Panel, {
    controller : undefined,
    baselayerStore : undefined,
    baseLayerCombo : undefined,
    legendCombo : undefined,
    layerOpacitySlider : undefined,
    instructionPanel : undefined,
    constructor : function(config) {
        LOG.debug('MapConfigPanel:constructor: Constructing self.');

        this.controller = config.controller;
        
        this.activityBar = new GDP.MapActivityBar({
            id : 'activityBar',
            map : config.map,
            layerController : this.controller
        });
        
        this.baseLayerStore = config.baseLayerStore;
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
            store : this.baseLayerStore
        });
        
        this.legendCombo = new Ext.form.ComboBox({
            xtype : 'combo'
            ,mode : 'local'
            ,triggerAction: 'all'
            ,store : this.controller.getLegendStore()
            ,fieldLabel : 'Legend'
            ,forceSelection : true
            ,lazyInit : false
            ,displayField : 'title'
            ,editable : false
            ,emptyText : 'Loading...'
        });
        
        this.layerOpacitySlider = new Ext.Slider({
            value : 40,
            fieldLabel: "Opacity",
            plugins: new GeoExt.LayerOpacitySliderTip({
                template: '<div>Opacity: {opacity}%</div>'
            })
        });
        
        this.instructionPanel =  new Ext.Panel({
            html:'To access the underlying data, select the Draw a Bounding Box tool and select your area of interest', 
            border : false
        })
        
        Ext.iterate([this.baseLayerCombo, this.legendCombo, this.layerOpacitySlider], function(item) {
            item.on('added', function(me, parent){ me.setWidth(parent.width - 5); })
        })
        
        config = Ext.apply({
            id : 'map-configuration-panel',
            labelAlign : 'top',
            items : [
                this.activityBar, 
                this.baseLayerCombo,
                this.legendCombo,
                this.layerOpacitySlider,
                this.instructionPanel
            ],
            layout : 'form',
            title : 'Map Configuration',
            width : config.width || undefined
        }, config);
        GDP.MapConfigPanel.superclass.constructor.call(this, config);
        LOG.debug('MapConfigPanel:constructor: Construction complete.');
        
        LOG.debug('MapConfigPanel:constructor: Registering listeners.');
        this.baseLayerCombo.on('select', function(combo, record, index) {
            LOG.debug('MapConfigPanel: Base Layer Combo Box ' + combo.getEl().id + ' observed select.');
            this.controller.requestBaseLayer(record);
        }, this);
        this.legendCombo.on('select', function(obj, rec, ind) {
            LOG.debug('MapConfigPanel: A new legend style chosen: ' + rec.id + ' (' + rec.data.abstrakt + ')');
            this.controller.requestLegendRecord(rec);
        },this);
        this.legendCombo.store.on('load', function(store) {
            LOG.debug('MapConfigPanel: Legend Combobox store Loaded.');
            this.onLegendComboStoreLoad(store);
        }, this);
        this.layerOpacitySlider.on('change', function() {
            LOG.debug('MapConfigPanel:layerOpacitySlider: Observed \'change\'.');
            this.controller.requestOpacity(this.layerOpacitySlider.getValue() / 100);
        }, this, {
            buffer: 5
        });
        this.controller.on('changebaselayer', function() {
            LOG.debug('MapConfigPanel: Observed "changebaselayer".');
            this.baseLayerCombo.setValue(this.controller.getBaseLayer().data.title);
        }, this);
        this.controller.on('bboxbuttonactivated', function(){
            LOG.debug('MapConfigPanel: Observed "bboxbuttonactivated"');
            this.bboxButtonActivated();
        }, this);
        this.controller.on('drewbbox', function(args){
            LOG.debug('LayerChooser: Observed "drewbbox"');
            this.drewBbox(args);
        }, this);
    },
    onLegendComboStoreLoad : function(store) {
        //  http://internal.cida.usgs.gov/jira/browse/GDP-372
        var recordIndex = store.find('name', GDP.DEFAULT_LEGEND_NAME);
        recordIndex = (recordIndex < 0) ? 0 : recordIndex;
        this.legendCombo.setValue(store.getAt(recordIndex).get('name'));
    },
    bboxButtonActivated : function() {
        if (this.get('coord-panel')) {
            LOG.debug('MapConfigPanel: Coordinate panel found. Reusing.');
            var coords = this.get('coord-panel').getCoords();
            if (coords.west && coords.south && coords.east && coords.north) {
                LOG.debug('MapConfigPanel: Coordinate panel has all 4 coordinates populated. Drawing and snapping to polygon.');

                var lonLatBounds = new OpenLayers.Bounds();
                lonLatBounds.extend(new OpenLayers.LonLat(coords.west, coords.south));
                lonLatBounds.extend(new OpenLayers.LonLat(coords.east, coords.north));

                this.controller.createGeomOverlay({bounds : lonLatBounds});
            }
        } else {
            LOG.debug('MapConfigPanel: Coordinate panel not found. Reconstructing panel.');
            var poiPanelConfig = {
                id : 'coord-panel',
                submitButton : new GDP.BoundsPanelSubmitButton({
                    layerController : this.controller
                })
            }
            var coordPanel = new GDP.PolygonPOIPanel(poiPanelConfig);
            this.add(coordPanel);
            this.doLayout(true);
            coordPanel.setWidth(this.getWidth());
        }
    },
    drewBbox : function(args) {
        var bounds = args.bounds;
        var map = args.map;
        var west = map.getLonLatFromPixel(new OpenLayers.Pixel(bounds.left, bounds.top)); 
        var south = map.getLonLatFromPixel(new OpenLayers.Pixel(bounds.left, bounds.bottom));
        var east = map.getLonLatFromPixel(new OpenLayers.Pixel(bounds.right, bounds.bottom));
        var north = map.getLonLatFromPixel(new OpenLayers.Pixel(bounds.right, bounds.top)); 

        // ul, br
        if (this.get('coord-panel')) {
            this.get('coord-panel').setCoords({
                west : west,
                south : south,
                east : east,
                north : north
            });
        } else {
            var poiPanelConfig = {
                id : 'coord-panel',
                west : west,
                south : south,
                east : east,
                north : north,
                submitButton : new GDP.BoundsPanelSubmitButton({
                    layerController : this.controller
                })
            }
            var coordPanel = new GDP.PolygonPOIPanel(poiPanelConfig);
            this.add(coordPanel);
            this.doLayout(true);
            coordPanel.setWidth(this.getWidth());
        }
    }
});
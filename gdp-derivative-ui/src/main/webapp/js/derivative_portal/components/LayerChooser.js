Ext.ns("GDP");

GDP.LayerChooser = Ext.extend(Ext.form.FormPanel, {
    controller : undefined,
    legendWindow : undefined,
    legendImage : undefined, 
    DEFAULT_LEGEND_X : 110,
    DEFAULT_LEGEND_Y : 274,
    realignLegend : function() {
        if (this.legendWindow) {this.legendWindow.alignTo(this.getEl(), "br-br");}
    },
    constructor : function(config) {
        LOG.debug('LayerChooser: Constructing self.');
        if (!config) config = {};

        this.controller = config.controller || new GDP.LayerController({});

        var baseLayerStore = config.baseLayerStore;
        var baseLayerCombo = new Ext.form.ComboBox({
                xtype : 'combo',
                mode : 'local',
                triggerAction: 'all',
                store : baseLayerStore,
                fieldLabel : 'Base Layer',
                forceSelection : true,
                lazyInit : false,
                displayField : 'title',
                editable : false
        });
        
        baseLayerCombo.on('select', function(combo, record, index) {
            this.controller.requestBaseLayer(record);
        }, this);
        
        
        var extendedLegendImage = Ext.extend(GeoExt.LegendImage, {
            initComponent: function(){
                Ext.apply(this, {
                    flex : 1
                })
                extendedLegendImage.superclass.initComponent.call(this, arguments);
            }
            ,onRender: function() {
                extendedLegendImage.superclass.onRender.apply(this, arguments);
            }
            ,setUrl: function(url) {
                this.url = url;
                var el = this.getEl();
                if (el) {
                    el.dom.src = '';
                    el.un("error", this.onImageLoadError, this);
                    el.on("error", this.onImageLoadError, this, {single: true});
                    el.dom.src = url;
                }
            }
        })
        this.legendImage = new extendedLegendImage();
        this.legendWindow = new Ext.Window({
            resizable: false
            ,draggable: false
            ,closable: false
            ,border: false
            ,frame: false
            ,shadow: false
            ,layout: 'absolute'
            ,items: [this.legendImage]
            ,height: this.DEFAULT_LEGEND_Y
            ,width: this.DEFAULT_LEGEND_X
        });
        this.legendWindow.show();

        var capabilitiesStore = config.capabilitiesStore;
        var layerCombo = new Ext.form.ComboBox({
            xtype : 'combo',
            mode : 'local',
            triggerAction: 'all',
            store : capabilitiesStore,
            fieldLabel : 'Layer',
            forceSelection : true,
            lazyInit : false,
            displayField : 'title'
        });
        layerCombo.on('select', function(combo, record, index) {
            this.controller.requestLayer(record);
        }, this);
        capabilitiesStore.on('load', function(capStore, records) {
            var firstRecord = capStore.getAt(0);
            layerCombo.setValue(firstRecord.get("title"));
            layerCombo.fireEvent('select', layerCombo, firstRecord, 0);
        });

        var zlayerCombo, zlayerName, zlayerStore, zlayerComboConfig;
        zlayerName = 'elevation';
        zlayerStore = new Ext.data.ArrayStore({
            storeId : 'zlayerStore',
            idIndex: 0,
            fields: [zlayerName]
        });
        zlayerComboConfig = {
            mode : 'local',
            triggerAction: 'all',
            store : zlayerStore,
            forceSelection : true,
            lazyInit : false,
            displayField : zlayerName
        };

        var legendCombo = new Ext.form.ComboBox({
            xtype : 'combo'
            ,mode : 'local'
            ,triggerAction: 'all'
            ,store : this.controller.getLegendStore()
            ,fieldLabel : 'Legend'
            ,forceSelection : true
            ,lazyInit : false
            ,displayField : 'title'
            ,editable : false
            ,emptyText : 'Please Select...'
        });
        legendCombo.on('select', function(obj, rec, ind) {
            LOG.debug('LayerChooser: A new legend style chosen: ' + rec.id + ' (' + rec.data.abstrakt + ')');
            this.controller.requestLegendRecord(rec);
        },this)
        legendCombo.store.on('load', function(store) {
            // When the combo's store gets loaded, set
            // the combo to the first value in the store.
            // Trigger the select action so the map will 
            // use the first record as the legend style
            LOG.debug('LayerChooser: Legend Combobox Loading.');
            this.setValue(store.getAt(0).get('name'));
            this.fireEvent('select', this, store.getAt(0), 0);
        }, legendCombo);
        var layerOpacitySlider = new Ext.Slider({
            value : 40,
            fieldLabel: "Opacity",
            plugins: new GeoExt.LayerOpacitySliderTip({
                template: '<div>Opacity: {opacity}%</div>'
            })
        });
        layerOpacitySlider.on('change', function() {
            this.controller.requestOpacity(layerOpacitySlider.getValue() / 100);
        }, this, {
            buffer: 5
        });

        var activityBar = new GDP.MapActivityBar({
                id : 'activityBar'
                ,map : config.map
        });

        config = Ext.apply({
            items : [
            activityBar
            ,baseLayerCombo
            ,layerCombo
            ,legendCombo
            ,layerOpacitySlider
            ]
        }, config);
        this.controller.on('changebaselayer', function() {
            baseLayerCombo.setValue(this.controller.getBaseLayer().data.title);
        }, this);
        this.controller.on('changelegend', function(){
            LOG.debug('LayerChooser: Observed legend change');
            LOG.debug('LayerChooser: Removing current legend image and reapplying new legend image.');
            var legendHref = this.controller.getLegendRecord().data.href;
            this.legendImage.setUrl(GDP.PROXY_PREFIX + legendHref);
            this.legendWindow.show(null, function() {
                this.realignLegend();
            }, this);
        }, this);
        this.controller.on('changelayer', function() {
            var layer = this.controller.getLayer();
            if (layer) {
                layerCombo.setValue(layer.getLayer().name);
                this.controller.modifyLegendStore(layer.data);
            }

            if (zlayerCombo) {
                this.remove(zlayerCombo)
            }
            var loaded = this.controller.loadDimensionStore(layer, zlayerStore, zlayerName);

            if (loaded) {
                var threshold = this.controller.getDimension(zlayerName);
                if (threshold) {
                    zlayerCombo = new Ext.form.ComboBox(Ext.apply({
                        fieldLabel : this.controller.getZAxisName()
                    }, zlayerComboConfig));
                    this.add(zlayerCombo);
                    zlayerCombo.setValue(threshold);
                    zlayerCombo.on('select', function(combo, record, index) {
                        this.controller.requestDimension(zlayerName, record.get(zlayerName));
                    }, this);
                    this.doLayout();
                }
            }

        }, this);

        this.controller.on('changedimension', function() {
            var threshold = this.controller.getDimension(zlayerName);
            if (threshold & zlayerCombo) {
                zlayerCombo.setValue(threshold);
            }
        }, this);
        GDP.LayerChooser.superclass.constructor.call(this, config);
        baseLayerCombo.setValue(this.controller.getBaseLayer().data.title);
        this.on('resize', function() {
            this.realignLegend(); 
        }, this);
    }
});
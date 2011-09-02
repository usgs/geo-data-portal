Ext.ns("GDP");

/**
 * This panel is a holder for all of the controls related to the dataset
 */
GDP.DatasetConfigPanel = Ext.extend(Ext.Panel, {
    controller : undefined,
    capabilitiesStore : undefined,
    catalogStore : undefined,
    derivativeCombo : undefined,
    scenarioCombo : undefined,
    gcmCombo : undefined,
    layerCombo : undefined,
    zlayerCombo : undefined,
    zlayerName : undefined,
    zlayerStore : undefined,
    zlayerComboConfig : undefined,
    constructor : function(config) {
        LOG.debug('DatasetConfigPanel:constructor: Constructing self.');

        this.controller = config.controller;
        
        this.capabilitiesStore = config.capabilitiesStore;
        this.catalogStore = config.getRecordsStore;
        
        this.derivativeCombo = new Ext.form.ComboBox({
            xtype : 'combo',
            mode : 'local',
            triggerAction : 'all',
            store : this.catalogStore,
            fieldLabel : 'Derivative',
            forceSelection : true,
            lazyInit : false,
            editable : false,
            displayField : 'version',
            emptyText : 'Loading...'
        });
        
        this.zlayerName = 'elevation';
        this.zlayerStore = new Ext.data.ArrayStore({
            storeId : 'zlayerStore',
            idIndex: 0,
            fields: [this.zlayerName]
        });
        this.zlayerComboConfig = {
            mode : 'local',
            triggerAction: 'all',
            store : this.zlayerStore,
            forceSelection : true,
            lazyInit : false,
            displayField : this.zlayerName,
            emptyText : 'Loading...',
            autoWidth : true
        };
        
        this.zlayerCombo = new Ext.form.ComboBox(Ext.apply({
            fieldLabel : this.controller.getZAxisName(),
            editable : false
        }, this.zlayerComboConfig));
        this.layerCombo = new Ext.form.ComboBox({
            xtype : 'combo',
            mode : 'local',
            triggerAction: 'all',
            store : this.capabilitiesStore,
            fieldLabel : 'Dataset',
            forceSelection : true,
            lazyInit : false,
            editable : false,
            displayField : 'title',
            emptyText : 'Loading...'
        });
        
        Ext.iterate([this.derivativeCombo, this.layerCombo, this.zlayerCombo], function(item) {
            item.on('added', function(me, parent){ 
                me.setWidth(parent.width - 5); 
            })
        }, this);
        
        config = Ext.apply({
            id : 'dataset-configuration-panel',
            labelAlign : 'top',
            items : [
                this.derivativeCombo,
                this.layerCombo,
                this.zlayerCombo
            ],
            layout : 'form',
            title : 'Dataset Configuration',
            width : config.width || undefined
        }, config);
        GDP.DatasetConfigPanel.superclass.constructor.call(this, config);
        
        this.catalogStore.proxy.setApi(Ext.data.Api.actions.read, args.url);
        this.catalogStore.load();
        LOG.debug('DatasetConfigPanel:constructor: Construction complete.');
        
        LOG.debug('DatasetConfigPanel:constructor: Registering listeners.');
        this.capabilitiesStore.on('load', function(capStore) {
            LOG.debug('DatasetConfigPanel: Capabilities store has finished loading.');
            this.capStoreOnLoad(capStore);
        }, this);
        this.capabilitiesStore.on('exception', function() {
            LOG.debug('DatasetConfigPanel: Capabilities store has encountered an exception.');
            this.controller.capabilitiesExceptionOccurred();
        }, this);
        this.catalogStore.on('load', function(catStore) {
            LOG.debug('DatasetConfigPanel: Catalog store has finished loading.');
            this.catStoreOnLoad(catStore);
        }, this);
        this.catalogStore.on('exception', function() {
            LOG.debug('DatasetConfigPanel: Catalog store has encountered an exception.');
            this.controller.getRecordsExceptionOccurred();
        }, this);
        this.layerCombo.on('select', function(combo, record, index) {
            this.controller.requestLayer(record);
        }, this);
        this.controller.on('selected-dataset', function(args) {
            LOG.debug('DatasetConfigPanel observed "selected-dataset"');
            this.onSelectedDataset(args);
        }, this);
        this.controller.on('loaded-capstore', function(args) {
            LOG.debug('DatasetConfigPanel observed "loaded-capstore"');
            this.onLoadedCapstore(args);
        }, this);
        this.controller.on('loaded-catstore', function(args) {
            LOG.debug('DatasetConfigPanel observed "loaded-catstore"');
            this.onLoadedCatstore(args);
        }, this);
        this.controller.on('changelayer', function() {
            LOG.debug('DatasetConfigPanel: Observed "changelayer".');
            this.onChangeLayer();
        }, this);
        this.controller.on('changedimension', function() {
            LOG.debug('DatasetConfigPanel: Observed \'changedimension\'.');
            this.onChangeDimension();
        }, this);
    },
    capStoreOnLoad : function(capStore) {
        this.controller.loadedCapabilitiesStore({
            record : capStore.getAt(0)
        });

        if (LOADMASK) LOADMASK.hide();
    },
    catStoreOnLoad : function(catStore) {
        this.controller.loadedGetRecordsStore({
            record : catStore.getAt(0)
        });

        if (LOADMASK) LOADMASK.hide();
    },
    onSelectedDataset : function(args) {
        this.capabilitiesStore.proxy.setApi(Ext.data.Api.actions.read, args.url);
        this.capabilitiesStore.load();
    },
    onLoadedCapstore : function(args) {
        this.layerCombo.setValue(args.record.get("title"));
        this.layerCombo.fireEvent('select', this.layerCombo, args.record, 0);
    },
    onLoadedCatstore : function(args) {
        this.layerCombo.setValue(args.record.get("version"));
        this.layerCombo.fireEvent('select', this.derivativeCombo, args.record, 0);
    },
    onChangeLayer : function() {
        var layer = this.controller.getLayer();
        if (layer) {
            this.layerCombo.setValue(layer.getLayer().name);
        }

        if (this.zlayerCombo) {
            this.remove(this.zlayerCombo)
        }

        var loaded = this.controller.loadDimensionStore(layer, this.zlayerStore, this.zlayerName);

        if (loaded) {
            var threshold = this.controller.getDimension(this.zlayerName);
            if (threshold) {
                LOG.debug('DatasetConfigPanel: Threshold found for layer. Re-adding zlayer combobox.');
                this.zlayerCombo = new Ext.form.ComboBox(Ext.apply({
                    fieldLabel : this.controller.getZAxisName(),
                    editable : false,
                    listWidth : this.width
                }, this.zlayerComboConfig));
                this.zlayerCombo.on('added', function(me, parent, index){ me.setWidth(parent.width); })
                this.add(this.zlayerCombo);

                LOG.debug('DatasetConfigPanel: Setting z-layer combobox to threshold: ' + threshold);
                this.zlayerCombo.setValue(threshold);

                this.zlayerCombo.on('select', function(combo, record, index) {
                    this.controller.requestDimension(this.zlayerName, record.get(this.zlayerName));
                }, this);
                this.doLayout();
            }
        }
    },
    onChangeDimension : function() {
        var threshold = this.controller.getDimension(this.zlayerName);
        if (threshold & this.zlayerCombo) {
            this.zlayerCombo.setValue(threshold);
        }
    }
});
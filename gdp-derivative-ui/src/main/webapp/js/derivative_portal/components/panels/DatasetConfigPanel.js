Ext.ns("GDP");

/**
 * This panel is a holder for all of the controls related to the dataset
 */
GDP.DatasetConfigPanel = Ext.extend(Ext.Panel, {
    controller : undefined,
    capabilitiesStore : undefined,
    parentRecordStore : undefined,
    derivRecordStore : undefined,
    derivRecordStoreLoaded : false, // TODO find a better way to do this
    leafRecordStore : undefined,
    derivativeStore : undefined,
    derivativeCombo : undefined,
    scenarioStore : undefined,
    scenarioCombo : undefined,
    gcmStore : undefined,
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
        this.parentRecordStore = config.getRecordsStore;
        
        this.derivativeStore = new Ext.data.ArrayStore({
            storeId : 'derivativeStore',
            fields: ['derivative']
        });
        this.derivativeCombo = new Ext.form.ComboBox({
            xtype : 'combo',
            mode : 'local',
            triggerAction : 'all',
            store : this.derivativeStore,
            fieldLabel : 'Derivative',
            forceSelection : true,
            lazyInit : false,
            editable : false,
            displayField : 'derivative',
            emptyText : 'Choose Derivative'
        });
        
        this.scenarioStore = new Ext.data.ArrayStore({
            storeId : 'scenarioStore',
            fields: ['scenario']
        });
        this.scenarioCombo = new Ext.form.ComboBox({
            xtype : 'combo',
            mode : 'local',
            triggerAction : 'all',
            store : this.scenarioStore,
            fieldLabel : 'Scenario',
            forceSelection : true,
            lazyInit : false,
            editable : false,
            displayField : 'scenario',
            emptyText : 'Choose Scenario'
        });
        
        this.gcmStore = new Ext.data.ArrayStore({
            storeId : 'gcmStore',
            fields: ['gcm']
        });
        this.gcmCombo = new Ext.form.ComboBox({
            xtype : 'combo',
            mode : 'local',
            triggerAction : 'all',
            store : this.gcmStore,
            fieldLabel : 'GCM',
            forceSelection : true,
            lazyInit : false,
            editable : false,
            displayField : 'gcm',
            emptyText : 'Choose GCM'
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
            editable : false,
            hidden : true
        }, this.zlayerComboConfig));
//        this.layerCombo = new Ext.form.ComboBox({
//            xtype : 'combo',
//            mode : 'local',
//            triggerAction: 'all',
//            store : this.capabilitiesStore,
//            fieldLabel : 'Dataset',
//            forceSelection : true,
//            lazyInit : false,
//            editable : false,
//            displayField : 'title',
//            emptyText : 'Loading...'
//        });
        
        Ext.iterate([this.derivativeCombo, this.scenarioCombo, this.gcmCombo, this.zlayerCombo], function(item) {
            item.on('added', function(me, parent){ 
                me.setWidth(parent.width - 5); 
                me.setValue('');
            })
        }, this);
        
        config = Ext.apply({
            id : 'dataset-configuration-panel',
            labelAlign : 'top',
            items : [
                this.derivativeCombo,
                this.scenarioCombo,
                this.gcmCombo,
                this.zlayerCombo
            ],
            layout : 'form',
            title : 'Dataset Configuration',
            width : config.width || undefined
        }, config);
        GDP.DatasetConfigPanel.superclass.constructor.call(this, config);
        
        //this.catalogStore.proxy.setApi(Ext.data.Api.actions.read, args.url);
        this.parentRecordStore.load();
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
        this.parentRecordStore.on('load', function(catStore) {
            LOG.debug('DatasetConfigPanel: Catalog store has finished loading.');
            this.catStoreOnLoad(catStore);
        }, this);
        this.parentRecordStore.on('exception', function() {
            LOG.debug('DatasetConfigPanel: Catalog store has encountered an exception.');
            this.controller.getRecordsExceptionOccurred();
        }, this);
        this.derivativeCombo.on('select', function(combo, record, index) {
            this.controller.requestDerivative(record);
        }, this);
        this.scenarioCombo.on('select', function(combo, record, index) {
            this.controller.requestScenario(record);
        }, this);
        this.gcmCombo.on('select', function(combo, record, index) {
            this.controller.requestGcm(record);
        }, this);
        this.controller.on('selected-dataset', function(args) {
            LOG.debug('DatasetConfigPanel observed "selected-dataset"');
            this.onSelectedDataset(args);
        }, this);
        this.controller.on('selected-deriv', function(args) {
            LOG.debug('DatasetConfigPanel observed "selected-deriv"');
            this.onSelectedDerivative(args);
        }, this);
        this.controller.on('loaded-capstore', function(args) {
            LOG.debug('DatasetConfigPanel observed "loaded-capstore"');
            this.onLoadedCapstore(args);
        }, this);
        this.controller.on('loaded-catstore', function(args) {
            LOG.debug('DatasetConfigPanel observed "loaded-catstore"');
            this.onLoadedCatstore(args);
        }, this);
        this.controller.on('loaded-derivstore', function(args) {
            LOG.debug('DatasetConfigPanel observed "loaded-derivstore"');
            this.onLoadedDerivStore(args);
        }, this);
        this.controller.on('loaded-leafstore', function(args) {
            LOG.debug('DatasetConfigPanel observed "loaded-leafstore"');
            this.onLoadedLeafStore(args);
        }, this);
        this.controller.on('changelayer', function() {
            LOG.debug('DatasetConfigPanel: Observed "changelayer".');
            this.onChangeLayer();
        }, this);
        this.controller.on('changederiv', function() {
            LOG.debug('DatasetConfigPanel: Observed "changederiv".');
            this.onChangeDerivative();
        }, this);
        this.controller.on('changescenario', function() {
            LOG.debug('DatasetConfigPanel: Observed "changescenario".');
            this.onChangeScenario();
        }, this);
        this.controller.on('changegcm', function() {
            LOG.debug('DatasetConfigPanel: Observed "changegcm".');
            this.onChangeGcm();
        }, this);
        this.controller.on('changedimension', function() {
            LOG.debug('DatasetConfigPanel: Observed \'changedimension\'.');
            this.onChangeDimension();
        }, this);
    },
    capStoreOnLoad : function(capStore) {
        var index = capStore.findBy(this.capsFindBy, this, 0);
        this.controller.loadedCapabilitiesStore({
            record : capStore.getAt(index)
        });
    },
    catStoreOnLoad : function(catStore) {
        this.controller.loadedGetRecordsStore({
            record : catStore.getAt(0)
        });
        if (LOADMASK) LOADMASK.hide();
    },
    derivStoreOnLoad : function(derivStore) {
        this.controller.loadedDerivStore({
            record : derivStore.getAt(0)
        });
        if (LOADMASK) LOADMASK.hide();
    },
    leafStoreOnLoad : function(leafStore) {
        this.controller.loadedLeafStore({
            record : leafStore.getAt(0)
        });

        if (LOADMASK) LOADMASK.hide();
    },
    onSelectedDataset : function(args) {
        this.capabilitiesStore.proxy.setApi(Ext.data.Api.actions.read, args.url);
        this.capabilitiesStore.load();
    },
    onLoadedCapstore : function(args) {
        this.controller.fireEvent('changegcm');
        this.controller.fireEvent('changelayer');
//        this.layerCombo.setValue(args.record.get("title"));
//        this.layerCombo.fireEvent('select', this.layerCombo, args.record, 0);
    },
    onLoadedCatstore : function(args) {
        this.derivativeStore.removeAll();
        this.derivativeStore.loadData(args.record.get("derivatives"), true);
        this.scenarioStore.removeAll();
        this.scenarioStore.loadData(args.record.get("scenarios"), true);
        this.gcmStore.removeAll();
        this.gcmStore.loadData(args.record.get("gcms"), true);
//        this.derivativeCombo.fireEvent('select', this.derivativeCombo, this.derivativeStore.getAt(0), 0);
//        this.scenarioCombo.fireEvent('select', this.scenarioCombo, this.scenarioStore.getAt(0), 0);
//        this.gcmCombo.fireEvent('select', this.gcmCombo, this.gcmStore.getAt(0), 0);
//        this.derivativeCombo.
//        this.derivativeCombo.fireEvent('select', this.derivativeCombo, args.record, 0);
    },
    onLoadedDerivStore : function(args) {
        // this might be where I gray out some of the options
        this.derivRecordStoreLoaded = true;
        this.loadLeafRecordStore();
    },
    onLoadedLeafStore : function(args) {
        LOG.debug("DatasetConfigPanel: onLoadedLeafStore()");
        this.controller.setOPeNDAPEndpoint(args.record.get("opendap"));
        this.controller.fireEvent('selected-dataset', {url : GDP.PROXY_PREFIX + args.record.get("wms")});
        // this might be where I gray out some of the options
        // also probably call WMS GetCaps
    },
    onChangeLayer : function() {
        var layer = this.controller.getLayer();
        if (layer) {
            //this.layerCombo.setValue(layer.getLayer().name);
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
                this.zlayerCombo.on('added', function(me, parent, index){me.setWidth(parent.width);})
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
    onChangeDerivative : function () {
        var derivative = this.controller.getDerivative();
        if (derivative) {
            //this.derivativeCombo.setValue(derivative.get("derivative"))
        }
        this.loadDerivRecordStore();
    },
    onChangeScenario : function () {
        var scenario = this.controller.getScenario();
        if (scenario) {
            //this.scenarioCombo.setValue(scenario.get("scenario"))
        }
        if (this.derivRecordStoreLoaded) {
            this.loadLeafRecordStore();
        }
    },
    onChangeGcm : function () {
        var gcm = this.controller.getGcm();
        if (gcm) {
            //this.gcmCombo.setValue(gcm.get("gcm"));
            var index = this.capabilitiesStore.findBy(this.capsFindBy, this, 0);
            LOG.debug('DatasetConfigPanel: onChangeGcm got index ', index);
            this.controller.requestLayer(this.capabilitiesStore.getAt(index));
        }
    },
    onChangeDimension : function() {
        var threshold = this.controller.getDimension(this.zlayerName);
        if (threshold & this.zlayerCombo) {
            this.zlayerCombo.setValue(threshold);
        }
    },
    capsFindBy : function(record, id) {
        return (this.controller.getGcm().get("gcm") === record.get('layer').name);
    },
    loadDerivRecordStore : function() {
        // TODO fail nicely if this fails
        var derivative = this.controller.getDerivative();
        this.derivRecordStore = new GDP.CSWGetRecordsStore({
            url : "geonetwork/csw",
            storeId : 'cswStore',
            opts : {
                resultType : 'results',
                outputSchema : 'http://www.isotc211.org/2005/gmd',
                Query : {
                    ElementSetName : {value: 'full'},
                    Constraint : {
                        Filter : {
                            type : '&&',
                            filters : [{
                                type : "==",
                                property : 'ParentIdentifier',
                                value : this.parentRecordStore.getAt(0).get("identifier")
                            },{
                                type : "&&",
                                filters : [{
                                    type : "==",
                                    property : 'KeywordType',
                                    value : 'derivative'
                                },{
                                    type : "==",
                                    property : 'Subject',
                                    value : derivative.get('derivative')
                                }]
                            }]
                        },
                        version : '1.1.0'
                    }
                }
            },
            listeners : {
                load : function(catStore) {
                            LOG.debug('DatasetConfigPanel: Catalog store has finished loading.');
                            this.derivStoreOnLoad(catStore);
                       },
                exception : function() {
                    LOG.debug('DatasetConfigPanel: Catalog store has encountered an exception.');
                    this.controller.getRecordsExceptionOccurred();                
                },
                scope : this
            }
        });
        this.derivRecordStore.load();
    },
    loadLeafRecordStore : function() {
        // TODO fail nicely if things don't go so well
        var scenario = this.controller.getScenario();
        var gcm = this.controller.getGcm();
        this.leafRecordStore = new GDP.CSWGetRecordsStore({
            url : "geonetwork/csw",
            storeId : 'cswStore',
            opts : {
                resultType : 'results',
                outputSchema : 'http://www.isotc211.org/2005/gmd',
                Query : {
                    ElementSetName : {value: 'full'},
                    Constraint : {
                        Filter : {
                            type : '&&',
                            filters : [{
                                    type : "==",
                                    property : 'ParentIdentifier',
                                    value : this.derivRecordStore.getAt(0).get("identifier")
                                },{
                                    type : "&&",
                                    filters : [{
                                        type : "==",
                                        property : 'KeywordType',
                                        value : 'scenario'
                                    },{
                                        type : "==",
                                        property : 'Subject',
                                        value : scenario.get('scenario')
                                    }]
                                }]
                        },
                        version : '1.1.0'
                    }
                }
            },
            listeners : {
                load : function(catStore) {
                    LOG.debug('DatasetConfigPanel: Catalog store has finished loading.');
                    this.leafStoreOnLoad(catStore);
                },
                exception : function() {
                    LOG.debug('DatasetConfigPanel: Catalog store has encountered an exception.');
                    this.controller.getRecordsExceptionOccurred();
                },
                scope : this
            }
        });
        this.leafRecordStore.load();
    }
});
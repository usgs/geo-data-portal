Ext.ns("GDP");

/**
 * This panel is a holder for all of the controls related to the dataset
 */
GDP.DatasetConfigPanel = Ext.extend(Ext.Panel, {
    controller : undefined,
    capabilitiesStore : undefined,
    derivRecordStore : undefined,
    derivRecordStoreLoaded : false, 
    derivativeStore : undefined,
    derivativeCombo : undefined,
    featureOfInterestCombo : undefined,
    gcmStore : undefined,
    gcmCombo : undefined,
    layerCombo : undefined,
    leafRecordStore : undefined,
    parentRecordStore : undefined,
    scenarioStore : undefined,
    scenarioCombo : undefined,
    timestepName : undefined,
    timestepStore : undefined,
    timestepCombo : undefined,
    timestepComboConfig : undefined,
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
            fields: ['derivative', 'quicktip']
        });
        this.derivativeCombo = new Ext.form.ComboBox({
            xtype : 'combo',
            mode : 'local',
            triggerAction : 'all',
            store : this.derivativeStore,
            fieldLabel : '<tpl for="."><span ext:qtip="Some information about derivative" class="x-combo-list-item"><img class="quicktip-img" src="images/info.gif" /></span></tpl> Derivative',
            forceSelection : true,
            lazyInit : false,
            editable : false,
            displayField : 'derivative',
            emptyText : 'Choose Derivative',
            tpl : '<tpl for="."><div ext:qtip="<b>{derivative}</b><br /><br />{quicktip}" class="x-combo-list-item">{derivative}</div></tpl>'
        });
        
        this.scenarioStore = new Ext.data.ArrayStore({
            storeId : 'scenarioStore',
            fields: ['scenario', 'quicktip']
        });
        this.scenarioCombo = new Ext.form.ComboBox({
            xtype : 'combo',
            mode : 'local',
            triggerAction : 'all',
            store : this.scenarioStore,
            fieldLabel : '<tpl for="."><span ext:qtip="Some information about emission scenario" class="x-combo-list-item"><img class="quicktip-img" src="images/info.gif" /></span></tpl> Scenario',
            forceSelection : true,
            lazyInit : false,
            editable : false,
            displayField : 'scenario',
            emptyText : 'Choose Emission Scenario',
            tpl : '<tpl for="."><div ext:qtip="<b>{scenario}</b><br /><br />{quicktip}" class="x-combo-list-item">{scenario}</div></tpl>'
        });
        
        this.timestepName = 'time';
        this.timestepStore = new Ext.data.ArrayStore({
            storeId : 'timestepStore',
            fields: [this.timestepName, 'timestepDisplayName']
        });
        this.timestepComboConfig = {
            mode : 'local',
            triggerAction : 'all',
            store : this.timestepStore,
            forceSelection : true,
            lazyInit : false,
            valueField : this.timestepName,
            displayField : 'timestepDisplayName',
            editable : false,
            autoWidth : true
        }
        this.timestepCombo = new Ext.form.ComboBox({
            hidden : true
        }, this.timestepComboConfig);
        
        this.gcmStore = new Ext.data.ArrayStore({
            storeId : 'gcmStore',
            fields: ['gcm', 'quicktip']
        });
        this.gcmCombo = new Ext.form.ComboBox({
            xtype : 'combo',
            mode : 'local',
            triggerAction : 'all',
            store : this.gcmStore,
            fieldLabel : '<tpl for="."><span ext:qtip="Some information about GCM" class="x-combo-list-item"><img class="quicktip-img" src="images/info.gif" /></span></tpl> GCM',
            forceSelection : true,
            lazyInit : false,
            editable : false,
            displayField : 'gcm',
            emptyText : 'Choose GCM',
            tpl : '<tpl for="."><div ext:qtip="<b>{gcm}</b><br /><br />{quicktip}" class="x-combo-list-item">{gcm}</div></tpl>'
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
            editable : false,
            hidden : true
        }, this.zlayerComboConfig));
        
        var foiGetCapsStore = new GeoExt.data.WMSCapabilitiesStore({
            url: config.foiGetCapsURL,
            autoLoad: true,
            listeners: {
                load: function(data) {
                    Ext.each(data.data.items, function(item, index, allItems){
                        item.data.layer.url = GDP.PROXY_PREFIX + item.data.layer.url;
                        item.id = 'featureLayer';
                        item.data.layer.setOpacity(0.3)
                    }, this);
                },
                exception: function(proxy, type, action, options, response, arg) {
                    LOG.error(response.responseText);
                    NOTIFY.error({
                        msg : response.responseText
                    });
                }
            }
        });
        this.featureOfInterestCombo = new Ext.form.ComboBox({
            xtype : 'combo',
            mode : 'local',
            triggerAction : 'all',
            store : foiGetCapsStore,
            fieldLabel : '<tpl for="."><span ext:qtip="Choose an area of interest" class="x-combo-list-item"><img class="quicktip-img" src="images/info.gif" /></span></tpl> Area Of Interest',
            forceSelection : true,
            lazyInit : false,
            editable : false,
            displayField : 'title',
            emptyText : 'Choose Area Of Interest',
            tpl : '<tpl for="."><div ext:qtip="{title}" class="x-combo-list-item">{title}</div></tpl>'
        });
        
        Ext.iterate([this.derivativeCombo, this.scenarioCombo, this.timestepCombo, this.gcmCombo, this.zlayerCombo, this.featureOfInterestCombo], function(item) {
            item.on('added', function(me, parent){ 
                me.setWidth(parent.ownerCt.width - 15); 
                me.setValue('');
            })
        }, this);
        
        
        
        config = Ext.apply({
            id : 'dataset-configuration-panel',
            title : 'Dataset Configuration',
            width : config.width || undefined,
            animate : true,
            border : false,
            items : [{
                xtype: 'fieldset',
                labelAlign : 'top',
                autoHeight : true,
                ref : 'derivativeFieldSet',
                defaultType: 'combo',
                layout : 'form',
                items : [
                this.derivativeCombo,
                this.zlayerCombo
                ]
            },{
                xtype: 'fieldset',
                labelAlign : 'top',
                title: 'Map',
                autoHeight : true,
                ref : 'mapFieldSet',
                defaultType: 'combo',
                layout : 'form',
                items : [
                this.scenarioCombo,
                this.gcmCombo,
                this.timestepCombo
                ]
            },
            {
                xtype: 'fieldset',
                id : 'plotFieldSet',
                labelAlign : 'top',
                ref : 'plotFieldSet',
                title: 'Plot',
                autoHeight : true,
                defaultType: 'combo',
                layout : 'form',
                items : [
                this.featureOfInterestCombo
                ]
            }
            ]
        }, config);
        GDP.DatasetConfigPanel.superclass.constructor.call(this, config);
        LOG.debug('DatasetConfigPanel:constructor: Construction complete.');
        this.parentRecordStore.load();        
        
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
        this.featureOfInterestCombo.on('select', function(combo, record, index) {
            this.controller.requestFeatureOfInterest(record);
            var foiName = record.get('name');
            this.controller.currentFOI = foiName.substr(foiName.indexOf(":")+1);
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
        LOG.debug("DatasetConfigPanel: capStoreOnLoad()");
        var index = capStore.findBy(this.capsFindBy, this, 0);
        if (index > -1) {
            this.controller.loadedCapabilitiesStore({
                record : capStore.getAt(index)
            });
        }
    },
    catStoreOnLoad : function(catStore) {
        LOG.debug("DatasetConfigPanel: catStoreOnLoad()");
        this.controller.loadedGetRecordsStore({
            record : catStore.getAt(0)
        });
        if (LOADMASK) LOADMASK.hide();
    },
    derivStoreOnLoad : function(derivStore) {
        LOG.debug("DatasetConfigPanel: derivStoreOnLoad()");
        this.controller.loadedDerivStore({
            record : derivStore.getAt(0)
        });
        if (LOADMASK) LOADMASK.hide();
    },
    leafStoreOnLoad : function(leafStore) {
        LOG.debug("DatasetConfigPanel: leafStoreOnLoad()");
        this.controller.loadedLeafStore({
            record : leafStore.getAt(0)
        });

        if (LOADMASK) LOADMASK.hide();
    },
    onSelectedDataset : function(args) {
        LOG.debug("DatasetConfigPanel: onSelectedDataset()");
        this.capabilitiesStore.proxy.setApi(Ext.data.Api.actions.read, args.url);
        this.capabilitiesStore.load();
    },
    onLoadedCapstore : function(args) {
        LOG.debug("DatasetConfigPanel: onLoadedCapStore()");
        this.controller.fireEvent('changegcm');
        this.controller.fireEvent('changelayer');
    },
    onLoadedCatstore : function(args) {
        LOG.debug("DatasetConfigPanel: onLoadedCatStore()");
        this.derivativeStore.removeAll();
        this.derivativeStore.loadData(args.record.get("derivatives"), true);
        this.scenarioStore.removeAll();
        this.scenarioStore.loadData(args.record.get("scenarios"), true);
        this.gcmStore.removeAll();
        this.gcmStore.loadData(args.record.get("gcms"), true);
        
        this.timestepCombo.label.update('<tpl for="."><span ext:qtip="' + args.record.get("fieldLabels").timeperiod + '" class="x-combo-list-item"><img class="quicktip-img" src="images/info.gif" /></span></tpl> Time Period For Map');
        this.derivativeCombo.label.update('<tpl for="."><span ext:qtip="' + args.record.get("fieldLabels").derivative + '" class="x-combo-list-item"><img class="quicktip-img" src="images/info.gif" /></span></tpl> Derivative');
        this.scenarioCombo.label.update('<tpl for="."><span ext:qtip="' + args.record.get("fieldLabels").scenario + '" class="x-combo-list-item"><img class="quicktip-img" src="images/info.gif" /></span></tpl> Emissions Scenario');
        this.gcmCombo.label.update('<tpl for="."><span ext:qtip="' + args.record.get("fieldLabels").gcm + '" class="x-combo-list-item"><img class="quicktip-img" src="images/info.gif" /></span></tpl> Climate Model');
        
        // http://internal.cida.usgs.gov/jira/browse/GDP-416
        this.derivativeCombo.setValue(args.record.get("derivatives")[0][0]);
        this.scenarioCombo.setValue(args.record.get("scenarios")[0][0]);
        this.gcmCombo.setValue(args.record.get("gcms")[0][0]);
        this.derivativeCombo.fireEvent('select', this, this.derivativeCombo.getStore().data.items[0]);
        this.scenarioCombo.fireEvent('select', this, this.scenarioCombo.getStore().data.items[0]);
        this.gcmCombo.fireEvent('select', this, this.gcmCombo.getStore().data.items[0]);
    },
    onLoadedDerivStore : function(args) {
        LOG.debug("DatasetConfigPanel: onLoadedDerivStore()");
        // this might be where I gray out some of the options
        this.derivRecordStoreLoaded = true;
        this.controller.sosEndpoint = args.record.get("sos");
        this.loadLeafRecordStore();
    },
    onLoadedLeafStore : function(args) {
        LOG.debug("DatasetConfigPanel: onLoadedLeafStore()");
        this.controller.setOPeNDAPEndpoint(args.record.get("opendap"));
        this.controller.fireEvent('selected-dataset', {
            url : GDP.PROXY_PREFIX + args.record.get("wms")
        });
    },
    onChangeLayer : function() {
        LOG.debug("DatasetConfigPanel: onChangeLayer()");
        
        var layer = this.controller.getLayer();

        if (this.zlayerCombo) {
            this.derivativeFieldSet.remove(this.zlayerCombo);
        }
        
        if (this.timestepCombo) {
            this.mapFieldSet.remove(this.timestepCombo);
        }

        var loaded = this.controller.loadDimensionStore(layer, this.zlayerStore, this.zlayerName)
        && this.controller.loadDimensionStore(layer, this.timestepStore, this.timestepName);
        
        if (loaded) {
            this.controller.time = this.timestepStore.data.items[0].data.time;
            
            var threshold = this.controller.getDimension(this.zlayerName);
            
            // TODO - Move time combobox creation down here instead of in constructor
            var time = this.controller.getDimension(this.timestepName);
            
            if (time) {
                LOG.debug('DatasetConfigPanel: Time found for layer. Re-adding time step combobox.');
                this.timestepCombo = new Ext.form.ComboBox(Ext.apply({
                    fieldLabel : '<tpl for="."><span ext:qtip="' +this.getRecordsStore.data.items[0].data.fieldLabels.timeperiod + '" class="x-combo-list-item"><img class="quicktip-img" src="images/info.gif" /></span></tpl> Time Period For Map',
                    listWidth : this.width
                }, this.timestepComboConfig));
                this.timestepCombo.on('added', function(me, parent){
                    me.setWidth(parent.ownerCt.width - 15);
                });
                this.mapFieldSet.add(this.timestepCombo);
                
                LOG.debug('DatasetConfigPanel: Setting timestep combobox to time: ' + time);
                this.timestepCombo.setValue(time);
                
                this.timestepCombo.on('select', function(combo, record, index){
                    LOG.debug('DatasetConfigPanel:timeStepCombo: observed "select"');
                    this.controller.requestDimension(this.timestepName, record.get(this.timestepName));
                }, this);
            }
            
            if (threshold) {
                LOG.debug('DatasetConfigPanel: Threshold found for layer. Re-adding zlayer combobox.');
                this.zlayerCombo = new Ext.form.ComboBox(Ext.apply({
                    fieldLabel : '<tpl for="."><span ext:qtip="Which threshold to display the derivative data for" class="x-combo-list-item"><img class="quicktip-img" src="images/info.gif" /></span></tpl> ' + this.controller.getZAxisName(),
                    editable : false,
                    listWidth : this.width
                }, this.zlayerComboConfig));
                this.zlayerCombo.on('added', function(me, parent){
                    me.setWidth(parent.ownerCt.width - 15);
                });
                this.derivativeFieldSet.add(this.zlayerCombo);

                LOG.debug('DatasetConfigPanel: Setting z-layer combobox to threshold: ' + threshold);
                this.zlayerCombo.setValue(threshold);
                this.controller.threshold = threshold;

                this.zlayerCombo.on('select', function(combo, record, index) {
                    LOG.debug('DatasetConfigPanel:zlayerCombo: observed "select"');
                    this.controller.requestDimension(this.zlayerName, record.get(this.zlayerName));
                }, this);
            }
            this.doLayout();
        }
    },
    onChangeDerivative : function () {
        LOG.debug("DatasetConfigPanel: onChangeDerivative()");
        var derivative = this.controller.getDerivative();
        if (derivative) {
        }
        this.loadDerivRecordStore();
        if (this.controller.getFeatureAttribute()) {
            this.controller.updatePlotter();
        }
    },
    onChangeScenario : function () {
        LOG.debug("DatasetConfigPanel: onChangeScenario()");
        var scenario = this.controller.getScenario();
        if (scenario) {
        }
        if (this.derivRecordStoreLoaded) {
            this.loadLeafRecordStore();
        }
    },
    onChangeGcm : function () {
        LOG.debug("DatasetConfigPanel: onChangeGcm()");
        var gcm = this.controller.getGcm();
        if (gcm) {
            //this.gcmCombo.setValue(gcm.get("gcm"));
            var index = this.capabilitiesStore.findBy(this.capsFindBy, this, 0);
            LOG.debug('DatasetConfigPanel: onChangeGcm got index ', index);
            this.controller.requestLayer(this.capabilitiesStore.getAt(index));
        }
    },
    onChangeDimension : function() {
        LOG.debug("DatasetConfigPanel: onChangeDimension()");
        var threshold = this.controller.getDimension(this.zlayerName);
        if (threshold && this.zlayerCombo) {
            this.zlayerCombo.setValue(threshold);
            this.controller.threshold = threshold;
        }
        if (this.controller.getFeatureAttribute()) {
            this.controller.updatePlotter();
        }
    },
    capsFindBy : function(record, id) {
        LOG.debug("DatasetConfigPanel: capsFindBy()");
        var gcm = this.controller.getGcm()
        if (gcm) {
            return (gcm.get("gcm") === record.get('layer').name);
        }
        return false;
    },
    loadDerivRecordStore : function() {
        LOG.debug("DatasetConfigPanel: loadDerivRecordStore()");
        // TODO fail nicely if this fails
        var derivative = this.controller.getDerivative();
        this.derivRecordStore = new GDP.CSWGetRecordsStore({
            url : "geonetwork/csw",
            storeId : 'cswStore',
            opts : {
                resultType : 'results',
                outputSchema : 'http://www.isotc211.org/2005/gmd',
                Query : {
                    ElementSetName : {
                        value: 'full'
                    },
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
        LOG.debug("DatasetConfigPanel: loadLeafRecordStore()");
        var scenario = this.controller.getScenario();
        if (!scenario) return;
        this.leafRecordStore = new GDP.CSWGetRecordsStore({
            url : "geonetwork/csw",
            storeId : 'cswStore',
            opts : {
                resultType : 'results',
                outputSchema : 'http://www.isotc211.org/2005/gmd',
                Query : {
                    ElementSetName : {
                        value: 'full'
                    },
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

Ext.ns("PRMS");

PRMS.WPSConfigPanel = Ext.extend(Ext.Panel, {
    attributeCombo : undefined,
    compositeFieldDates : 'composite-field-dates',
    cswEndpoints : undefined,
    datasetDateBeginId : 'input-dataset-date-begin',
    datasetDateEndId : 'input-dataset-date-end',
    datasetEmailId : 'input-dataset-email',
    datasetIdId : 'input-dataset-id',
    inputAttributeId : 'input-attribute',
    inputDatasetUriId : 'input-dataset-uri',
    layerCombo : undefined,
    layerComboId : 'input-layer',
    outputIdId : 'output-id',
    requireFullCoverageId : 'input-dataset-require-full-coverage',
    uomInputId : 'input-uom',
    'wfs-url' : undefined,
    'wps-processing-url' : undefined,
    constructor : function(config) {
        if (!config) config = {};

        this.cswEndpoints = {
            datasetType1 : {
                url : [
                ['http://type1.csw.url1.usgs.gov:8081/wherever'],
                ['http://type1.csw.url2.usgs.gov:8081/wherever'],
                ['http://type1.csw.url3.usgs.gov:8081/wherever'],
                ['http://type1.csw.url4.usgs.gov:8081/wherever']
                ],
                id : [
                ['id1'],
                ['id2'],
                ['id3'],
                ['id4']
                ]
            },
            datasetType2 : {
                url : [
                ['http://type2.csw.url1.usgs.gov:8081/wherever'],
                ['http://type2.csw.url2.usgs.gov:8081/wherever'],
                ['http://type2.csw.url3.usgs.gov:8081/wherever'],
                ['http://type2.csw.url4.usgs.gov:8081/wherever']
                ],
                id : [
                ['id1'],
                ['id2'],
                ['id3'],
                ['id4']
                ]
            },
            datasetType3 : {
                url : [
                ['http://type3.csw.url1.usgs.gov:8081/wherever'],
                ['http://type3.csw.url2.usgs.gov:8081/wherever'],
                ['http://type3.csw.url3.usgs.gov:8081/wherever'],
                ['http://type3.csw.url4.usgs.gov:8081/wherever']
                ],
                id : [
                ['id1'],
                ['id2'],
                ['id3'],
                ['id4']
                ]
            }
        }
        
        // This is just for visual display purposes. This gets replaced automatically
        this.attributeCombo = new Ext.form.TextField({
            id : this.inputAttributeId,
            fieldLabel : 'Attributes',
            disabled : true
        })
    
        this.layerCombo = new Ext.form.ComboBox({
            id : this.layerComboId,
            fieldLabel : 'Choose A Layer'
        });
        this['wfs-url'] = config['wfs-url'];
        this['wps-processing-url'] = config['wps-processing-url'];

        config = Ext.apply({
            id: 'panel-wpsconfig',
            layout : 'form',
            padding : 5,
            items: [
            this.layerCombo,
            this.attributeCombo,
            {
                xtype : 'fieldset',
                title : 'Dataset Type 1',
                id : 'csw-fieldset-1',
                collapsible : false,
                autoHeight : true,
                items : [
                {
                    xtype : 'combo',
                    id : this.inputDatasetUriId + '-1',
                    fieldLabel : 'Dataset URI',
                    mode : 'local',
                    valueField : 'url',
                    displayField : 'url',
                    triggerAction : 'all',
                    store : new Ext.data.ArrayStore({
                        idIndex : 0,
                        fields : ['url'],
                        data : this.cswEndpoints.datasetType1.url
                    })
                },
                {
                    xtype : 'combo',
                    id : this.datasetIdId + '-1',
                    fieldLabel : 'Dataset ID',
                    mode : 'local',
                    valueField : 'id',
                    displayField : 'id',
                    triggerAction : 'all',
                    store : new Ext.data.ArrayStore({
                        idIndex : 0,
                        fields : ['id'],
                        data : this.cswEndpoints.datasetType1.id
                    })
                },
                {
                    xtype : 'textfield',
                    fieldLabel : 'Output ID',
                    id : this.outputIdId + '-1'
                },
                {
                    xtype : 'combo',
                    id : this.uomInputId + '-1',
                    fieldLabel : 'UOM',
                    mode : 'local',
                    valueField : 'uom',
                    displayField : 'uom',
                    triggerAction : 'all',
                    store : new Ext.data.ArrayStore({
                        idIndex : 0,
                        fields : ['uom'],
                        data : [['m'],['mi']]
                    })
                }
                ]
            },
            {
                xtype : 'fieldset',
                title : 'Dataset Type 2',
                id : 'csw-fieldset-2',
                collapsible : false,
                autoHeight : true,
                items : [
                {
                    xtype : 'combo',
                    id : this.inputDatasetUriId + '-2',
                    fieldLabel : 'Dataset URI',
                    mode : 'local',
                    valueField : 'url',
                    displayField : 'url',
                    triggerAction : 'all',
                    store : new Ext.data.ArrayStore({
                        idIndex : 0,
                        fields : ['url'],
                        data : this.cswEndpoints.datasetType2.url
                    })
                },
                {
                    xtype : 'combo',
                    id : this.datasetIdId + '-2',
                    fieldLabel : 'Dataset ID',
                    mode : 'local',
                    valueField : 'id',
                    displayField : 'id',
                    triggerAction : 'all',
                    store : new Ext.data.ArrayStore({
                        idIndex : 0,
                        fields : ['id'],
                        data : this.cswEndpoints.datasetType2.id
                    })
                },
                {
                    xtype : 'textfield',
                    fieldLabel : 'Output ID',
                    id : this.outputIdId + '-2'
                },
                {
                    xtype : 'combo',
                    id : this.uomInputId + '-2',
                    fieldLabel : 'UOM',
                    mode : 'local',
                    valueField : 'uom',
                    displayField : 'uom',
                    triggerAction : 'all',
                    store : new Ext.data.ArrayStore({
                        idIndex : 0,
                        fields : ['uom'],
                        data : [['m'],['mi']]
                    })
                }
                ]
            },
            {
                xtype : 'fieldset',
                title : 'Dataset Type 3',
                id : 'csw-fieldset-3',
                collapsible : false,
                autoHeight : true,
                items : [
                {
                    xtype : 'combo',
                    id : this.inputDatasetUriId + '-3',
                    fieldLabel : 'Dataset URI',
                    mode : 'local',
                    valueField : 'url',
                    displayField : 'url',
                    triggerAction : 'all',
                    store : new Ext.data.ArrayStore({
                        idIndex : 0,
                        fields : ['url'],
                        data : this.cswEndpoints.datasetType3.url
                    })
                },
                {
                    xtype : 'combo',
                    id : this.datasetIdId + '-3',
                    fieldLabel : 'Dataset ID',
                    mode : 'local',
                    valueField : 'id',
                    displayField : 'id',
                    triggerAction : 'all',
                    store : new Ext.data.ArrayStore({
                        idIndex : 0,
                        fields : ['id'],
                        data : this.cswEndpoints.datasetType3.id
                    })
                },
                {
                    xtype : 'textfield',
                    fieldLabel : 'Output ID',
                    id : this.outputIdId + '-3'
                },
                {
                    xtype : 'combo',
                    id : this.uomInputId + '-3',
                    fieldLabel : 'UOM',
                    mode : 'local',
                    valueField : 'uom',
                    displayField : 'uom',
                    triggerAction : 'all',
                    store : new Ext.data.ArrayStore({
                        idIndex : 0,
                        fields : ['uom'],
                        data : [['m'],['mi']]
                    })
                }
                ]
            },
            {
                xtype : 'compositefield',
                labelWidth : 120,
                fieldLabel : 'Begin/End Dates',
                id : this.compositeFieldDates,
                items : [
                {
                    xtype : 'datefield',
                    allowBlank: true,
                    id : this.datasetDateBeginId
                },
                {
                    xtype : 'datefield',
                    allowBlank: true,
                    id : this.datasetDateEndId
                }
                ]
            },
            {
                xtype : 'checkbox',
                fieldLabel : 'Require Full Coverage',
                id : this.requireFullCoverageId
            },{
                xtype : 'textfield',
                fieldLabel : 'E-Mail',
                id : this.datasetEmailId
            }
            ],
            buttons: [
            {
                xtype : 'button',
                text : 'Submit',
                id : 'submit-button',
                listeners : {
                    click : this.submitButtonClicked,
                    scope : this
                }
            },
            {
                xtype : 'button',
                text : 'Clear',
                id : 'clear-button'
            }
            ]
        }, config);
        PRMS.WPSConfigPanel.superclass.constructor.call(this, config);
    
        this.initializeLayerCombo();
    
    },
    comboSelectFunctionality : function(store, record) {
        var chosenLayer = record.id;
        
        Ext.Ajax.request({
            url : 'proxy?url=' + this['wfs-url'] + '/rest/workspaces/upload/datastores/' + chosenLayer + '/featuretypes/' + chosenLayer + '.json',
            disableCaching : false,
            success : function(response) {
                var responseJSON = Ext.util.JSON.decode(response.responseText);
                var attributeStore = new Ext.data.JsonStore({
                    storeId: 'attribute-store',
                    root: 'featureType.attributes.attribute',
                    idProperty: 'name',
                    fields: ['name']
                });
                attributeStore.loadData(responseJSON);  
                
                // We automatically remove the_geom as that's included automatically later - usually at 0 index but who knows...
                attributeStore.removeAt(attributeStore.findExact('name', 'the_geom'));
                
                this.remove(this.inputAttributeId);
                this.attributeCombo = new Ext.form.ComboBox({
                    id : this.inputAttributeId,
                    triggerAction: 'all',
                    fieldLabel : 'Attributes',
                    mode : 'local',
                    store : attributeStore,
                    valueField: 'name',
                    displayField: 'name',
                    editable: false,
                    width : '50%'
                })
                this.insert(1, this.attributeCombo);
                this.doLayout();
            }, 
            failure : function() {
                LOG.debug('')
            },
            scope : this
        })
        
    },
    initializeLayerCombo : function() {
        Ext.Ajax.request({
            url : 'proxy?url=' + this['wfs-url'] + '/rest/workspaces/upload/datastores.json',
            disableCaching : false,
            success : function(response) {
                var responseJSON = Ext.util.JSON.decode(response.responseText);
                if (!responseJSON.dataStores) {
                    this.layerCombo.setRawValue('Upload a shapefile');
                    this.layerCombo.setDisabled(true);
                    Ext.each(this.buttons, function(button){
                        button.setDisabled(true)
                    })
                } else {
                    var layerStore = new Ext.data.JsonStore({
                        storeId: 'layer-store',
                        root: 'dataStores.dataStore',
                        idProperty: 'name',
                        fields: ['name']
                    });
                    layerStore.loadData(responseJSON);
                
                    this.remove(this.layerComboId);
                    this.layerCombo = new Ext.form.ComboBox({
                        id : this.layerComboId,
                        fieldLabel : 'Layer',
                        emptyText : 'Choose A Layer',
                        triggerAction: 'all',
                        mode : 'local',
                        store : layerStore,
                        valueField: 'name',
                        displayField: 'name',
                        editable: false,
                        listeners : {
                            select : this.comboSelectFunctionality,
                            scope : this
                        }
                    })
                    this.insert(0, this.layerCombo);
                    Ext.each(this.buttons, function(button){
                        button.setDisabled(false)
                    })
                    this.doLayout();
                }
            }, 
            failure : function() {
                LOG.debug('')
            },
            scope : this
        })
    },
    submitButtonClicked : function() {
        var form = Ext.getCmp('panel-wpsconfig');
        var layerName = form.getComponent(this.layerComboId).getValue();
        var attribute = form.getComponent(this.inputAttributeId).getValue();
        var datasetUriAndId = function() {
            var dsUriAndId = [];
            var idCounter = 1;
            var cswFieldSetIdPre = 'csw-fieldset-';
            var cswFieldSet = form.getComponent(cswFieldSetIdPre + idCounter);
            while (cswFieldSet && cswFieldSet.items.items[0].getValue()) {
                dsUriAndId.push([cswFieldSet.items.items[0].getValue(), cswFieldSet.items.items[1].getValue(), cswFieldSet.items.items[2].getValue(), cswFieldSet.items.items[3].getValue()]);
                idCounter++;
                cswFieldSet = form.getComponent(cswFieldSetIdPre + idCounter)
            }
            return dsUriAndId;
        }();
        var datasetDateBegin = form.getComponent(this.compositeFieldDates).items.get(this.datasetDateBeginId).getValue();
        var datasetDateEnd = form.getComponent(this.compositeFieldDates).items.get(this.datasetDateEndId).getValue();
        var requireFullCoverage = form.getComponent(this.requireFullCoverageId).getValue(); 
        var datasetEmail = form.getComponent(this.datasetEmailId).getValue(); 
//        var uom = form.getComponent(this.uomInputId).getValue(); 
        
        var PRMSProcXml = new PRMS.PRMSWFGSProcess({
            attribute : attribute.split(','),
            datasetUriAndId : datasetUriAndId,
            datasetDateBegin : datasetDateBegin,
            datasetDateEnd : datasetDateEnd,
            layerName : layerName,
            requireFullCoverage : requireFullCoverage,
//            uom : uom,
            wfsUrl : this['wfs-url'] + "/wfs"
        }).createWpsExecuteRequest();
        
        Ext.Ajax.request({
            url : 'proxy?url=' + CONFIG.WPS_PROCESS_URL + '/WebProcessingService',
            method : "POST",
            xmlData : PRMSProcXml,
            scope : {
                that : this,
                initializeEmailProcess : this.initializeEmailProcess,
                email : datasetEmail
            },
            success : function(response) {
                if ($(response.responseXML).find('ns\\:ProcessStarted').length) {
                    var statusURL = $(response.responseXML).find('ns\\:ExecuteResponse').attr('statusLocation');
                    this.initializeEmailProcess({
                        statusURL : statusURL,
                        email : this.email
                    });
                } else {
                    LOG.debug('The process failed')
                }
            },
            failure : function(response) {
                LOG.debug('')
            }
        })
        
    },
    initializeEmailProcess : function(args) {
        var emailProcXml = new PRMS.EmailWhenFinishedProcess({
            email : args.email,
            'wps-checkpoint' : args.statusURL
        }).createWpsExecuteRequest();
        
        Ext.Ajax.request({
            url : 'proxy?url=' + CONFIG.WPS_UTILITY_URL + '/WebProcessingService',
            method : "POST",
            xmlData : emailProcXml,
            scope : this,
            success : function(response) {
                if ($(response.responseXML).find('ns\\:ProcessSucceeded').length) {
                    alert($(response.responseXML).find('ns\\:Output > ns\\:Data > ns\\:LiteralData').text());
                } else {
                    LOG.debug('The process failed')
                }
            },
            failure : function(response) {
                LOG.debug('')
            }
        })
    }
});
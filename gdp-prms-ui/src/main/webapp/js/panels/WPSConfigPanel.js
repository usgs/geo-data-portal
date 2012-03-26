Ext.ns("PRMS");

PRMS.WPSConfigPanel = Ext.extend(Ext.Panel, {
    layerCombo : undefined,
    'wfs-url' : undefined,
    'wps-processing-url' : undefined,
    constructor : function(config) {
        if (!config) config = {};
        //http://igsarm-cida-javadev1.er.usgs.gov:8081/geoserver/watersmart/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=watersmart:se_sites&maxFeatures=50
        this['wfs-url'] = config['wfs-url'];
        this['wps-processing-url'] = config['wps-processing-url'];

        config = Ext.apply({
            id: 'panel-wpsconfig',
            items: [],
            buttons: []
        }, config);
        PRMS.WPSConfigPanel.superclass.constructor.call(this, config);
    
        this.initializeLayerCombo();
    
    },
    initializeLayerCombo : function() {
        
        Ext.Ajax.request({
            url : 'proxy?url=' + this['wfs-url'] + '/rest/workspaces/upload/datastores.json',
            disableCaching : false,
            success : function(response) {
                var responseJSON = Ext.util.JSON.decode(response.responseText);
                var layerStore = new Ext.data.JsonStore({
                    storeId: 'layer-store',
                    root: 'dataStores.dataStore',
                    idProperty: 'name',
                    fields: ['name']
                });
                layerStore.loadData(responseJSON);
                
                var layerCombo = new Ext.form.ComboBox({
                    fieldLabel: 'Available Layers',
                    mode : 'local',
                    store : layerStore,
                    valueField: 'name',
                    displayField: 'name'
                })
                this.add(layerCombo);
                this.doLayout();
            }, 
            failure : function() {
                LOG.debug('')
            },
            scope : this
        })
    }
});
Ext.ns("GDP");

GDP.WPSCapabilitiesStore = function(meta) {
    meta = meta || {};
    
    meta.format = new OpenLayers.Format.WPSCapabilities();
    meta.fields = [
        {name: "version", type: "string"},
        {name: "languages"}, // Array of objects
        {name: "operationsMetadata"}, // Array of objects
        {name: "processOfferings"}, // Object
        {name: "serviceIdentification"}, // Object
        {name: "serviceProvider"}
    ]
    GDP.WPSCapabilitiesStore.superclass.constructor.call(
        this,
        Ext.apply(meta, {
            proxy: meta.proxy || (!meta.data ? new Ext.data.HttpProxy({url: meta.url, disableCaching: false, method: "GET"}) : undefined),
            reader: new GDP.WPSCapabilitiesReader(meta)
        })
    );
};

Ext.extend(GDP.WPSCapabilitiesStore, Ext.data.Store);

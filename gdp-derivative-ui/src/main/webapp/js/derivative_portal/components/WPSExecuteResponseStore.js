Ext.ns("GDP");

GDP.WPSExecuteResponseStore = function(meta) {
    meta = meta || {};
    
    meta.format = new OpenLayers.Format.WPSExecute();
    meta.fields = [
        {name: "version", type: "string"},
        {name: "languages"}, // Array of objects
        {name: "operationsMetadata"}, // Array of objects
        {name: "processOfferings"}, // Object
        {name: "serviceIdentification"}, // Object
        {name: "serviceProvider"}
    ]
    GDP.WPSExecuteResponseStore.superclass.constructor.call(
        this,
        Ext.apply(meta, {
            proxy: meta.proxy || (!meta.data ? new Ext.data.HttpProxy({url: meta.url, disableCaching: false, method: "GET"}) : undefined),
            reader: new GDP.WPSExecuteResponseReader(meta)
        })
    );
};

Ext.extend(GDP.WPSExecuteResponseStore, Ext.data.Store);

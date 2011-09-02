Ext.ns("GDP");

GDP.CSWGetRecordsStore = function(meta) {
    meta = meta || {};
    
    meta.format = new OpenLayers.Format.CSWGetRecords(meta.opts);
    meta.fields = [
        {name: "version", type: "string"}//,
//        {name: "languages"}, // Array of objects
//        {name: "operationsMetadata"}, // Array of objects
//        {name: "processOfferings"}, // Object
//        {name: "serviceIdentification"}, // Object
//        {name: "serviceProvider"}
    ]
    GDP.CSWGetRecordsStore.superclass.constructor.call(
        this,
        Ext.apply(meta, {
            proxy: meta.proxy || (!meta.data ? new Ext.data.HttpProxy({url: meta.url, disableCaching: false, method: "POST"}) : undefined),
            reader: new GDP.CSWGetRecordsReader(meta)
        })
    );
};

Ext.extend(GDP.CSWGetRecordsStore, Ext.data.Store);

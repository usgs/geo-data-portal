Ext.ns("GDP");

GDP.CSWGetRecordsStore = function(meta) {
    meta = meta || {};
    
    meta.format = new OpenLayers.Format.CSWGetRecords(meta.opts);
    meta.format.write();
    meta.fields = [
        {name: "identifier", type: "string"},
        {name: "derivatives"}, // Array of objects
        {name: "scenarios"}, // Array of objects
        {name: "gcms"} // Array of objects
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
            baseParams : { xmlData : meta.format.write() },
            reader: new GDP.CSWGetRecordsReader(meta)
        })
    );
};

Ext.extend(GDP.CSWGetRecordsStore, Ext.data.Store);

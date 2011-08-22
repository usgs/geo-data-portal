Ext.ns("GDP");

GDP.WPSDescribeProcessStore = function(meta) {
    meta = meta || {};
    
    meta.format = new OpenLayers.Format.WPSDescribeProcess();
    meta.fields = [
        {name: "title", type: "string"},
        {name: "storeSupported", type: "boolean"},
        {name: "statusSupported", type: "boolean"},
        {name: "processVersion", type: "string"},
        {name: "abstract", type: "string"},
        {name: "dataInputs"}, // Array
        {name: "processOutputs"} // Array
    ]
    GDP.WPSDescribeProcessStore.superclass.constructor.call(
        this,
        Ext.apply(meta, {
            proxy: meta.proxy || (!meta.data ? new Ext.data.HttpProxy({url: meta.url, disableCaching: false, method: "GET"}) : new Ext.data.MemoryProxy(meta.data)),
            reader: new GDP.WPSDescribeProcessReader(meta)
        })
    );
};

Ext.extend(GDP.WPSDescribeProcessStore, Ext.data.Store);

Ext.ns("GDP");

GDP.WPSDescribeProcessReader = function(meta, recordType) {
    meta = meta || {};
    meta.format = meta.format || new OpenLayers.Format.WPSDescribeProcess();
    
    if(typeof recordType !== "function") {
        recordType = Ext.data.Record.create(meta.fields);
    }
    GDP.WPSDescribeProcessReader.superclass.constructor.call(this, meta, recordType);
};

Ext.extend(GDP.WPSDescribeProcessReader, Ext.data.DataReader, {


    /** api: config[attributionCls]
     *  ``String`` CSS class name for the attribution DOM elements.
     *  Element class names append "-link", "-image", and "-title" as
     *  appropriate.  Default is "gx-attribution".
     */
    attributionCls: "gx-attribution",

    /** private: method[read]
     *  :param request: ``Object`` The XHR object which contains the parsed XML
     *      document.
     *  :return: ``Object`` A data block which is used by an ``Ext.data.Store``
     *      as a cache of ``Ext.data.Record`` objects.
     */
    read: function(request) {
        var data = request.responseXML;
        if(!data || !data.documentElement) {
            data = request.responseText;
        }
        return this.readRecords(data);
    },
    
    records : undefined,

    /** private: method[readRecords]
     *  :param data: ``DOMElement | String | Object`` A document element or XHR
     *      response string.  As an alternative to fetching capabilities data
     *      from a remote source, an object representing the capabilities can
     *      be provided given that the structure mirrors that returned from the
     *      capabilities parser.
     *  :return: ``Object`` A data block which is used by an ``Ext.data.Store``
     *      as a cache of ``Ext.data.Record`` objects.
     *  
     *  Create a data block containing Ext.data.Records from an XML document.
     */
    readRecords: function(data) {
        if(typeof data === "string" || data.nodeType) {
            data = this.meta.format.read(data).processDescriptions;
        }
        
        this.records = [];
        Ext.iterate(data, function(k,v,o) {
            var values = {};
            values.title = v.title;
            values.identifier = v.identifier;
            values.storeSupported = v.storeSupported;
            values.statusSupported = v.statusSupported;
            values.processVersion = v.processVersion;
            values.abstrakt = v['abstract'];
            values.dataInputs = v.dataInputs;
            values.processOutputs = v.processOutputs;
            this.records.push(new this.recordType(values)); 
        }, this);
        
        
        return {
            totalRecords: this.records.length,
            success: true,
            records: this.records
        };

    }

});

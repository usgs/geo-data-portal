Ext.ns("GDP");

GDP.CSWGetRecordsReader = function(meta, recordType) {
    meta = meta || {};
    if(!meta.format) {
        meta.format = new OpenLayers.Format.CSWGetRecords();
    }
    if(typeof recordType !== "function") {
        recordType = Ext.data.Record.create(meta.fields || [
            {name: "identifier", type: "string"},
            {name: "derivatives"}, // Array of objects
            {name: "scenarios"}, // Array of objects
            {name: "gcms"} // Array of objects
//            {name: "operationsMetadata"}, // Array of objects
//            {name: "processOfferings"}, // Object
//            {name: "serviceIdentification"}, // Object
//            {name: "serviceProvider"}
        ]
        );
    }
    GDP.CSWGetRecordsReader.superclass.constructor.call(
        this, meta, recordType
    );
};

Ext.extend(GDP.CSWGetRecordsReader, Ext.data.DataReader, {


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
            data = this.meta.format.read(data);
        }
        
        var records = [];

        Ext.iterate(data.records, function (item) {
            var values = {};
            values.identifier = item.fileIdentifier.CharacterString.value;
            values.derivatives = [];
            values.scenarios = [];
            values.gcms = [];
            var keywordTypes = item.identificationInfo[0].descriptiveKeywords;
            
            Ext.iterate(keywordTypes, function (kt) {
                if (kt.type.codeListValue === "derivative") {
                    Ext.iterate(kt.keyword, function(key) {
                        var derivArr = [];
                        derivArr.push(key.CharacterString.value);
                        values.derivatives.push(derivArr);
                    }, this);
                }
                else if (kt.type.codeListValue === "scenario") {
                    Ext.iterate(kt.keyword, function(key) {
                        var scenarioArr = [];
                        scenarioArr.push(key.CharacterString.value);
                        values.scenarios.push(scenarioArr);
                    }, this);
                }
                else if (kt.type.codeListValue === "gcm") {
                    Ext.iterate(kt.keyword, function(key) {
                        var gcmArr = [];
                        gcmArr.push(key.CharacterString.value);
                        values.gcms.push(gcmArr);
                    }, this);
                }
            }, this);
            
            records.push(new this.recordType(values));
        }, this);
//        values.identifier = data.records[0].fileIdentifier...;
//        values.languages = data.languages;
//        values.operationsMetadata = data.operationsMetadata;
//        values.processOfferings = data.processOfferings;
//        values.serviceIdentification = data.serviceIdentification;
//        values.serviceProvider = data.serviceProvider;
        //records.push(new this.recordType(values));
        
        return {
            totalRecords: records.length,
            success: true,
            records: records
        };

    }

});

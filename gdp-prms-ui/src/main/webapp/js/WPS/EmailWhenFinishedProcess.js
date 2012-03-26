Ext.ns('PRMS');

PRMS.EmailWhenFinishedProcess  = function(args) {
    if (!args) args = {};
    var _identifier = 'gs:ConstituentSummary';
    var _placeId = args['place_id'] || [];
    var that = {
        init : function(args) {
            that.placeId = args['place_id'] || [];
        },
        wpsEndpoint : "/ows?service=WPS&version=1.0.0&request=Execute&identifier=gs:ConstituentSummary",
        identifier : _identifier,
        placeId : _placeId,
        createWpsExecuteReference : function() {
                    
            var dataInputs = [];
            
            if (that.placeId) {
                Ext.each(that.placeId, function(val) {
                    dataInputs.push({
                        title : 'place_id',
                        identifier : 'place_id',
                        data : {
                            literalData : {
                                value : val
                            }
                        }
                    }) 
                }, this)
            } 
            
            return {
                mimeType : "text/xml; subtype=wfs-collection/1.0",
                href : "http://geoserver/wps",
                method : "POST",
                body : {
                    identifier : that.identifier,
                    dataInputs : dataInputs,
                    responseForm : {
                        rawDataOutput : {
                            mimeType : "text/xml; subtype=wfs-collection/1.0",
                            identifier : 'output'
                        }
                    }
                }
            };
        },
        createWpsExecuteRequest : function() {
            // To easier see what's happening here, take a look at:
            // js/openlayers/lib/OpenLayers/Format/WPSExecute.js 
            // at the writers object.
            var writer = new OpenLayers.Format.WPSExecute();
            var executeXml = writer.writeNode('wps:Execute', that.createWpsExecuteReference().body);
			
            return new OpenLayers.Format.XML().write(executeXml);
        }
    };
	
    return that;
}

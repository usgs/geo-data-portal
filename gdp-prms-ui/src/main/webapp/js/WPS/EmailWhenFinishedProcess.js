Ext.ns('PRMS');

PRMS.EmailWhenFinishedProcess  = function(args) {
    if (!args) args = {};
    var identifier = 'gov.usgs.cida.gdp.wps.algorithm.communication.EmailWhenFinishedAlgorithm';
    var wpsCheckpoint = args['wps-checkpoint'];
    var email = args.email;
    var that = {
        init : function(args) {
            that.placeId = args['place_id'] || [];
        },
        identifier : identifier,
        wpsCheckpoint : wpsCheckpoint,
        email : email,
        createWpsExecuteReference : function() {
                    
            var dataInputs = [];
            
            dataInputs.push({
                title : 'wps-checkpoint',
                identifier : 'wps-checkpoint',
                data : {
                    literalData : {
                        value : that.wpsCheckpoint
                    }
                }
            }) 

            dataInputs.push({
                title : 'email',
                identifier : 'email',
                data : {
                    literalData : {
                        value : that.email
                    }
                }
            }) 
            
            return {
                mimeType : "text/xml; subtype=wfs-collection/1.0",
                href : "http://geoserver/wps",
                method : "POST",
                body : {
                    identifier : that.identifier,
                    dataInputs : dataInputs,
                    responseForm : {
                        responseDocument : {
                            output : {
                                identifier : 'result',
                                title : 'result'
                            }
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

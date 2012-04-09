Ext.ns('PRMS');

PRMS.PRMSWFGSProcess  = function(args) {
    if (!args) args = {};
    var _identifier = 'gov.usgs.cida.gdp.wps.algorithm.PRMSParameterGeneratorAlgorithm';
    var layerName = args.layerName;
    var storeName = args.storeName || 'upload';
    var attribute = args.attribute;
    var datasetUriAndId = args.datasetUriAndId || [];
    var datasetDateBegin = args.datasetDateBegin || '';
    var datasetDateEnd = args.datasetDateEnd || '';
    var requireFullCoverage = args.requireFullCoverage; 
    var datasetEmail = args.datasetEmail; 
    var wfsUrl = args.wfsUrl;
    var uom = args.uom || 'm';
    var that = {
        init : function(args) {
        },
        identifier : _identifier,
        layerName : layerName,
        storeName : storeName,
        attribute : attribute,
        datasetUriAndId : datasetUriAndId,
        datasetDateBegin : datasetDateBegin,
        datasetDateEnd : datasetDateEnd,
        requireFullCoverage : requireFullCoverage,
        datasetEmail : datasetEmail,
        wfsUrl : wfsUrl,
        uom : uom,
        createWpsExecuteReference : function() {
                    
            var dataInputs = [];

            dataInputs.push({
                title : 'FEATURE_COLLECTION',
                identifier : 'FEATURE_COLLECTION',
                reference : {
                    href : that.wfsUrl,
                    body : {
                        wfs : new OpenLayers.Format.WFST.v1_1_0({
                            featureType: that.layerName,
                            featurePrefix: that.storeName,
                            geometryName: "the_geom",
                            outputFormat: 'text/xml; subtype=gml/3.1.1',
                            version: '1.1.0',
                            propertyNames : ['the_geom', that.attribute],
                            filter : null
                        })
                    }
                }
            })
            Ext.each(that.attribute, function(attribute) {
                this.push({
                    title : 'FEATURE_ATTRIBUTE_NAME',
                    identifier : 'FEATURE_ATTRIBUTE_NAME',
                    data : {
                        literalData : {
                            value : attribute
                        }
                    }
                }) 
            }, dataInputs)
            
            Ext.each(datasetUriAndId, function(uriAndId){
                this.push({
                    title : 'INPUT_URI',
                    identifier : 'INPUT_URI',
                    data : {
                        literalData : {
                            value : uriAndId[0]
                        }
                    }
                })
                
                this.push({
                    title : 'INPUT_ID',
                    identifier : 'INPUT_ID',
                    data : {
                        literalData : {
                            value : uriAndId[1]
                        }
                    }
                })
                
                this.push({
                    title : 'OUTPUT_ID',
                    identifier : 'OUTPUT_ID',
                    data : {
                        literalData : {
                            value : uriAndId[2]
                        }
                    }
                })
                
                this.push({
                    title : 'OUTPUT_UNIT',
                    identifier : 'OUTPUT_UNIT',
                    data : {
                        literalData : {
                            value : uriAndId[3]
                        }
                    }
                })
            }, dataInputs)
            
            if (that.datasetDateBegin) {
                dataInputs.push({
                    title : 'TIME_START',
                    identifier : 'TIME_START',
                    data : {
                        literalData : {
                            value : that.datasetDateBegin.format('Y-m-d') + 'T00:00:00.000Z'
                        }
                    }
                }) 
            }
            
            if (that.datasetDateBegin) {
                dataInputs.push({
                    title : 'TIME_END',
                    identifier : 'TIME_END',
                    data : {
                        literalData : {
                            value : that.datasetDateEnd.format('Y-m-d') + 'T00:00:00.000Z'
                        }
                    }
                }) 
            }
            
            dataInputs.push({
                title : 'REQUIRE_FULL_COVERAGE',
                identifier : 'REQUIRE_FULL_COVERAGE',
                data : {
                    literalData : {
                        value : that.requireFullCoverage
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
                            storeExecuteResponse : true,
                            status : true,
                            output : {
                                identifier : 'OUTPUT',
                                title : 'OUTPUT',
                                asReference : true
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
        },
        createWfsGetFeatureRequest : function(args) {
            var attribute = args.attribute;
            var layerName = args.layerName;
            var storeName = args.storeName;
            var filter = args.filter;
            
            var xml = '';
            xml += '<wfs:GetFeature service="WFS" version="1.1.0" outputFormat="text/xml; subtype=gml/3.1.1" xmlns:wfs="http://www.opengis.net/wfs" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengis.net/wfs ../wfs/1.1.0/WFS.xsd">'
            + '<wfs:Query typeName="'+storeName+':'+layerName+'">'
            + '<wfs:PropertyName>the_geom</wfs:PropertyName>'
            + '<wfs:PropertyName>'+attribute+'</wfs:PropertyName>'
            //                + filter ? '' : ''
            + '</wfs:Query>'
            + '</wfs:GetFeature>';
            
            return xml;
        }
    };
	
    return that;
}

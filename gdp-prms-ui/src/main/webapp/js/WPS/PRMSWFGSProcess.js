Ext.ns('PRMS');

PRMS.PRMSWFGSProcess  = function(args) {
    if (!args) args = {};
    var _identifier = 'gov.usgs.cida.gdp.wps.algorithm.PRMSFeatureWeightedGridStatisticsAlgorithm';
    var layerName = args.layerName;
    var storeName = args.storeName || 'upload';
    var attribute = args.attribute;
    var datasetUri = args.datasetUri;
    var datasetId = args.datasetId;
    var datasetDateBegin = args.datasetDateBegin || '';
    var datasetDateEnd = args.datasetDateEnd || '';
    var requireFullCoverage = args.requireFullCoverage; 
    var datasetEmail = args.datasetEmail; 
    var wfsUrl = args.wfsUrl
    var that = {
        init : function(args) {
        },
        wpsEndpoint : "/ows?service=WPS&version=1.0.0&request=Execute&identifier=gov.usgs.cida.gdp.wps.algorithm.PRMSFeatureWeightedGridStatisticsAlgorithm",
        identifier : _identifier,
        layerName : layerName,
        storeName : storeName,
        attribute : attribute,
        datasetUri : datasetUri,
        datasetId : datasetId,
        datasetDateBegin : datasetDateBegin,
        datasetDateEnd : datasetDateEnd,
        requireFullCoverage : requireFullCoverage,
        datasetEmail : datasetEmail,
        wfsUrl : wfsUrl,
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
            
            dataInputs.push({
                title : 'FEATURE_ATTRIBTUE_NAME',
                identifier : 'FEATURE_ATTRIBTUE_NAME',
                data : {
                    literalData : {
                        value : that.attribute
                    }
                }
            }) 
            
            dataInputs.push({
                title : 'DATASET_URI',
                identifier : 'DATASET_URI',
                data : {
                    literalData : {
                        value : that.datasetUri
                    }
                }
            }) 
            
            dataInputs.push({
                title : 'DATASET_ID',
                identifier : 'DATASET_ID',
                data : {
                    literalData : {
                        value : that.datasetId
                    }
                }
            }) 
            
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
                        output : {
                            identifier : 'OUTPUT',
                            title : 'OUTPUT',
                            asReference : true
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

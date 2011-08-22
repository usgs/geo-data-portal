Ext.ns('GDP');

GDP.FeatureCoverageOPeNDAPIntersection  = function(args) {
    var _identifier = 'gov.usgs.cida.gdp.wps.algorithm.FeatureCoverageOPeNDAPIntersectionAlgorithm';
    var _abstract = args.abstrakt || undefined;
    var _processVersion = args.processVersion || undefined;
    var _statusSupported = args.statusSupported || undefined;
    var _storeSupported = args._storeSupported || undefined;
    var _dataInputs = args.dataInputs || undefined;
    var _processOutputs = args.processOutputs || undefined;
    var _bounds = args.bounds || undefined;
    var _fullCoverage = args.fullCoverage || true;
    var _datasetUri = args.datasetUri || undefined;
    var _datasetId = args.datasetId || undefined;
    var _areaTypes = ['bbox'];
    var _title = args.title || undefined;
    var _configurable = false;
    var _runningProcessUrl = args.runningProcessUrl || undefined;
    return {
        init : function(args) {
            this.bounds = args.bounds || this.bounds;
            this.abstrakt = args.abstrakt || this.abstrakt;
            this.processVersion = args.abprocessVersionstrakt || this.processVersion;
            this.statusSupported = args.statusSupported || this.statusSupported;
            this.storeSupported = args.storeSupported || this.storeSupported; 
            this.dataInputs = args.dataInputs || this.dataInputs;
            this.processOutputs = args.processOutputs || this.processOutputs;
            this.fullCoverage = args.fullCoverage || this.fullCoverage;
            this.datasetUri = args.datasetUri || this.datasetUri;
            this.datasetId = args.datasetId || this.datasetId;
            this.title = args.title || this.title;
            this.areaTypes = args.areaTypes || this.areaTypes;
            this.runningProcessUrl = args.runningProcessUrl || this.runningProcessUrl;
        },
        bounds : _bounds,
        abstrakt : _abstract,
        processVersion : _processVersion,
        statusSupported : _statusSupported,
        storeSupported : _storeSupported,
        dataInputs : _dataInputs,
        processOutputs : _processOutputs,
        fullCoverage : _fullCoverage,
        datasetUri : _datasetUri,
        datasetId : _datasetId,
        identifier : _identifier,
        configurable : _configurable,
        areaTypes : _areaTypes,
        title : _title,     
        runningProcessUrl : _runningProcessUrl,
        getConfigurables : function() {
            return null
        },
        createWpsExecuteRequest : function() {
            return function(args) {
                // To easier see what's happening here, take a look at:
                // js/openlayers/lib/OpenLayers/Format/WPSExecute.js 
                // at the writers object.
                var writer = new OpenLayers.Format.WPSExecute();
                var executeXml = writer.writeNode('wps:Execute', {
                    identifier : args.scope.identifier,
                    dataInputs : [
                    {
                        title : 'FEATURE_COLLECTION',
                        identifier : 'FEATURE_COLLECTION',
                        data : {
                            complexData : {
                                mimeType : 'text/xml',
                                encoding : 'UTF-8',
                                schema : 'http://schemas.opengis.net/gml/3.1.1/base/feature.xsd',
                                value : ''
                            }
                        }
                    },
                    {
                        title : 'DATASET_URI',
                        identifier : 'DATASET_URI',
                        data : {
                            literalData : {
                                value : args.scope.datasetUri
                            }
                        }
                    },
                    {
                        title : 'DATASET_ID',
                        identifier : 'DATASET_ID',
                        data : {
                            literalData : {
                                value : args.scope.datasetId
                            }
                        }
                    },
                    {
                        title : 'REQUIRE_FULL_COVERAGE',
                        identifier : 'REQUIRE_FULL_COVERAGE',
                        data : {
                            literalData : {
                                value : args.scope.fullCoverage
                            }
                        }
                    }
                    ],
                    responseForm : {
                        responseDocument : {
                            storeExecuteResponse : true,
                            status : true,
                            output : {
                                asReference : true,
                                identifier : 'OUTPUT',
                                title : 'OUTPUT',
                                'abstract' : ''
                            }
                        }
                    }
                })
                // We have to do this here. The reason being that
                // the way the OpenLayers writer does this is to 
                // encode this data and that doesn't work for us
                // so we will shimmy the node into the XML
                executeXml.getElementsByTagName('wps:ComplexData')[0].appendChild(args.scope.createWfsFeatureXml().childNodes[0]);
                return executeXml;
            }({
                scope : this
            });
        },
        createWfsFeatureXml : function() {
            LOG.debug('WPSPanel:createWfsXml: Creating WFS XML.');
            if (!this.bounds) return null;
            
            var left = this.bounds.left; 
            var right = this.bounds.right; 
            var top = this.bounds.top; 
            var bottom = this.bounds.bottom; 
            var now = new Date();
            var timestamp = now.format('c'); 
            var externalPortalMapping = 'http://cida.usgs.gov/qa/climate/derivative/xsd/draw.xsd';
        
            var result = '<gml:featureMembers xmlns:ogc="http://www.opengis.net/ogc" xmlns:draw="gov.usgs.cida.gdp.draw" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:ows="http://www.opengis.net/ows" xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink" xsi:schemaLocation="gov.usgs.cida.gdp.draw '+externalPortalMapping+'">';
            result += '<gml:box gml:id="box.1">';
            result += '<gml:the_geom>';
            result += '<gml:MultiPolygon srsDimension="2" srsName="http://www.opengis.net/gml/srs/epsg.xml#4326">';
            result += '<gml:polygonMember>';
            result += '<gml:Polygon>';
            result += '<gml:exterior>';
            result += '<gml:LinearRing>';
            result += '<gml:posList>';
            result += left + " " + top + " ";
            result += left + " " + bottom + " ";
            result += right + " " + bottom + " ";
            result += right + " " + top + " ";
            result += left + " " + top;
            result += '</gml:posList>'
            result += '</gml:LinearRing>'
            result += '</gml:exterior>'
            result += '</gml:Polygon>'
            result += '</gml:polygonMember>'
            result += '</gml:MultiPolygon>'
            result += '</gml:the_geom>'
            result += '<gml:ID>0</gml:ID>'
            result += '</gml:box>'
            result += '</gml:featureMembers>'
        
        
            var doc;
            if(window.ActiveXObject){
                doc = new ActiveXObject("Microsoft.XMLDOM");
                doc.async = "false";
                doc.loadXML(result);
            }else{
                doc = new DOMParser().parseFromString(result,"text/xml");
            }
            return doc;
        }
    }
}

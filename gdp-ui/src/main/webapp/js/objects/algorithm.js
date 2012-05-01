var Algorithm = function() {
    var _algorithms;
    var _USER_CONFIGURABLES = ['FEATURE_COLLECTION','FEATURE_ATTRIBUTE_NAME','DATASET_URI','DATASET_ID', 'TIME_START', 'TIME_END'];
    
    function _needsConfiguration(algorithm) {
        var inputs = new Array();
        $.each(algorithm.inputs, function(k,v) {
            if ($.inArray(k,_USER_CONFIGURABLES) == -1) inputs.push(k);
        })
        if (inputs.length) return true;
        return false;
    }
    
    return {
        algorithms : _algorithms,
        userConfigurables : _USER_CONFIGURABLES,
        init : function() {
            logger.debug("GDP: Getting algorithms from server.");
            this.algorithms = new Object();
            var wpsURL = Constant.endpoint.proxy + Constant.endpoint.processwps;
            var singleAlgorithm = function(){
                var splitAlgorithm = Constant.ui.view_algorithm_list.split(',');
                if (splitAlgorithm.length != 1) return '';
                return splitAlgorithm[0];
            }();
                
            var _this = this;
            // We first create a getcapabilities request, and then we send a describe process 
            // request for each algorithm in that request, creating an algorithm array from that
            WPS.sendWPSGetRequest(wpsURL, WPS.getCapabilitiesParams, false, function(getCapsXML) {
                $(getCapsXML).find('ns|Process').each(function() {
                    var processID = $(this).find('ns1|Identifier').text();
                    if (singleAlgorithm && processID != singleAlgorithm) return true;
                    WPS.sendWPSGetRequest(wpsURL, WPS.describeProcessParams(processID), false, function(describeProcessXML) {
                        var identifier = $(describeProcessXML).find('ns1|Identifier:first').text();
                        var title = $(describeProcessXML).find('ns1|Title:first').text();
                        var abstrakt = $(describeProcessXML).find('ns1|Abstract:first').text();
                        var inputs = {};
                        
                        WPS.processDescriptions[identifier] = describeProcessXML;
                        
                        // Create the inputs
                        $(describeProcessXML).find('ns|ProcessDescriptions').find('Input').each(function(i,v){
                            var inputIdentifier = $(v).find('ns1|Identifier').text();
                            var title = $(v).find('ns1|Title').text();
                            var minOccurs = $(v).attr('minOccurs');
                            var maxOccurs = $(v).attr('maxOccurs');
                            var reference = '';
                            var allowedValues = '';
                            var type = function(){
                                if ($(v).find('ComplexData').length > 0) return 'complex';
                                if ($(v).find('LiteralData').length > 0) return 'literal';
                                if ($(v).find('BoundingBox').length > 0) return 'bbox';
                                return '';
                            }();
                            
                            if (type == 'literal') { // TODO- We're not using the xmlns plugin to pull attribute. Figure out why it doesn't work with it
                                reference = $(v).find('LiteralData').find('ns1|DataType').attr('ns1:reference');
                                allowedValues = function(){
                                    if ($(v).find('LiteralData').find('ns1|AnyValue').length > 0) return ['*'];
                                    else return function(){
                                        var valArr = [];
                                        $(v).find('LiteralData').find('ns1|AllowedValues').find('ns1|Value').each(function(valI, valV) {
                                            valArr.push($(valV).text())
                                        });
                                        return valArr;
                                    }()
                                }()
                            } else if (type == 'complex') {
                                var complexFormats = [];  
                                $(v).find('ComplexData Format').each(function(i,v) {
                                    var format = {};
                                    format['type'] = $(v).parent()[0].nodeName.toLowerCase();
                                    format['mimeType'] = $(v).find('MimeType').text();
                                    format['encoding'] = $(v).find('Encoding').text(); 
                                    format['schema'] = $(v).find('Schema').text(); 
                                    complexFormats.push(format);
                                })
                            } else if (type == 'bbox') {
                                //TODO- Once we have BBOX algorithms, parse those. Leave out for now
                            }
                            
                            inputs[inputIdentifier] = {
                                'maxOccurs' : maxOccurs,
                                'minOccurs' : minOccurs,
                                'title' : title,
                                'type' : type,
                                'reference' : reference,
                                'allowedValues' : allowedValues
                            }
                        });
                        
                        _this.algorithms[identifier] = {
                            'title' : title,
                            'abstrakt' : abstrakt,
                            'xml' : describeProcessXML,
                            'inputs' : inputs
                        };
                        _this.algorithms[identifier].needsConfiguration = _needsConfiguration(_this.algorithms[identifier]);
                        logger.debug("GDP: Created " + title + " algorithm.");
                    });
                });
            });
        },
        isPopulated : function() {
            for (member in this.algorithms) return true;
            return false;
        }
    }
}
    
    

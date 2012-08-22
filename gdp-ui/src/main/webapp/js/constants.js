var Constant = function() {
    var _algorithms;
    var _UI = {
        'fadespeed' : 250
    };
    
    var _INCOMING = {};
    
    var _ENDPOINT  = {
        'properties' : 'PropertiesServlet',
        // Science Base Specific
        'redirect_url' : 'https://beta.sciencebase.gov/catalog/gdp/landing'
    };
    var _NAMESPACES = {
        'cat' : 'http://www.esri.com/metadata/csw/',
        'csw' : 'http://www.opengis.net/cat/csw/2.0.2',
        'dc'  : 'http://purl.org/dc/elements/1.1/',
        'dct' : 'http://purl.org/dc/terms/',
        'gco' : 'http://www.isotc211.org/2005/gco',
        'geonet' : 'http://www.fao.org/geonetwork',
        'gmd' : 'http://www.isotc211.org/2005/gmd',
        'gmi' : 'http://www.isotc211.org/2005/gmi',
        'gml' : 'http://www.opengis.net/gml',
        'gn' : 'http://geonetwork-opensource.org',
        'gsr' : 'http://www.isotc211.org/2005/gsr',
        'gss' : 'http://www.isotc211.org/2005/gss',
        'gts' : 'http://www.isotc211.org/2005/gts',
        'gmx' : 'http://www.isotc211.org/2005/gmx',
        'nc' : 'http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2',
        'ns' : 'http://www.opengis.net/wps/1.0.0',
        'ns1' : 'http://www.opengis.net/ows/1.1',
        'ows' : 'http://www.opengis.net/ows',
        'srv' : 'http://www.isotc211.org/2005/srv',
        'wfs' : 'http://www.opengis.net/wfs',
        'xsd' : 'http://www.w3.org/2001/XMLSchema',
        'wcs11' : 'http://www.opengis.net/wcs/1.1',
        'wcs111' : 'http://www.opengis.net/wcs/1.1.1'
    };
    
    // Is the incoming request from ScienceBase?
    var _IS_SB = false;
    
    // When passed an array of keys and a value, returns the corresponding json
    // object. E.g. buildJsonFromProperty(['map', 'layer1'], 'layer1url') ==
    // { 'map' : { 'layer1' : 'layer1url' } }.
    function buildJsonFromProperty(keys, value) {
        // if key array is empty
        if (!keys[0]) return value;

        var ob = {};
        ob[keys[0]] = buildJsonFromProperty(keys.slice(1), value);

        return ob;
    }
    
    // Ripped directly from OpenLayers
    function getUrlParameters(url) {
        // if no url specified, take it from the location bar
        url = (url === null || url === undefined) ? window.location.href : url;

        //parse out parameters portion of url string
        var paramsString = "";
        if (url.indexOf('?') != -1) {
            var start = url.indexOf('?') + 1;
            var end = url.indexOf('#') != -1 ? url.indexOf('#') : url.length;
            paramsString = url.substring(start, end);
        }

        var parameters = {};
        var pairs = paramsString.split(/[&;]/);
        for(var i=0, len=pairs.length; i<len; ++i) {
            var keyValue = pairs[i].split('=');
            if (keyValue[0]) {

                var key = keyValue[0];
                try {
                    key = decodeURIComponent(key);
                } catch (err) {
                    key = unescape(key);
                }
            
                // being liberal by replacing "+" with " "
                var value = (keyValue[1] || '').replace(/\+/g, " ");

                try {
                    value = decodeURIComponent(value);
                } catch (err) {
                    value = unescape(value);
                }
            
                // follow OGC convention of comma delimited values
                value = value.split(",");

                //if there's only one value, do not return as array                    
                if (value.length == 1) {
                    value = value[0];
                }                
            
                parameters[key] = value;
            }
        }
        return parameters;
    }

    return {
        ui : _UI,
        endpoint : _ENDPOINT,
        incoming : _INCOMING,
        namespaces : _NAMESPACES,
        algorithms : _algorithms,
        selectString : '<select></select>',
        optionString : '<option></option>',
        labelString : '<label></label>',
        divString : '<div></div>',
        inputString : '<input></input>',
        spanString : '<span></span>',
        tableString : '<table></table>',
        tableRowString : '<tr></tr>',
        tableDataString : '<td></td>',
        init: function() {
            logger.debug("GDP: Beginning Constants initialization.");
            
            $().xmlns(this.namespaces);
            
            this.getConstantsFromServer();

            $.extend(
                true,
                Constant.incoming,
                getUrlParameters()
            );
            
            // By this point, the ScienceBase object has initialized and 
            // may have incoming parameters. Use those to set our params 
            // here.
            $.each(ScienceBase.endpoints, function(key, value) {
                if (key === 'feature_wms') {
                    Constant.endpoint.wms = value;
                }
                
                if (key === 'coverage_wcs') {
                    Constant.endpoint.wcs = value;
                }
                
                if (key === 'feature_wfs') {
                    Constant.endpoint.wfs = value;
                }
                
                if (key === 'redirect_url') {
                    Constant.endpoint['redirect_url'] = value;
                }
            })
        },
        getConstantsFromServer : function() {
            logger.debug("GDP: Getting constant values from server.");
            $.ajax( {
                url : Constant.endpoint.properties,
                data : {
                    'command' : 'getprops'
                },
                async: false, // Until we have all of our constants, we do not want to load the rest of the application
                processData : true,
                dataType : 'xml',
                contentType : 'text/xml',
                success : function(data, textStatus, XMLHttpRequest) {
                    Constant.xml = data;
                    $(data).find('entry').each(function(sIndex, sElement) {
                        // Do recursive merge of existing Constant object and
                        // new object created from current property. Conflicting
                        // keys will be set to Constant's value of that key.
                        Constant = $.extend(
                            true,
                            {}, 
                            buildJsonFromProperty($(sElement).attr('key').split('.'), ($(sElement).text() == undefined) ? '' : $(sElement).text()),
                            Constant
                            );
                    });
                },
                error : function(jqXHR, textStatus, errorThrown) {
                    throw({
                        name : textStatus,
                        message : "Could not get constants from server. Application can not continue.",
                        errorObject : errorThrown
                    })
                }
            });
        }
    };
};
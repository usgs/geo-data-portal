var Constant = function() {
    var _algorithms;
    var _UI = {'fadespeed' : 250};
    var _ENDPOINT  = {'properties' : 'PropertiesServlet'};
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
        'xsd' : 'http://www.w3.org/2001/XMLSchema'
    };
    
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

    return {
        ui : _UI,
        endpoint : _ENDPOINT,
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
            this.getConstantsFromServer();
            $().xmlns(this.namespaces);
            
        },
        getConstantsFromServer : function() {
            logger.debug("GDP: Getting constant values from server.");
            $.ajax( {
                url : Constant.endpoint.properties,
                data : {'command' : 'getprops'},
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
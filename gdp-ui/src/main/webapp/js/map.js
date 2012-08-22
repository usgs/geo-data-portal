var map;
var geometryOverlay;
var highlightGeometryOverlay;
var drawFeatureLayer;
var datasetOverlay;
var watersControl;
var drawFeatureControl;
var saveStrategy;

var _NUM_ZOOM_LEVELS = 18;
var _PROJ900913 = new OpenLayers.Projection("EPSG:900913")
var _PROJ4326 = new OpenLayers.Projection("EPSG:4326");
var _DEFAULT_ATTRIBUTE = 'ID'; // placeholder attribute added by us
var _FEATURE_TYPE_NAME = '#featuretypeName';

// When using an XYZ layer with OpenLayers, this needs to be appended to the
// base URL.
var _XYZ_URL_POSTFIX = '${z}/${y}/${x}';

OpenLayers.Control.Click = OpenLayers.Class(OpenLayers.Control, {
    defaultHandlerOptions: {
        'single': true,
        'double': false,
        'pixelTolerance': 0,
        'stopSingle': false,
        'stopDouble': false
    },

    initialize: function(options) {
        this.handlerOptions = OpenLayers.Util.extend(
            {}, this.defaultHandlerOptions
        );
        OpenLayers.Control.prototype.initialize.apply(
            this, arguments
        );
        this.handler = new OpenLayers.Handler.Click(
            this, {
               'click': this.trigger
            }, this.handlerOptions
        );
    },

    trigger: function(e) {

        if ($(_FEATURE_TYPE_NAME).val() == '') {
            alert('Please specify name for waters featuretype.');
            $(_FEATURE_TYPE_NAME).focus();
            return;
        }

        // Release waters button
        AOI.releaseToggleButton($('#watersButton')[0]);

        var lonlat = map.getLonLatFromViewPortPx(e.xy);
        lonlat.transform(map.getProjectionObject(), _PROJ4326);
        var name = $(_FEATURE_TYPE_NAME).val();

        var wpsAlgo = 'gov.usgs.cida.gdp.wps.algorithm.filemanagement.GetWatersGeom';

        var wpsInputs = {
            'lat': [lonlat.lat],
            'lon': [lonlat.lon],
            'name' : [name]
        }

        var wpsOutputs = ['layer-name'];

        WPS.sendWpsExecuteRequest(Constant.endpoint.proxy + Constant.endpoint.utilitywps, wpsAlgo, wpsInputs, wpsOutputs, false, watersCallback);
    }
});

function watersCallback(data) {
    if (!WPS.checkWpsResponse(data, 'Error getting geometry from waters.')) return;

    var layerName;

    // get layer name from data
    $(data).find('ns|Output').each(function(index, el) {
        if ($(el).find('ns1|Identifier:contains("layer-name")')) {
            layerName = $(el).find('ns|LiteralData').text();
            setGeometryOverlay(layerName);
        }
    });

    AOI.updateFeatureTypesListAndSelect(layerName);
}

function submitDrawFeature() {
    var name = $(_FEATURE_TYPE_NAME).val();

    if (drawFeatureLayer.features.length == 0) {
        showErrorNotification('Please draw a polygon on the map.');
        return;
    }
    
    if (name == '') {
        showErrorNotification('Please specify name for feature type.');
        $(_FEATURE_TYPE_NAME).focus();
        return;
    }
    
    if (/\W/.test(name) || /^[^A-Za-z]/.test(name)) {
        showErrorNotification('Name must begin with a letter, and may only contain letters, numbers, and underscores.');
        $(_FEATURE_TYPE_NAME).focus();
        return;
    }

    // OpenLayers trunk has a nice protocol.setFeatureType api method which
    // does this for us.  But, with 2.10, we're stuck doing it manually.
    drawFeatureLayer.protocol.featureType = name;
    drawFeatureLayer.protocol.format.featureType = name;

    var wpsAlgo = 'gov.usgs.cida.gdp.wps.algorithm.filemanagement.CreateNewShapefileDataStore';
    var wpsInputs = {'name' : [name]}
    var wpsOutputs = ['layer-name'];

    

    WPS.sendWpsExecuteRequest(Constant.endpoint.proxy + Constant.endpoint.utilitywps,
        wpsAlgo, wpsInputs, wpsOutputs, false, drawFeatureCallback);
}

function drawFeatureCallback(data) {
    if (!WPS.checkWpsResponse(data, 'Error creating draw feature geometry.')) {
        return;
    }

    var layerName;

    // get layer name from data
    $(data).find('ns|Output').each(function(index, el) {
        if ($(el).find('ns1|Identifier:contains("layer-name")')) {
            layerName = $(el).find('ns|LiteralData').text();
        }
    });

    saveStrategy.events.remove('success');

    saveStrategy.events.register('success', null, function() {
        // GDP requires having at least one attribute value, so send an update
        // transaction to set the 'ID' attribute to 0 for the newly created geometry.
        updateFeatureTypeAttribute(layerName, _DEFAULT_ATTRIBUTE, 0, function(data) {
            // Release draw feature button
            AOI.releaseToggleButton($('#drawFeatureButton')[0]);
            clearDrawFeatureLayer();
            AOI.updateFeatureTypesListAndSelect(layerName);
        });
    });

    saveStrategy.save();
}

function updateFeatureTypeAttribute(featureType, attribute, value, callback) {

    var updateTransaction =
        '<?xml version="1.0"?>' +
        '<wfs:Transaction xmlns:ogc="http://www.opengis.net/ogc" ' +
                         'xmlns:wfs="http://www.opengis.net/wfs" ' +
                         'xmlns:gml="http://www.opengis.net/gml" ' +
                         'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" ' +
                         'version="1.1.0" service="WFS" '+
                         'xsi:schemaLocation="http://www.opengis.net/wfs ../wfs/1.1.0/WFS.xsd">' +
          '<wfs:Update typeName="' + featureType + '">' +
            '<wfs:Property>' +
              '<wfs:Name>' + attribute + '</wfs:Name>' +
              '<wfs:Value>' + value + '</wfs:Value>'+
            '</wfs:Property>'+
          '</wfs:Update>'+
        '</wfs:Transaction>';

    $.ajax({
        url: Constant.endpoint.proxy + Constant.endpoint.wfs,
        type: 'POST',
        contentType: 'application/xml',
        data: updateTransaction,
        success: function(data, textStatus, jqXHR) {
            callback(data);
        }
    });
}

function clearDrawFeatureLayer() {
    drawFeatureLayer.removeAllFeatures();
}

function setDatasetOverlay(wmsURL, wmsLayerName) {
    logger.debug('GDP: Setting new dataset overlay for layer name :' + wmsLayerName);

    if ($.inArray(datasetOverlay, map.layers) > -1) {
        logger.debug('GDP: Removing previous dataset overlay layer.');
        map.removeLayer(datasetOverlay);
    }

    datasetOverlay = new OpenLayers.Layer.WMS(
        "Dataset Overlay",
        wmsURL,
        {
            layers: wmsLayerName,
            transparent: 'true',
            isBaseLayer: false
        },
        {
            opacity: 0.5
        }
    );

    logger.debug('GDP: Adding dataset overlay layer.');
    map.addLayer(datasetOverlay);
    map.setLayerIndex(datasetOverlay, 1); // Dataset layer should always be below geometry layer
}

// TODO: refactor to remove duplicated code between this and setDatasetOverlay
function setGeometryOverlay(wmsLayerName) {
    // Remove previous overlay, if one exists
    if ($.inArray(geometryOverlay, map.layers) > -1) map.removeLayer(geometryOverlay);
    if ($.inArray(highlightGeometryOverlay, map.layers) > -1) map.removeLayer(highlightGeometryOverlay);

    // unselected features
    geometryOverlay = new OpenLayers.Layer.WMS(
        "Geometry Overlay",
        Constant.endpoint.proxy + Constant.endpoint.wms,
        {
            layers: wmsLayerName,
            transparent: 'true',
            isBaseLayer: false,
            // ScienceBase does not have 'Polygon' as a set style. 
            styles: ScienceBase.useSB ? 'MultiPolygon' : 'polygon'
        },
        {
            opacity: 0.7
        }
    );

    // selected features
    highlightGeometryOverlay = new OpenLayers.Layer.WMS(
        "Geometry Highlight Overlay",
        Constant.endpoint.proxy + Constant.endpoint.wms,
        {
            layers: wmsLayerName,
            transparent: 'true',
            isBaseLayer: false,
            styles: 'highlight'
        },
        {
            opacity: 0.8
        }
    );

    highlightFeatures(null, '*'); // highlight all features

    map.addLayer(geometryOverlay);
    map.addLayer(highlightGeometryOverlay);

    zoomToLayer(wmsLayerName);
}

function highlightFeatures(attr, attrValues) {
    var hlCqlFilter, baseCqlFilter;

    if (attrValues == '*') {
        // Draw all features using highlight
        hlCqlFilter = "INCLUDE";
        baseCqlFilter = "EXCLUDE";
    } else {
        var cqlFilter = " IN ('" + attrValues[0] + "'";

        for (var i = 1; i < attrValues.length; i++) {
            cqlFilter += ", '" + attrValues[i] + "'";
        }

        cqlFilter += ")"

        var escapedAttr = "\"" + attr + "\"";
        hlCqlFilter = escapedAttr + cqlFilter;
        baseCqlFilter = escapedAttr + " NOT" + cqlFilter;
    }

    geometryOverlay.mergeNewParams({'cql_filter': encodeURIComponent(baseCqlFilter)});
    highlightGeometryOverlay.mergeNewParams({'cql_filter': encodeURIComponent(hlCqlFilter)});
}

function zoomToLayer(layerName) {
            // Unfortunately there's no jQuery selector that matches inner text exactly.
            // There's :contains('str'), but that only tests that 'str' is a substring.
            logger.debug('GDP: Zooming to layer. Using cached getCapabilities document from WFS server.');
            $(WFS.cachedGetCapabilities).find('FeatureType').each(function(i, elem) {
                if ($(elem).find('Name').text() == layerName) {
                    var bbox = $(elem).find('ows|WGS84BoundingBox');
                    var lowerCorner = $(bbox).find('ows|LowerCorner').text().split(' ');
                    var upperCorner = $(bbox).find('ows|UpperCorner').text().split(' ');

                    var minx = lowerCorner[0];
                    var miny = lowerCorner[1];
                    var maxx = upperCorner[0];
                    var maxy = upperCorner[1];

                    map.zoomToExtent(new OpenLayers.Bounds(minx, miny, maxx, maxy));
                }
            });
}

function clearGeometryOverlay() {
    logger.debug('GDP: Removing geometry overlay from map.');
    if ($.inArray(geometryOverlay, map.layers) > -1) map.removeLayer(geometryOverlay);
    if ($.inArray(highlightGeometryOverlay, map.layers) > -1) map.removeLayer(highlightGeometryOverlay);
}

function clearDatasetOverlay() {
    logger.debug('GDP: Clearing dataset overlay');
    if ($.inArray(datasetOverlay, map.layers) > -1) map.removeLayer(datasetOverlay);
}

function onMapZoom() {
    if (this.getZoom() >= 7 && this.baseLayer == blueMarbleLayer) {
        logger.trace('GDP: Map switching to "naip1mLayer"');
        this.removeLayer(this.baseLayer);
        this.addLayer(naip1mLayer);

    } else if (this.getZoom() < 7 && this.baseLayer == naip1mLayer) {
        logger.trace('GDP: Map switching to "blueMarbleLayer"');
        this.removeLayer(this.baseLayer);
        this.addLayer(blueMarbleLayer);
    }
}

/**
 * Create the map layer. 
 */
function initMap(options) {
    logger.info('GDP: Initializing map.');
    OpenLayers.IMAGE_RELOAD_ATTEMPTS = 3;

    map = new OpenLayers.Map('map-div', {
        numZoomLevels: _NUM_ZOOM_LEVELS,
        controls: [
            new OpenLayers.Control.Navigation(),
            new OpenLayers.Control.ArgParser(),
            new OpenLayers.Control.Attribution(),
            new OpenLayers.Control.LayerSwitcher(),
            new OpenLayers.Control.PanZoomBar(),
            new OpenLayers.Control.MousePosition(),
            new OpenLayers.Control.ScaleLine()
        ],
        // Got this number from Hollister, and he's not sure where it came from.
        // Without this line, the esri road and relief layers will not display
        // outside of the upper western hemisphere.
        maxResolution: 1.40625/2
    });

    saveStrategy = new OpenLayers.Strategy.Save();

    drawFeatureLayer = new OpenLayers.Layer.Vector('Draw Polygon Layer', {
        strategies: [new OpenLayers.Strategy.BBOX(), saveStrategy],
        projection: new OpenLayers.Projection('EPSG:4326'),
        protocol: new OpenLayers.Protocol.WFS({
            version: '1.1.0',
            srsName: 'EPSG:4326',
            url: Constant.endpoint.proxy + Constant.endpoint.wfs,
            featureNS :  'gov.usgs.cida.gdp.draw',
            featureType : 'temp', // this gets changed before submitting geometry
            geometryName: 'the_geom'
        })
    });
    map.addLayer(drawFeatureLayer);

    drawFeatureControl = new OpenLayers.Control.DrawFeature(
        drawFeatureLayer,
        OpenLayers.Handler.Polygon,
        {multi : true}
    );
    map.addControl(drawFeatureControl);

    watersControl = new OpenLayers.Control.Click();
    map.addControl(watersControl);

    // Read and add layers specified in config.jsp
    var tileLayers = parseLayerDefinition(Constant.map.layers.tile);
    var wmsLayers = parseLayerDefinition(Constant.map.layers.wms);

    $(tileLayers).each(function(index, layerOb) {
        setupLayer(layerOb, 'tile', Constant.map.default_layer);
    });

    $(wmsLayers).each(function(index, layerOb) {
        setupLayer(layerOb, 'WMS', Constant.map.default_layer);
    });

    map.setCenter(new OpenLayers.LonLat(-96, 38), 4);
    logger.info('GDP: Map initialized.');
    // Change layer based on zoom level.
    //map.events.register("zoomend", null, onMapZoom);
}

function parseLayerDefinition(layersDefOb) {
    
    if (!layersDefOb) return null;

    var parsedLayers = [];

    $.each(layersDefOb, function(key, val) {
        // split into name and url/params
        var layer = val.split(',');
        
        // split into url and params
        var urlAndParams = layer[1].split('?');

        var layerParams = {
            transitionEffect: 'resize',
            projection: 'EPSG:4326'
        };

        $(urlAndParams[1].split('&')).each(function(index, elem) {
            var param = elem.split('=');
            layerParams[param[0]] = param[1];
        });

        var layerOb = {
            id: key,
            name: layer[0],
            url: urlAndParams[0],
            params: layerParams,
            OLparams: {} // parameters passed to OpenLayers, such as opacity
        };

        parsedLayers.push(layerOb);
    });

    return parsedLayers;
}

function setupLayer(layerOb, type, defaultLayer) {
    // In order for OpenLayers to make the layer a true overlay (that can be
    // drawn on top of a base layer), transparent needs to be true.
    if (layerOb.params['isBaseLayer'] == 'false') {
        layerOb.params['transparent'] = 'true';
        layerOb.OLparams['opacity'] = .7;
    }

    var layer;
    if (type == 'tile') {
        // Make sure url has trailing /
        if (layerOb.url.search(/\/$/) == -1) layerOb.url = layerOb.url + '/';

        // Add tile cache specific url parameters
        layerOb.url = layerOb.url + _XYZ_URL_POSTFIX;

        layer = new OpenLayers.Layer.XYZ(
            layerOb.name,
            layerOb.url,
            layerOb.params,
            layerOb.OLparams
        );
    } else {
        layer = new OpenLayers.Layer.WMS(
            layerOb.name,
            layerOb.url,
            layerOb.params,
            layerOb.OLparams
        );
    }

    // Default to the overlay not being shown.
    if (layerOb.params['isBaseLayer'] == 'false') {
        layer.setVisibility(false);
    }

    map.addLayer(layer);

    if (layerOb.id == defaultLayer && layerOb.params['isBaseLayer'] == 'true') {
        map.setBaseLayer(layer);
    }
}

function deactivateWatersControl() {
    watersControl.deactivate();
}

function activateWatersControl() {
    watersControl.activate();
}

function activateDrawFeature() {
    drawFeatureControl.activate();
    drawFeatureLayer.setVisibility(true);
}

function deactivateDrawFeature() {
    drawFeatureControl.deactivate();
    drawFeatureLayer.setVisibility(false);
}

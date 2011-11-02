Ext.ns("GDP");

GDP.LayerController = Ext.extend(Ext.util.Observable, {
    MAXIMUM_DIMENSION_COUNT : 101,
    baseLayer : undefined,
    getBaseLayer : function() {
        return this.baseLayer;
    },
    layer : undefined,
    getLayer : function() {
        return this.layer;
    },
    derivative : undefined,
    getDerivative : function() {
        return this.derivative;
    },
    scenario : undefined,
    getScenario : function() {
        return this.scenario;
    },
    gcm : undefined,
    getGcm : function() {
        return this.gcm;
    },
    time : undefined,
    getTime : function() {
        return this.time;
    },
    dimensions : undefined,
    getDimension : function(extentName) {
        return this.dimensions[extentName];
    },
    getAllDimensions : function() {
        return this.dimensions;
    },
    zaxisName : undefined,
    getZAxisName : function() {
        return this.zaxisName;
    },
    layerOpacity : 0.8,
    getLayerOpacity : function() {
        return this.layerOpacity;
    },
    legendStore : undefined,
    getLegendStore : function() {
        return this.legendStore;
    },
    legendRecord : undefined,
    getLegendRecord : function() {
        return this.legendRecord;
    },
    wmsCapsStore : undefined,
    getWMSCapsStore : function() {
        return this.wmsCapsStore;
    },
    opendapEndpoint : undefined,
    getOPeNDAPEndpoint : function() {
        return this.opendapEndpoint;
    },
    featureLayer : [],
    getCurrentFeatureLayer : function() {
        return this.featureLayer;
    },
    constructor : function(config) {
        LOG.debug('LayerController:constructor: Constructing self.');
            
        if (!config) config = {};

        this.layerOpacity = config.layerOpacity || this.layerOpacity;
        this.legendStore = config.legendStore || this.legendStore;
        this.wmsCapsStore = config.wmsCapsStore || this.wmsCapsStore;
            
        var filledDims = {
            'time' : ''
        }; 
        Ext.each(config.dimensions, function(item) {
            filledDims[item] = '';
        }, this);
        this.dimensions = filledDims;

        GDP.LayerController.superclass.constructor.call(this, config);
        LOG.debug('LayerController:constructor: Construction complete.');
            
        LOG.debug('LayerController:constructor: Registering Observables.');
        this.addEvents(
            "changebaselayer",
            "changelayer",
            "changelegend",
            "changedimension",
            "changeopacity",
            "drewbbox",
            "bboxbuttonactivated",
            "creategeomoverlay",
            "submit-bounds",
            "selected-dataset",
            "selected-deriv",
            "loaded-capstore",
            "loaded-catstore",
            "loaded-derivstore",
            "exception-capstore",
            "exception-catstore",
            "requestfoi",
            "updateplotter"
        );
            
        // There shouldn't be anything listening at this point. 
        LOG.trace('LayerController:constructor: replacing base layer for this object. The next notification firing shouldn\'t be picked up by any other object at this point in the initialization');
        this.requestBaseLayer(config.baseLayer);

    },
    requestBaseLayer : function(baseLayer) {
        LOG.debug('LayerController:requestBaseLayer');
        if (!baseLayer) return;
        this.baseLayer = baseLayer;
        LOG.debug('LayerController:requestBaseLayer: Added new base layer to LayerController. Firing "changebaselayer".');
        this.fireEvent('changebaselayer');
    },
    requestLayer : function(layerRecord) {
        LOG.debug('LayerController:requestLayer');
        if (!layerRecord) return;
        this.layer = layerRecord;
		
        var dims = layerRecord.get('dimensions');
        Ext.iterate(dims, function(key, value) {
            this.modifyDimensions(key, value['default']);
        }, this);
		
        var layerName = layerRecord.get('name');
        //this.zaxisName = dims.elevation.name + ' (' + dims.elevation.units + ')
        //TODO switch back to above, with proper variable name
        this.zaxisName = 'Threshold (' + dims.elevation.units + ')';
        LOG.debug('LayerController:requestLayer: Firing event "changelayer".');
        this.modifyLegendStore(layerRecord.data);
        this.fireEvent('changelayer', {
            zaxisName : this.zaxisName,
            record : layerRecord
        });
    },
    requestDerivative : function(derivRecord) {
        LOG.debug('LayerController:requestDerivative');
        if (!derivRecord) return;
        this.derivative = derivRecord;
        
        // TODO get derivative record and modify scenario and gcm stores
        //var derivName = derivRecord.get('derivative');
        this.fireEvent('changederiv', {
            record : derivRecord
        })
    },
    requestScenario : function(scenRecord) {
        LOG.debug('LayerController:requestScenario');
        if (!scenRecord) return;
        this.scenario = scenRecord;
        
        // TODO get derivative record and modify scenario and gcm stores
        //var derivName = derivRecord.get('derivative');
        this.fireEvent('changescenario', {
            record : scenRecord
        })
    },
    requestGcm : function(gcmRecord) {
        LOG.debug('LayerController:requestGcm');
        if (!gcmRecord) return;
        this.gcm = gcmRecord;
        
        // TODO get derivative record and modify scenario and gcm stores
        //var derivName = derivRecord.get('derivative');
        this.fireEvent('changegcm', {
            record : gcmRecord
        })
    },
    requestLegendStore : function(legendStore) {
        LOG.debug('LayerController:requestLegendStore: Handling request.');
        if (!legendStore) return;
        this.legendStore = legendStore;
        LOG.debug('LayerController:requestLegendStore: Firing event "changelegend".');
        this.fireEvent('changelegend');
    },
    modifyLegendStore : function(jsonObject) {
        LOG.debug('LayerController:modifyLegendStore: Handling request.');
        if (!jsonObject) return;
        if (!this.legendStore) return;
        this.legendStore.loadData(jsonObject);
            
        //  http://internal.cida.usgs.gov/jira/browse/GDP-372
        var recordIndex = this.legendStore.find('name', GDP.DEFAULT_LEGEND_NAME);
        recordIndex = (recordIndex < 0) ? 0 : recordIndex;
            
        this.requestLegendRecord(this.legendStore.getAt(recordIndex));
            
        LOG.debug('LayerController:modifyLegendStore: Firing event "changelegend".');
        this.fireEvent('changelegend');
    },
    requestLegendRecord : function(legendRecord) {
        LOG.debug('LayerController:requestLegendRecord: Handling request.');
        if (!legendRecord) return;
        this.legendRecord = legendRecord;
        LOG.debug('LayerController:requestLegendRecord: Firing event "changelegend".');
        this.fireEvent('changelegend');
    },
    requestOpacity : function(opacity) {
        if (!opacity && opacity !== 0) return;
        LOG.debug('LayerController:requestOpacity: Handling request.');
        if (0 <= opacity && 1 >= opacity) {
            LOG.debug('LayerController:requestOpacity: Setting opacity to ' + opacity);
            this.layerOpacity = opacity;
            LOG.debug('LayerController:requestOpacity: Firing event "changeopacity".');
            this.fireEvent('changeopacity');
        }
    },
    requestFeatureOfInterest : function(args) {
        LOG.debug('LayerController:requestFeatureOfInterest: Firing Event "requestfoi".');
        this.fireEvent('requestfoi', args);
    },
    requestDimension : function(extentName, value) {
        LOG.debug('LayerController:requestDimension: Handling request.');
        if (!extentName) return;
        if (this.modifyDimensions(extentName, value)) {
            LOG.debug('LayerController:requestDimension: Firing event "changedimension".');
            this.fireEvent('changedimension');
        } else {
            LOG.info('Requested dimension (' + extentName + ') does not exist');
        }
    },
    modifyDimensions : function(extentName, value) {
        LOG.debug('LayerController:modifyDimensions: Handling request.');
        if (this.dimensions.hasOwnProperty(extentName)) {
            var val = value || '';
            this.dimensions[extentName] = val;
            return true;
        } else {
            return false;
        }
    },
    loadDimensionStore : function(record, store, extentName, maxCount) {
        if (!record || !store || !extentName) return null;
        LOG.debug('LayerController:loadDimensionStore: Handling request.');
        var maxNum = maxCount || this.MAXIMUM_DIMENSION_COUNT;

        store.removeAll();

        var extents = record.get('dimensions')[extentName];
        if (extents) {
            var currentExtent = extents['default'];
            var timesToLoad = [];
            Ext.each(extents.values, function(item, index, allItems){
                if (index > maxNum) {
                    return false;
                } else {
                    timesToLoad.push([item.trim()]);
                }
                return true;
            }, this);

            store.loadData(timesToLoad);
            return {
                currentExtent : currentExtent,
                loadedData : timesToLoad
            }
        } else {
            return null;
        }
    },
    drewBoundingBox : function(args) {
        LOG.debug('LayerController:drewBoundingBox: Polygan drawn. Firing event: "drewbbox".');
        this.fireEvent('drewbbox', args);
    },
    boundingBoxButtonActivated : function(args) {
        LOG.debug('LayerController:drewBoundingBox: Bounding Box button clicked. Firing event: "bboxbuttonclicked"')
        this.fireEvent('bboxbuttonactivated', args);
    },
    createGeomOverlay : function(args) {
        LOG.debug('LayerController:createGeomOverlay: Polygon requested. Firing event: "creategeomoverlay".');
        this.fireEvent('creategeomoverlay', args);
    },
    submitBounds : function(args) {
        LOG.debug('LayerController:submitBounds: Firing event "submit-bounds"');
        args = Ext.apply({
            controller : this
        }, args)
        this.fireEvent('submit-bounds', args);
    },
    selectedDataset : function(args) {
        LOG.debug('LayerController:selectedDataset: Firing event "selected-dataset"');
        this.fireEvent('selected-dataset', args);
    },
    selectedDerivative : function(args) {
        LOG.debug('LayerController:selectedDerivative: Firing event "selected-deriv"');
        this.fireEvent('selected-deriv', args);
    },
    loadedCapabilitiesStore : function(args) {
        LOG.debug('LayerController:loadedCapabilitiesStore: Firing event "loaded-capstore"');
        this.modifyLegendStore(args.record.data);
        this.fireEvent('loaded-capstore', args);
    },
    loadedGetRecordsStore : function(args) {
        LOG.debug('LayerController:loadedGetRecordsStore: Firing event "loaded-catstore"');
        this.fireEvent('loaded-catstore', args);
    },
    loadedDerivStore : function(args) {
        LOG.debug('LayerController:loadedDerivStore: Firing event "loaded-derivstore"');
        //this.modifyLegendStore(args.record.data);
        this.fireEvent('loaded-derivstore', args);
    },
    loadedLeafStore : function(args) {
        LOG.debug('LayerController:loadedLeafStore: Firing event "loaded-leafstore"');
        //this.modifyLegendStore(args.record.data);
        this.fireEvent('loaded-leafstore', args);
    },
    capabilitiesExceptionOccurred : function(args) {
        LOG.debug('LayerController:capabilitiesExceptionFired: Firing event "exception-capstore"');
        if (LOADMASK) LOADMASK.hide();
        NOTIFY.error({
            msg : 'Could not access WMS endpoint. Application will not be functional until another endpoint is chosen.'
        });
        this.fireEvent('exception-capstore', args);
    },
    getRecordsExceptionOccurred : function(args) {
        LOG.debug('LayerController:getRecordsExceptionFired: Firing event "exception-catstore"');
        if (LOADMASK) LOADMASK.hide();
        NOTIFY.error({
            msg : 'Could not access CSW catalog. Application will not be functional until another endpoint is chosen.'
        });
        this.fireEvent('exception-catstore', args);
    },
    setOPeNDAPEndpoint : function(args) {
        LOG.debug('LayerController:setOPeNDAPEndpoint: Setting current OPeNDAP endpoint to ' + args);
        this.opendapEndpoint = args;
    },
    updatePlotter : function(args) {
        LOG.debug('LayerController:updatePlotter: Firing event "updateplotter"');
        this.fireEvent('updateplotter', args);
    }
});
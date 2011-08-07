Ext.ns("GDP");

GDP.PolygonPOIPanel = Ext.extend(Ext.Panel, {
    layerController : undefined,
    west : undefined,
    south : undefined, 
    east : undefined,
    north : undefined,
    getCoords : function() {
        return {
            west : this.west,
            south : this.south,
            east : this.east,
            north : this.north
        }
    },
    constructor : function(config) {
        LOG.debug('PolygonPOIPanel:constructor: Constructing self.');
        
        if (!config) config = {};
        this.layerController = config.layerController;
        
        var coordPanel; 
        var westboundLonTexbox = new Ext.form.TextField({
            id : 'west-box',
            fieldLabel : 'West Lon'
        });
        var northboundLatTextbox = new Ext.form.TextField({
            id : 'north-box',
            fieldLabel : 'North Lat'
        });
        var southboundLatTextbox = new Ext.form.TextField({
            id : 'south-box',
            fieldLabel : 'South Lat'
        });
        var eastboundLonTextBox = new Ext.form.TextField({
            id : 'east-box',
            fieldLabel : 'East Lon'
        });
        coordPanel = new Ext.Panel({
            id : 'coord-text-panel',
            region : 'center',
            layout : 'form',
            title : 'Coordinates',
            items : [
                westboundLonTexbox, 
                northboundLatTextbox, 
                southboundLatTextbox, 
                eastboundLonTextBox
            ]
        })
        
        config = Ext.apply({
//            layout : 'anchor',
            items : [
                coordPanel
            ]
        }, config);
        GDP.PolygonPOIPanel.superclass.constructor.call(this, config);
        LOG.debug('PolygonPOIPanel:constructor: Construction complete.');
    },
      initComponent : function() {
          GDP.PolygonPOIPanel.superclass.initComponent.call(this);
          this.addEvents();
    }
});
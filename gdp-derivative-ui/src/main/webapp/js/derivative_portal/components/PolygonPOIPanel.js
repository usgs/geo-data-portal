Ext.ns("GDP");

GDP.PolygonPOIPanel = Ext.extend(Ext.Panel, {
    layerController : undefined,
    west : undefined,
    south : undefined, 
    east : undefined,
    north : undefined,
    setCoords : function(args) {
        this.get('coord-text-panel').get('west-box').setValue(args.west.lon);
        this.get('coord-text-panel').get('south-box').setValue(args.south.lat);
        this.get('coord-text-panel').get('east-box').setValue(args.east.lon);
        this.get('coord-text-panel').get('north-box').setValue(args.north.lat);
    },
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
        this.west = config.west || '';
        this.south = config.south || '';
        this.east = config.east || '';
        this.north = config.north || '';
        
        var coordPanel; 
        var westboundLonTexbox = new Ext.form.TextField({
            id : 'west-box',
            fieldLabel : 'West Lon',
            value : this.west.lon
        });
        var northboundLatTextbox = new Ext.form.TextField({
            id : 'north-box',
            fieldLabel : 'North Lat',
            value : this.north.lat
        });
        var southboundLatTextbox = new Ext.form.TextField({
            id : 'south-box',
            fieldLabel : 'South Lat',
            value : this.south.lat
        });
        var eastboundLonTextBox = new Ext.form.TextField({
            id : 'east-box',
            fieldLabel : 'East Lon',
            value : this.east.lon
        });
        coordPanel = new Ext.Panel({
            id : 'coord-text-panel',
            region : 'center',
            layout : 'form',
            title : 'Coordinates',
            border : false,
            items : [
                westboundLonTexbox, 
                southboundLatTextbox, 
                eastboundLonTextBox,
                northboundLatTextbox
            ]
        })
        
        var configItems = [coordPanel];
        var button = config.submitButton;
        if (button) {
            button.setTextBoxes({
                westBox : westboundLonTexbox, 
                southBox : southboundLatTextbox, 
                northBox : northboundLatTextbox, 
                eastBox : eastboundLonTextBox
            })
            configItems.push(button);
        }
        
        config = Ext.apply({
            items : configItems
        }, config);
        GDP.PolygonPOIPanel.superclass.constructor.call(this, config);
        LOG.debug('PolygonPOIPanel:constructor: Construction complete.');
    },
      initComponent : function() {
          GDP.PolygonPOIPanel.superclass.initComponent.call(this);
          this.addEvents();
    }
});
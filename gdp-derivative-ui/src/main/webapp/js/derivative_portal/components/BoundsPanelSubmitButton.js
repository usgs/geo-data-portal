Ext.ns("GDP");

GDP.BoundsPanelSubmitButton = Ext.extend(Ext.Button, {
    layerController : undefined,
    textBoxes : undefined,
    validator : undefined,
    setTextBoxes : function(args) {
        this.textBoxes.westBox = args.westBox;
        this.textBoxes.southBox = args.southBox;
        this.textBoxes.eastBox = args.eastBox;
        this.textBoxes.northBox = args.northBox;
    },
    constructor : function(config) {
        LOG.debug('BoundsPanelSubmitButton:constructor: Constructing self.');
        
        if (!config) config = {};
        this.layerController = config.layerController;
        this.textBoxes = config.textBoxes || {};
        this.validator = config.validator || true;
        config = Ext.apply({
            id : 'boundspanel-submit-button',
            text : 'Get Data'
        }, config);
        GDP.PolygonPOIPanel.superclass.constructor.call(this, config);
        LOG.debug('BoundsPanelSubmitButton:constructor: Construction complete.');
    },
    onClick : function(button, eventObj) {
        LOG.debug('BoundsPanelSubmitButton:click: Handling Request.');
        var north = this.textBoxes.northBox.getValue();
        var south = this.textBoxes.southBox.getValue();
        var east = this.textBoxes.eastBox.getValue()
        var west = this.textBoxes.westBox.getValue()
        
        LOG.debug('West: ' + west);
        LOG.debug('South: ' + south);
        LOG.debug('East: ' + east);
        LOG.debug('North: ' + north);
        
        // Do bounding box validation
        var valid = this.validator({
            northBox : this.textBoxes.northBox,
            southBox : this.textBoxes.southBox, 
            eastBox : this.textBoxes.eastBox,
            westBox : this.textBoxes.westBox
        });
        if (valid) {
            LOG.debug('BoundsPanelSubmitButton:click:validator returned true');
            
            // Let's draw the polygon on the map
            // We need to create some boundaries first
            var bounds = new OpenLayers.Bounds();
            bounds.extend(new OpenLayers.LonLat(west, south));
            bounds.extend(new OpenLayers.LonLat(east, north));
            
            this.layerController.submitBounds({bounds : bounds});
        } else {
            LOG.debug('BoundsPanelSubmitButton:click:validator returned false')
        }
        
        
 
    }
});
Ext.ns("GDP");

GDP.BoundsPanelSubmitButton = Ext.extend(Ext.Button, {
    layerController : undefined,
    textBoxes : undefined,
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
        
        config = Ext.apply({
            id : 'boundspanel-submit-button',
            text : 'Submit Bounds'
        }, config);
        GDP.PolygonPOIPanel.superclass.constructor.call(this, config);
        LOG.debug('BoundsPanelSubmitButton:constructor: Construction complete.');
    },
    onClick : function(button, eventObj) {
        LOG.debug('BoundsPanelSubmitButton:click: Handling Request.');
        LOG.debug('West: ' + this.textBoxes.westBox.value);
        LOG.debug('South: ' + this.textBoxes.southBox.value);
        LOG.debug('East: ' + this.textBoxes.eastBox.value);
        LOG.debug('North: ' + this.textBoxes.northBox.value);
    }
});